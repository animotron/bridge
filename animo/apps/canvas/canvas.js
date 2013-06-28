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
(function(){

    var figures = [];
    var focused = {};

    var canvas = document.getElementById("canvas").getContext("2d");

    function f(x) {
        if (x instanceof Function) return x;
        return function(){return x;};
    }

    function point(x, y){return {x : f(x), y : f(y), move : function(p){this.x = p.x; this.y= p.y;}};}

    function size(width, height){return {width : width, height : height};}

    function rect(center, size){

        var _ = {center : f(center), size : f(size)};

        var a = function(){return _.size().width / 2;};
        var b = function(){return _.size().height / 2;};

        _.left   = function(){return _.center().x() - a();};
        _.right  = function(){return _.center().x() + a();};
        _.top    = function(){return _.center().y() - b();};
        _.bottom = function(){return _.center().y() + b();};

        _.N  = function(){return point(_.center().x, _.top);};
        _.NE = function(){return point(_.right, _.top);};
        _.E  = function(){return point(_.right, _.center().y);};
        _.SE = function(){return point(_.right, _.bottom);};
        _.S  = function(){return point(_.center().x, _.bottom);};
        _.SW = function(){return point(_.left, _.bottom);};
        _.W  = function(){return point(_.left, _.center().y);};
        _.NW = function(){return point(_.left, _.top);};

        _.moveCenter = function(p){center.move(p);};

        _.resize = function(s){size.width = s.width; size.height = s.height;};

        _.render = function(){
            canvas.fillRect(_.left(), _.top(), size.width, size.height);
        };

        _.contains = function(p){
            return p.x() >= _.left() && p.x() <= _.right() && p.y() >= _.top() && p.y() <= _.bottom();
        };

        return _;
    }

    function anchor(point){
        var r = 4;
        var _ = rect(point, size(r, r));
        return _;
    }

    function figure(center, size){

        var _ = rect(center, size);

        var anchors = [
            anchor(_.NW), anchor(_.N), anchor(_.NE),
            anchor(_.W), anchor(_.center), anchor(_.E),
            anchor(_.SW), anchor(_.S), anchor(_.SE)
        ];

        _.renderAnchors = function(){
            for (var i in anchors){
                anchors[i].render();
            }
        };

        _.focus = function(){
            focused[_] = true;
        }

        _.unfocus = function(){
            delete focused[_];
        }

        figures.push(_);

        return _;
    }

    function render() {
        for (var i in figures) {
            figures[i].render();
        }
    }

    z = point(25, 25);
    p = point(50, 50);
    s = size(10, 10);
    g = figure(p, s);

})();
