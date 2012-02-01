(function($){

   /*
    *  The simple socket management system
    */

    if (window.MozWebSocket) {
        window.WebSocket = window.MozWebSocket;
    }

    $.socket = {
        spool : [],
        uri   : "ws://" + window.location.host + "/ws",
        close : function (protocol) {
            var s = $.socket.spool[protocol];
            if (s) {
                s.close();
                clearInterval(s.ping);
            }
        },
        send  : function (message, protocol, callback) {
            var s = $.socket.spool[protocol];
            function send() {
                s.onmessage = function(event){
                    if (s.readyState == 1) {
                        callback(event);
                    }
                };
                s.send(message);
            }
            if (s && s.readyState == 1) {
                send();
            } else {
                s = new WebSocket($.socket.uri, protocol);
                s.onopen = function() {
                    send();
                    s.ping = setInterval(function(){
                        if (s.readyState == 1) {
                            s.send("");
                        }
                    }, 44000)
                };
                s.onclose = function() {
                    clearInterval(s.ping);
                };
                $.socket.spool[protocol] = s;
            }
        }
    };




   /*
    *  The live change JQuery plugin
    */

    $.fn.liveChange = function(callback) {
        return $(this).each(function(){
            var self = $(this);
            if (callback) {
                self.bind("liveChange", callback);
            }
            var value = self.val();
            setInterval(function(){
                var val = self.val();
                if (val != value) {
                    value = val;
                    setTimeout(function(){
                        var v = self.val();
                        if (v == val) {
                            self.trigger("liveChange");
                        }
                    }, 300);
                }
            }, 100);
        });
    };



   /*
    *  The live change JQuery plugin
    */



})(jQuery);
