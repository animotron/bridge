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

    var editor, sinput;

    if (window.MozWebSocket) {
        window.WebSocket = window.MozWebSocket;
    }

    window.addEventListener('popstate', function(event){
        if (event.state) {
            open(event.state);
        }
    });

    window.addEventListener('hashchange', function (event) {
        open(window.location.hash.substr(1));
    });

    var uri = "ws://" + window.location.host + "/ws";

    var socket = new WebSocket(uri, "src");
    socket.onmessage = function (event) {
        var id = getId(event.data);
        editor.getSession().setValue(event.data);
        editor.focus();
    };
    socket.onopen = function (event) {
        var id = window.location.hash.substr(1);
        socket.send(id == "" ? "new" : id);
        onopen(event);
    };

    function onopen(event) {
        setInterval(function() {
              event.target.send("");
        }, 5 * 60 * 1000);
    }

    function open(id) {
        socket.send(id);
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
            editor.socket.send(editor.getSession().getValue());
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

    function getId(data) {
        return data.split("\n")[0].split(" ")[1].split(".")[0];
    }

    $.fn.ideEditor = function() {
        self = $(this);
        editor = ace.edit(self.get(0));
        editor.getSession().setMode(new AnimoMode());
        editor.socket = new WebSocket(uri, "save");
        editor.socket.onmessage = function (event) {
            current = getId(event.data);
            editor.getSession().setValue(event.data);
        };
        editor.socket.onopen = onopen;
        return self;
    };

    $.fn.ideSearch = function () {
        var socket;
        var value = "";
        var canClose = true;
        var mustOpen = true;
        sinput = $(this).keypress(function(event) {
           if (event.keyCode == 37) {
                editor.focus();
           }
        }).one("focus", function(){
            sinput.focus(function(){
                if (mustOpen) {
                    value = "";
                };
            });
            setInterval(function(){
                var val = sinput.val();
                if (val != value) {
                    try {
                        socket.close();
                    } catch (e) {}
                    setTimeout(function(){
                        var v = sinput.val();
                        if (v == "") {
                            sinput.popover("hide");
                            mustOpen = true;
                        } else if (v == val) {
                            list.html("");
                            if (mustOpen) {
                                mustOpen = false;
                                sinput.popover("show");
                                var left = sinput.offset().left;
                                var max = $(window).width() - 600;
                                if (left > max) {
                                    left = max / 2;
                                } else {
                                    tip.find(".arrow").css({left : sinput.width() / 2});
                                }
                                tip.css({left : left});
                            }
                            socket = new WebSocket(uri, "search");
                            socket.onmessage = function (event) {
                                var id = getId(event.data);
                                var target = editor.getSession().getUndoManager().hasUndo()
                                                ? "target='_blank'" : "";
                                var a = $("<a href='#" + id + "'" + target + ">" + id + "</a><pre>" + event.data + "</pre>")
                                        .mouseenter(function(){
                                            canClose= false;
                                        }).mouseleave(function(){
                                            sinput.focus();
                                            canClose= true;
                                        });
                                        list.append(a);
                            };
                            socket.onopen = function(){
                                socket.send(val);
                            }
                        }
                    }, 300);
                    value = val;
                }
            }, 100);
        }).blur(function(event){
            if (canClose) {
                sinput.popover("hide");
                mustOpen = true;
                try {
                    socket.close();
                } catch (e) {}
            } else {
                sinput.focus();
            }
        });
        var tip = $(sinput.popover({
            placement : "bottom",
            trigger   : "manual",
            title     : "Searching..."
        }).data().popover.tip());
        var list = tip.find(".inner").width(600).find("p").css({
            overflow : "auto",
            maxHeight : "400px"
        });
        $(window).resize(function(){
            sinput.blur();
        });
        return sinput;
    };

})(jQuery);
