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

    var pool  = [];
    var uri   = "ws://" + window.location.host + "/ws";

    $.socket = function(protocol, callback){

        var s = pool[protocol];

        if (s) {

            s.onMessage(callback);

        } else {

            var ping;

            var onMessage = callback;

            var getSocket = function(){
                var socket = new WebSocket(uri, protocol);
                ping = setInterval(function(){
                    if (socket.readyState == 1){
                        socket.send("");
                    }
                }, 44000);
                socket.onclose = function(){
                    clearInterval(ping);
                };
                socket.onmessage = function(event){
                    if (onMessage && socket.readyState == 1) {
                        onMessage(event);
                    }
                };
                return socket;
            };

            var socket = getSocket();

            s = {

                close : function(protocol){
                    socket.close();
                    clearInterval(ping);
                },

                send : function(message){
                    if (socket.readyState == 1){
                        socket.send(message);
                    } else {
                        socket = getSocket();
                        socket.onopen = function(){
                            socket.send(message);
                        };
                    }
                },

                onMessage : function(callback){
                    onMessage = callback
                }

            };

            pool[protocol] = s;

        }

        return s;

    };

})(jQuery);



(function($){

   /*
    *  The live change JQuery plugin
    */

    $.fn.liveChange = function(callback) {
        return $(this).each(function(){
            var self = $(this);
            if (callback) {
                self.bind("liveChange", callback);
            }
            var timer;
            self.focus(function(){
                var value = self.val();
                function callback(){
                    var val = self.val();
                    if (val != value) {
                        value = val;
                        clearInterval(timer);
                        setTimeout(function(){
                            var v = self.val();
                            if (v == val) {
                                self.trigger("liveChange");
                            }
                            timer = setInterval(callback, 250);
                        }, 250);
                    }
           };
                timer = setInterval(callback, 250);
            }).blur(function(){
                clearInterval(timer);
            });
        });
    };

})(jQuery);



(function($){

   /*
    *  The dynamic application environment
    */

    $.app = {

        context : function(root) {
            function build(root) {
                var a = [];
                $.each(root, function(){
                    var item = $(this);
                    var id = item.attr("id");
                    var c = build(item.children());
                    if (id) {
                        var o = {};
                        var v = item.val();
                        if (c.length > 0) {
                            c = c.length == 1 ? c[0] : c;
                            o[id] = v ? [v, c] : c;
                        } else {
                            o[id] = v;
                        }
                        a.push(o);
                    } else {
                        for (var i = 0; i < c.length; i++) {
                            a.push(c[i]);
                        }
                    }
                });
                return a;
            }
            var c = build(root ? root : $("body"));
            return c.length == 1 ? c[0] : c;
        },

        eval : function (action, id, value) {
            var exp = {
                id      : id,
                action  : action,
                value   : value,
                request : document.cookie["request"],
                state   : $.app.context()
            };
            $.socket(
                "app",
                function(event){
                    eval("(function($){" + event.data + "})(jQuery);");
                }
            ).send($.toJSON(exp));
        }

    };

    function resize (){
        $('.frame').css({top: $('nav').height() - 3});
    }

    $(window).resize(resize);

    $('.nav-collapse').on("shown", resize);
    $('.nav-collapse').on("hidden", resize);

    resize();

})(jQuery);
