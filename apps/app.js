(function($){

    var socket = [];
    var uri = "ws://" + window.location.host + "/ws";

    $.socket = {
        close : function (protocol) {
            try {
                var s = socket[protocol];
                if (s) {
                    s.close();
                    clearInterval(s.ping);
                }
            } catch (e) {}
        },
        send : function (message, protocol, callback) {
            var s = socket[protocol];
            var onmessage = function (event) {
                if (s.readyState == 1) {
                    callback(event);
                }
            };
            try {
                s.onmessage = onmessage;
                s.sendMessage(message);
            } catch (e) {
                s = new WebSocket(uri, protocol);
                socket[protocol] = s;
                s.onmessage = onmessage;
                s.onopen = function() {
                    s.send(message);
                    s.ping = setInterval(function(){
                        if (s.readyState == 1) {
                            s.sendMessage("");
                        }
                    }, 44000)
                };
                s.onclose = function() {
                    clearInterval(s.ping);
                };
            }
        }
    };

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

})(jQuery);
