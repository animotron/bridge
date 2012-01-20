(function($){

    var edit;
    var input;

    $.fn.ide = {

        editor : function () {
            var self = $(this);
            edit = ace.edit(self.get(0));
            return self;
        },

        input : function () {
            input = $(this);
            return input;
        }

    }


})(jQuery);
