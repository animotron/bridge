/*
 *  Copyright (C) 2011-2012 The Animo Project
 *  http://animotron.org
 *
 *  This file is part of Animotron.
 *
 *  Animotron is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Animotron is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of
 *  the GNU Affero General Public License along with Animotron.
 *  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
 (function($){

   /*
    *  The simple socket management system
    */

    if (window.MozWebSocket) {
        window.WebSocket = window.MozWebSocket;
    }

    $.socket = {

        pool  : [],
        uri   : "ws://" + window.location.host + "/ws",

        close : function (protocol) {
            var s = $.socket.pool[protocol];
            if (s) {
                s.close();
                clearInterval(s.ping);
            }
        },

        send  : function (message, protocol, callback) {
            var s = $.socket.pool[protocol];
            var send = function () {
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
                $.socket.pool[protocol] = s;
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
    *  The dynamic application environment
    */

    $.app = {
        eval : function (action, id, value) {
            $.socket.send(action + " " + id + " " + value, "app" ,function(event){
                eval("(function($){" + event.data + "})(jQuery);");
            });
        }
    };


})(jQuery);
