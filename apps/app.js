(function($){

   /*
    *  The simple socket management system
    */

    if (window.MozWebSocket) {
        window.WebSocket = window.MozWebSocket;
    }

    var socket = [];
    var uri = "ws://" + window.location.host + "/ws";

    $.socket = {
        close : function (protocol) {
            var s = socket[protocol];
            if (s) {
                s.close();
                clearInterval(s.ping);
            }
        },
        send : function (message, protocol, callback) {
            var s = socket[protocol];
            if (s && s.readyState == 1) {
                s.send(message);
            } else {
                s = new WebSocket(uri, protocol);
                s.onmessage = function(event){
                    if (s.readyState == 1) {
                        callback(event);
                    }
                };
                s.onopen = function() {
                    s.send(message);
                    s.ping = setInterval(function(){
                        if (s.readyState == 1) {
                            s.send("");
                        }
                    }, 44000)
                };
                s.onclose = function() {
                    clearInterval(s.ping);
                };
                socket[protocol] = s;
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
