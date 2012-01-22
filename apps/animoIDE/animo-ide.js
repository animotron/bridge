(function($){

    var editor;
    var search;

    var commands = require("pilot/canon");

    if (window.MozWebSocket) {
        window.WebSocket = window.MozWebSocket;
    }

    function onopen(event) {
        setInterval(function() {
              event.target.send("");
        }, 5 * 60 * 1000);
    }

    var uri = "ws://" + location.host + "/ws";

    var src_s = new WebSocket(uri, "src");
    src_s.onmessage = function (event) {
        editor.getSession().setValue(event.data);
    }
    src_s.onopen = onopen;

    var save_s = new WebSocket(uri, "save");
    save_s.onmessage = src_s.onmessage;
    save_s.onopen = onopen;

    $.fn.ideEditor = function() {
        var self = $(this);
        editor = ace.edit(self.get(0));
        commands.addCommand({
            name: 'save',
            bindKey: {
                win: 'Ctrl-S',
                mac: 'Command-S',
                sender: 'editor'
            },
            exec: function(env, args, request) {
                save_s.send(editor.getSession().getValue());
            }
        });
        return self;
    };

    $.fn.ideSearch = function () {
        search = $(this);
        search.keypress(function(event) {
                if (event.which == 13) {
                    src_s.send(search.val());
                }
        });
        return search;
    };

})(jQuery);
