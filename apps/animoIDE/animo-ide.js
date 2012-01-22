(function($){

    var strip;

    var commands = require("pilot/canon");
    commands.addCommand({
        name: 'save',
        bindKey: {
            win: 'Ctrl-S',
            mac: 'Command-S',
            sender: 'editor'
        },
        exec: function(env, args, request) {
            var tab = strip.select()[0];
            tab.socket.send(tab.editor.getSession().getValue());
        }
    });

    if (window.MozWebSocket) {
        window.WebSocket = window.MozWebSocket;
    }

    function onopen(event) {
        setInterval(function() {
              event.target.send("");
        }, 5 * 60 * 1000);
    }

    var uri = "ws://" + location.host + "/ws";

    function title(data) {
        return data.split("\n")[0].split(" ")[1].split(".")[0];
    }

    function search(title) {
        return strip.tabGroup.children("#"+title);
    }

    function append(id, content) {
        strip.append([{text : id}]);
        var tab = strip.tabGroup.children("li").last().attr({id : id});
        strip.select(tab);
        var editor = ace.edit(
            $(strip.contentElement(tab.index())).css({
                position : "absolute",
                top : 36, right : 0, bottom : 0, left : 0,
                padding : 0, overflow : "hidden"
            }).html("<div></div>").children().css({
                position : "absolute",
                top : 4, right : 4, bottom : 4, left : 4,
                overflow : "hidden"
            }).get(0)
        );
        editor.getSession().setValue(content);
        var socket = new WebSocket(uri, "save");
        socket.onmessage = function (event) {
            var id = title(event.data);
            tab.attr({id : id});
            tab.children("span").text(id);
            editor.getSession().setValue(event.data);
        };
        socket.onopen = onopen;
        tab[0].socket = socket;
        tab[0].editor = editor;
    }

    $.fn.ideEditor = function() {
        strip = $(this).kendoTabStrip({
            animation : {
                open : {
                    effects: "none"
                }
            }
        }).data("kendoTabStrip");
        //editor = ace.edit(self.get(0));
        append("new", "the new.");
        return strip;
    };

    $.fn.ideSearch = function () {
        var socket = new WebSocket(uri, "src");
        socket.onmessage = function (event) {
            var id = title(event.data);
            var tab = search(id);
            if (tab.length == 0) {
                append(id, event.data);
            } else {
                strip.select(tab);
            }
        };
        socket.onopen = onopen;
        var input = $(this)
        input.keypress(function(event) {
            if (event.which == 13) {
                var id = input.val();
                var tab = search(id)
                if (tab.length == 0) {
                    socket.send(id);
                } else {
                    strip.select(tab);
                }
            }
        });
        return input;
    };

})(jQuery);
