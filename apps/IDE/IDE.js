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

    define("ace/mode/animo", function(require, exports, module) {

        var oop = require("pilot/oop");
        var TextMode = require("ace/mode/text").Mode;
        var Tokenizer = require("ace/tokenizer").Tokenizer;
        var AnimoHighlightRules = require("ace/mode/animo_highlight_rules").AnimoHighlightRules;

        var Mode = function() {
            this.$tokenizer = new Tokenizer(new AnimoHighlightRules().getRules());
        };
        oop.inherits(Mode, TextMode);

        (function() {
            // Extra logic goes here. (see below)
        }).call(Mode.prototype);

        exports.Mode = Mode;

    });

    define('ace/mode/animo_highlight_rules', function(require, exports, module) {

        var oop = require("pilot/oop");
        var lang = require("pilot/lang");
        var TextHighlightRules = require("ace/mode/text_highlight_rules").TextHighlightRules;

        var AnimoHighlightRules = function() {

            var operators = lang.arrayToMap(
                ("def an any ~ all ~~ prefer get <~~ this ?is ?has each " +
                 "use weak-use with eq gt ge lt le ne not and or id 's " +
                 "add delete set replace -> --> value qname ptrn").split(" ")
            );

            var ml = lang.arrayToMap(
                ("\\ @ $ !-- !! [!CDATA[ ?? &# ").split(" ")
            );

            var prefix = lang.arrayToMap(
                ("\\ @ $ &# ").split(" ")
            );

            var suffix = lang.arrayToMap(
                ("'s ").split(" ")
            );

            this.$rules = {
                "start" : [
                    {
                        token : "punctuation.operator",
                        regex : ",."
                    }, {
                        token : "paren.lparen",
                        regex : "\\("
                    }, {
                        token : "paren.rparen",
                        regex : "\\)"
                    }, {
                        token : "constant.numeric", // hex
                        regex : "0[xX][0-9a-fA-F]+\\b"
                    }, {
                        token : "constant.numeric", // float
                        regex : "[+-]?\\d+(?:(?:\\.\\d*)?(?:[eE][+-]?\\d+)?)?\\b"
                    }, {
                        token : "keyword", // float
                        regex : "\\^"
                    }, {
                        token : function(value) {
                            if (operators.hasOwnProperty(value))
                                return "keyword";
                            else if (
                                        ml.hasOwnProperty(value) ||
                                        prefix.hasOwnProperty(value[0]) ||
                                        prefix.hasOwnProperty(value.substr(0, 2))
                                    )
                                return "keyword.operator";
                            else
                                return "identifier";
                        },
                        regex : '[^,"\\s\\(\\)]+'
                    }, {
                        token : "string",
                        merge : "true",
                        next  : "string",
                        regex : '"'
                    }
                ],
            "string" : [
                    {
                        token : "string",
                        merge : "true",
                        next  : "string",
                        regex : '(?:(?:\\\\.)|(?:[^"\\\\]))+'
                    }, {
                        token : "string",
                        merge : "true",
                        next  : "string",
                        regex : '[^"]+'
                    }, {
                        token : "string",
                        merge : "true",
                        next  : "string",
                        regex : '[^"]+'
                    }, {
                        token : "string",
                        merge : "true",
                        next  : "start",
                        regex : '.*?"'
                    }
                ]
            };

        }

        oop.inherits(AnimoHighlightRules, TextHighlightRules);
        exports.AnimoHighlightRules = AnimoHighlightRules;

    });

    var editor;
    var current;
    var modified = false;

    var self = $("#editor");
    var sinput = $("#search");
    var caption = $("header");
    var list = $("section");
    var cons = $("#console");
    var res = $("#search-result")


    window.addEventListener('hashchange', function(event){
        if (modified) {
            window.open(event.newURL, "_blank");
            window.history.back();
        } else {
            openLocation();
        }
    });

    function openLocation(){
        var id = window.location.hash.substr(1);
        if (id == "") {
            self.hide();
        } else {
            open(id);
        }
    }

    function open(id) {
        current = id;
        $.socket.send(id, "src", function (event) {
            cons.modal("hide");
            var id = getId(event.data);
            editor.getSession().setValue(event.data);
            modified = false;
            update(id);
            show();
        });
    }

    function update(id) {
        var hash = "#" + id;
        if (hash != window.location.hash) {
            window.history.replaceState(null, null, window.location.pathname + hash);
        }
    }

    function getId(data) {
        var t = data.split("\n")[0].split(" ");
        if (t.length > 1 && t[0] == "def") {
            return t[1].split(".")[0];
        }
        return "";
    }

    var AnimoMode = require("ace/mode/animo").Mode;
    var commands = require("pilot/canon");

    commands.addCommand({
        name: 'save',
        bindKey: {
            win: 'Ctrl-S',
            mac: 'Command-S',
            sender: 'editor'
        },
        exec: function(env, args, request) {
            $.socket.send(editor.getSession().getValue(), "save", function(event){
                cons.modal("hide");
                editor.getSession().setValue(event.data);
                modified = false;
                update(getId(event.data));
            });
        }
    });

    commands.addCommand({
        name: 'eval',
        bindKey: {
            win: 'F9',
            mac: 'F9',
            sender: 'editor'
        },
        exec: function(env, args, request) {
            $.socket.send(editor.getSession().getValue(), "eval", function(event){
                modified = false;
                var W = self.width();
                var H = self.height() + 40;
                var w = W * 0.9;
                var h = H * 0.9;
                cons.css({
                    maxWidth    : w,
                    width       : w,
                    maxHeight   : h,
                    height      : h
                }).one("shown", function(){
                    cons.find(".console-editor").height(cons.innerHeight() - cons.find(".modal-header").outerHeight()- cons.find(".modal-footer").outerHeight());
                    cons.editor.resize();
                }).modal().offset({top: (H-h)/2, left: (W-w)/2});
                cons.editor.getSession().setValue(event.data);
                update(getId(event.data));
            });
        }
    });

    commands.addCommand({
        name: 'open',
        bindKey: {
            win: 'Ctrl-O',
            mac: 'Command-O',
            sender: 'editor'
        },
        exec: function(env, args, request) {
            sinput.focus();
        }
    });

    commands.addCommand({
        name: 'new',
        bindKey: {
            win: 'Ctrl-N',
            mac: 'Command-N',
            sender: 'editor'
        },
        exec: function(env, args, request) {
            window.location.hash = "#new";
        }
    });

    commands.addCommand({
        name: 'new blank',
        bindKey: {
            win: 'Ctrl-Shift-N',
            mac: 'Command-Shift-N',
            sender: 'editor'
        },
        exec: function(env, args, request) {
            window.open(window.location.pathname + "#new");
        }
    });

    function onIdentifier(editor, callback) {
        var pos = editor.getCursorPosition();
        var token = editor.getSession().bgTokenizer.lines[pos.row].tokens;
        var t = 0, n = 0, s = 0;
        for (var i = 0; i < token.length; i++) {
            n += token[i].value.length;
            if (n > pos.column) {
                t = i;
                break;
            }
            s = n;
        }
        if (t > 0 && s == pos.column && token[t].type != "identifier") {
            t--;
        }
        if (token[t].type == "identifier") {
            callback(token[t].value);
        }
    }

    commands.addCommand({
        name: 'lookup',
        bindKey: {
            win: 'Ctrl-B',
            mac: 'Command-B',
            sender: 'editor'
        },
        exec: function(env, args, request) {
            onIdentifier(env.editor, function(id){
                window.location.hash = "#" + id
            });
        }
    });

    commands.addCommand({
        name: 'lookup blank',
        bindKey: {
            win: 'Ctrl-Shift-B',
            mac: 'Command-Shift-B',
            sender: 'editor'
        },
        exec: function(env, args, request) {
            onIdentifier(env.editor, function(id){
                window.open(window.location.pathname + "#" + id, "_blank");
            });
        }
    });

    commands.addCommand({
        name: 'hierarchy',
        bindKey: {
            win: 'Ctrl-H',
            mac: 'Command-H',
            sender: 'editor'
        },
        exec: function(env, args, request) {
            onIdentifier(env.editor, function(id){
                sinput.val("all " + id);
                sinput.focus();
            });
        }
    });

    function show() {
        sinput.unbind("blur");
        res.hide();
        if (current) {
            self.show();
        }
        editor.focus();
        editor.resize();
    }

    editor = ace.edit(self.get(0));
    editor.getSession().setMode(new AnimoMode());
    editor.getSession().doc.on("change", function(){
        modified = editor.getSession().getUndoManager().hasUndo();
    });
    openLocation();

    cons.find(".modal-body").css({padding : 0});
    cons.on("hidden", function(){editor.focus()});
    cons.editor = ace.edit(cons.find(".console-editor").get(0));
    cons.editor.getSession().setMode(new AnimoMode());
    cons.editor.setShowPrintMargin(false);
    cons.editor.setReadOnly(true);

    function safe (s) {
        return s.replace(/&(?!\w+([;\s]|$))/g, "&amp;")
                .replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }

    sinput.keypress(function(event) {
       if (event.keyCode == 27) {
            show();
       }
    }).focus(function(){
        cons.modal("hide");
        sinput.blur(function(event){
            show();
        });
        if (sinput.val() != "") {
            self.hide();
            res.show();
        }
    }).liveChange(function(){
        var val = sinput.val();
        if (val != "") {
            self.hide();
            res.show();
            var count = 0;
            $.socket.close("search");
            caption.html("<a class='close'>&times;</a><h2>Searching...</h2><h6>Not found anything still</h6>");
            list.html("<ol></ol>");
            $.socket.send(val, "search", function(event){
                count++;
                caption.html("<a class='close'>&times;</a><h2>Searching...</h2><h6>Found " + count + "</h6>");
                var id = getId(event.data);
                var hash = "#" + id;
                var item = $("<li><p><a href='" + hash + "'>" + id + "</a></p><pre>" + safe(event.data) + "</pre></li>");
                var canFocus = true;
                item.find("a").click(function(event){
                    canFocus = false;
                    current = id;
                    show();
                    if (modified && hash != window.location.hash) {
                        window.open(window.location.pathname + "#" + id);
                        return false;
                    }
                });
                item.find("p, pre").mouseenter(function(){
                    sinput.unbind("blur");
                    canFocus = true;
                }).mouseleave(function(){
                    if (canFocus) {
                        sinput.focus();
                    }
                });
                list.find("ol").append(item);
            });
        }
    }).val("");

})(jQuery);
