(function($){

    $.fn.ide = function(){

        var self = $(this);

        var editor = ace.edit(self.get(0));

        return self;

    }

})(jQuery);
