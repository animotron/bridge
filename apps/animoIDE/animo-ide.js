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
                        token : "constant.language",
                        regex : "\\(|\\)"
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
                                return "constant.language";
                            else
                                return "identifier";
                        },
                        regex : '[^,."\\s\\(\\)]+'
                    }, {
                        token : "string",
                        regex : '["](?:(?:\\\\.)|(?:[^"\\\\]))*?["]'
                    }
                ]
            };

        }

        oop.inherits(AnimoHighlightRules, TextHighlightRules);
        exports.AnimoHighlightRules = AnimoHighlightRules;

    });

    var strip, sinput;

    if (window.MozWebSocket) {
        window.WebSocket = window.MozWebSocket;
    }

    window.addEventListener('popstate', function(event){
        select(event.state);
    }, false);

    var uri = "ws://" + window.location.host + "/ws";

    var socket = new WebSocket(uri, "src");
    socket.onmessage = function (event) {
        var id = title(event.data);
        var tab = search(id);
        if (tab.length == 0) {
            append(id, event.data);
        } else {
            strip.select(tab);
        }
    };
    socket.onopen = function (event) {
        var id = window.location.hash.substr(1);
        socket.send(id == "" ? "new" : id);
        socket.onopen = onopen;
    };

    function open(id) {
        socket.send(id);
    }

    function select(id) {
        var tab = search(id)
        if (tab.length == 0) {
            open(id);
        } else {
            strip.select(tab);
            tab[0].editor.focus();
        }
    }

    function onopen(event) {
        setInterval(function() {
              event.target.send("");
        }, 5 * 60 * 1000);
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
            var tab = strip.select()[0];
            tab.socket.send(tab.editor.getSession().getValue());
        }
    });

    commands.addCommand({
        name: 'close',
        bindKey: {
            win: 'Ctrl-W',
            mac: 'Command-W',
            sender: 'editor'
        },
        exec: function(env, args, request) {
            var tab = strip.select();
            window.history.back();
            tab[0].socket.close();
            strip.remove(tab);
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
            var tab = strip.select()[0];
            var pos = tab.editor.getCursorPosition();
            var token = tab.editor.getSession().bgTokenizer.lines[pos.row].tokens;
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
                select(token[t].value);
            }
        }
    });

    function title(data) {
        return data.split("\n")[0].split(" ")[1].split(".")[0];
    }

    function search(title) {
        return strip.tabGroup.children("#"+title);
    }

    function push(id) {
        window.history.pushState(id, null, window.location.pathname + "#" + id);
    }

    function append(id, content) {
        push(id);
        strip.append([{text : id}]);
        var tab = strip.tabGroup.children("li").last().attr({id : id});
        strip.select(tab);
        var editor = ace.edit(
            $(strip.contentElement(tab.index())).css({
                position : "absolute",
                top : 36, right : 0, bottom : 0, left : 0,
                padding : 0, overflow : "hidden"
            }).html("<div></div>").children().css({
                position : "absolute",
                top : 4, right : 4, bottom : 4, left : 4,
                overflow : "hidden"
            }).get(0)
        );
        editor.getSession().setMode(new AnimoMode());
        editor.getSession().setValue(content);
        editor.focus();
        var socket = new WebSocket(uri, "save");
        socket.onmessage = function (event) {
            var id = title(event.data);
            tab.attr({id : id});
            tab.children("span").text(id);
            editor.getSession().setValue(event.data);
        };
        socket.onopen = onopen;
        tab[0].socket = socket;
        tab[0].editor = editor;
    }

    $.fn.ideEditor = function() {
        strip = $(this).kendoTabStrip({
            animation : {
                open : {
                    effects: "none"
                }
            },
            select : function(tab) {
                push($(tab.item).attr("id"));
                tab.item.editor.focus();
            }
        }).data("kendoTabStrip");
        return strip;
    };

    $.fn.ideSearch = function () {
        sinput = $(this)
        sinput.keypress(function(event) {
            if (event.which == 13) {
                select($(event.target).val());
            } else if (event.which == 0) {
                strip.select()[0].editor.focus();
            }
        });
        return sinput;
    };

})(jQuery);
