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
                ("the an any ~ all ~~ prefer get <~~ this ?is ?has each " +
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
                        regex : '[^,."\\s\\(\\)]+'
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

    if (window.MozWebSocket) {
        window.WebSocket = window.MozWebSocket;
    }

    var socket = [];
    var uri = "ws://" + window.location.host + "/ws";

    function close (socket) {
        try {
            socket.close();
        } catch (e) {}
    }

    function send (message, protocol, onmessage) {
        var s = socket[protocol];
        try {
            s.onmessage = onmessage;
            s.sendMessage(message);
        } catch (e) {
            s = new WebSocket(uri, protocol);
            socket[protocol] = s;
            s.onmessage = onmessage;
            s.onopen = function() {
                s.send(message);
            };
        }
    }

    $.animoIDE = function(self, sinput, caption, list) {

        var editor;

        window.addEventListener('popstate', function(event){
            if (event.state) {
                open(event.state);
            }
        });

        window.addEventListener('hashchange', function (event) {
            openLocation();
        });

        function openLocation() {
            var id = window.location.hash.substr(1);
            open(id == "" ? "new" : id);
        }

        function open(id) {
            if (editor.getSession().getUndoManager().hasUndo()) {
                window.open(window.location.pathname + "#" + id, "_blank");
            } else {
                send(id, "src", function (event) {
                    var id = getId(event.data);
                    editor.getSession().setValue(event.data);
                    editor.focus();
                });
            }
        }

        function getId(data) {
            return data.split("\n")[0].split(" ")[1].split(".")[0];
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
                send(editor.getSession().getValue(), "save", function(event){
                    current = getId(event.data);
                    editor.getSession().setValue(event.data);
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
            name: 'lookup',
            bindKey: {
                win: 'Ctrl-B',
                mac: 'Command-B',
                sender: 'editor'
            },
            exec: function(env, args, request) {
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
                    open(token[t].value);
                }
            }
        });

        editor = ace.edit(self.get(0));
        editor.getSession().setMode(new AnimoMode());
        openLocation();

        var value = "";
        var canClose = true;
        var mustOpen = true;

        sinput.keypress(function(event) {
           if (event.keyCode == 27) {
                self.show();
                editor.focus();
           }
        }).one("focus", function(){
            sinput.focus(function(){
                if (mustOpen) {
                    value = "";
                };
            });
            setInterval(function(){
                var count = 0;
                var val = sinput.val();
                if (val != value) {
                    close(socket["search"]);
                    setTimeout(function(){
                        var v = sinput.val();
                        if (v == "") {
                            self.show();
                            mustOpen = true;
                        } else if (v == val) {
                            count = 0;
                            caption.html("<h2>Searching...</h2><h6>Not found anything still</h6>");
                            list.html("<ol></ol>");
                            if (mustOpen) {
                                mustOpen = false;
                                self.hide();
                            }
                            send(val, "search", function(event){
                                count++;
                                caption.html("<h2>Searching...</h2><h6>Found " + count + "</h6>");
                                var id = getId(event.data);
                                var item = $("<li><p><a href='#" + id + "'>" + id + "</a></p><pre>" + event.data + "</pre></li>")
                                        .mouseenter(function(){
                                            canClose= false;
                                        }).mouseleave(function(){
                                            sinput.focus();
                                            canClose= true;
                                        });
                                item.find("a").click(function(){
                                    self.show();
                                    editor.focus();
                                });
                                list.find("ol").append(item);
                            });
                        }
                    }, 300);
                    value = val;
                }
            }, 100);
        }).blur(function(event){
            if (canClose) {
                self.show();
                mustOpen = true;
                close(socket["search"]);
            } else {
                sinput.focus();
            }
        });

    };

})(jQuery);
