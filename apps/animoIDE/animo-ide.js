(function($){

    var strip, sinput;

    if (window.MozWebSocket) {
        window.WebSocket = window.MozWebSocket;
    }

    window.addEventListener('popstate', function(event){
        select(event.state);
    }, false);

    var uri = "ws://" + window.location.host + "/ws";

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
    socket.onopen = function (event) {
        var id = window.location.search.split("&")[0].substr(1);
        socket.send(id == "" ? "new" : id);
        socket.onopen = onopen;
    };

    function open(id) {
        socket.send(id);
    }

    function select(id) {
        var tab = search(id)
        if (tab.length == 0) {
            open(id);
        } else {
            strip.select(tab);
            tab[0].editor.focus();
        }
    }

    function onopen(event) {
        setInterval(function() {
              event.target.send("");
        }, 5 * 60 * 1000);
    }

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

    commands.addCommand({
        name: 'close',
        bindKey: {
            win: 'Ctrl-W',
            mac: 'Command-W',
            sender: 'editor'
        },
        exec: function(env, args, request) {
            var tab = strip.select();
            window.history.back();
            tab[0].socket.close();
            strip.remove(tab);
        }
    });

    commands.addCommand({
        name: 'open',
        bindKey: {
            win: 'Ctrl-O',
            mac: 'Command-O',
            sender: 'editor'
        },
        exec: function(env, args, request) {
            sinput.focus();
        }
    });

    function title(data) {
        return data.split("\n")[0].split(" ")[1].split(".")[0];
    }

    function search(title) {
        return strip.tabGroup.children("#"+title);
    }

    function push(id) {
        window.history.pushState(id, null, window.location.pathname + "?" + id);
    }

    function append(id, content) {
        push(id);
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
        editor.focus();
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
            },
            select : function(tab) {
                push($(tab.item).attr("id"));
                tab.item.editor.focus();
            }
        }).data("kendoTabStrip");
        return strip;
    };

    $.fn.ideSearch = function () {
        sinput = $(this)
        sinput.keypress(function(event) {
            if (event.which == 13) {
                select(event.target.val());
            }
        });
        return sinput;
    };

})(jQuery);
