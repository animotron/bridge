(function($){

    var editor;
    var search;

    if (MozWebSocket) {
        WebSocket = MozWebSocket;
    }

    var uri = "ws://" + location.host + "/ws";

    var src_s = new WebSocket(uri, "src");

    src_s.onmessage = function (event) {
        editor.getSession().setValue(event.data);
    }

    $.fn.ideEditor = function() {
        var self = $(this);
        editor = ace.edit(self.get(0));
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
