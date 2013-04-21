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
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
 (function($){

    var COLOR = [
        "Blue", "BlueViolet", "Brown", "CadetBlue", "Chocolate", "CornflowerBlue", "Crimson",
        "DarkBlue", "DarkCyan", "DarkGoldenRod", "DarkGreen", "DarkMagenta", "DarkOliveGreen",
        "DarkOrange", "DarkOrchid", "DarkRed", "DeepPink", "Gold", "GoldenRod", "Green",
        "Indigo", "Maroon", "MediumBlue", "Navy", "Orange", "OrangeRed", "Purple", "Red"
    ]

    var body = $("body");
    var editor = $("#editor");

    var getSelection = function(){
        var position = function(container, offset) {
            var parent = container.nodeType == document.ELEMENT_NODE ? container : container.parentNode;
            return {
                container : $(parent).attr("id"),
                index : (function(){
                            var children = parent.childNodes;
                            for (var i = 0; i < children.length; i++) {
                                if (children.item(i) == container) {
                                    return i;
                                }
                            }
                            return -1;
                        })(),
                offset : offset
            }
        };
        with (window.getSelection().getRangeAt(0)) {
            return {
                session : $.session,
                start : position(startContainer, startOffset),
                end : position(endContainer, endOffset)
            }
        }
    };

    var sessions = {};

    var hideSelection = function(session){
        for (var i = 0; i < session.set.length; i++) {
            var x = session.set[i];
            if (x) {
                x.remove();
            }
        }
        session.set = [];
     };

    var showSelection = function(session){
        console.log(session);
        var rects = (function(){
            var range = function(start, end){
                var container = function (position) {
                    var element = $("#"+position.container).get(0);
                    return position.index == -1 ? element : element.childNodes[position.index];
                }
                var range = document.createRange();
                range.setStart(container(start), start.offset);
                range.setEnd(container(end), end.offset);
                return range.getClientRects();
            };
            with (session.selection) {
                var rects = range(start, end);
                if (rects.length == 0) {
                    return [];
                } else if (rects.length == 1) {
                    return [rects.item(0)];
                } else {
                    var left = rects.item(0).left;
                    var right = rects.item(0).right;
                    var top = rects.item(0).top;
                    var bottom = rects.item(0).bottom;
                    for (var i = 1; i < rects.length; i++) {
                        var rect = rects.item(i);
                        left = Math.min(left, rect.left);
                        right = Math.max(right, rect.right);
                        top = Math.min(top, rect.top);
                        bottom = Math.max(bottom, rect.bottom);
                    }
                    s = range(start, start).item(0);
                    e = range(end, end).item(0);
                    if (!e) {
                        e = {left : left, right : right, top: s.bottom, bottom : bottom};
                    }
                    return [
                        {left : s.left, right : right, top : s.top, bottom : s.bottom},
                        {left : left, right : right, top : s.bottom, bottom: e.top},
                        {left : left, right : e.right, top : e.top, bottom: e.bottom}
                    ];
                }
            }
        })();
        var set = [];
        for (var i = 0; i < rects.length; i++) {
            with (rects[i]){
                var width = right - left;
                var height = bottom - top;
                var x = $("<div></div>")
                    .width(width > 0 ? width : 2)
                    .height(height)
                    .css("background-color", session.color)
                    .css("position", "absolute")
                    .css("opacity", width > 0 ? 0.2 : 1)
                    .css("left", left + body.scrollLeft())
                    .css("top", top + body.scrollTop());
                body.append(x);
                set[i] = x;
            }
         }
        session.set = set;
    };

    var echoSocket = $.socket("echo", function(event){
        var data = JSON.parse(event.data);
        if (data.session == $.session) {
            return;
        }
        var session = sessions[data.session];
        if (session) {
            hideSelection(session);
            session.selection = data;
        } else {
            session = {selection : data, set : [], color : COLOR[Math.round(COLOR.length * Math.random())]};
            sessions[data.session] = session;
        }
        showSelection(session);
    });

    var sendSelection = function() {
        echoSocket.send(JSON.stringify(getSelection()));
    };

    var resize = function(){
        for (var key in sessions) {
            var session = sessions[key];
            hideSelection(session);
            showSelection(session);
        }
    };

    editor.mouseup(sendSelection);
    editor.keyup(sendSelection);
    $(window).resize(resize);

})(jQuery);
