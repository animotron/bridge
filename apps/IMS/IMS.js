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
