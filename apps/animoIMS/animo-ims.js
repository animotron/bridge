(function($){

    var search;

    if (window.MozWebSocket) {
        window.WebSocket = window.MozWebSocket;
    }

    function reopenSocket() {
        return this;
    }

    var uri = "ws://" + location.host + "/ws";

    var ims_s = new WebSocket(uri, "animoIMS");
    ims_s.onmessage = function (event) {
    	$("#workspace").html(event.data);
    }
    ims_s.onclose = reopenSocket;

    $.fn.workspaceSearch = function () {
        search = $(this);
        search.keypress(function(event) {
            if (event.which == 13) {
                ims_s.send(search.val());
            }
        });
        return search;
    };

})(jQuery);
