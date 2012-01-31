(function($){

    $.fn.liveChange = function(callback) {
        return $(this).each(function(){
            var self = $(this);
            if (callback) {
                self.bind("liveChange", callback);
            }
            var value = self.val();
            setInterval(function(){
                var val = self.val();
                if (val != value) {
                    value = val;
                    setTimeout(function(){
                        var v = self.val();
                        if (v == val) {
                            self.trigger("liveChange");
                        }
                    }, 300);
                }
            }, 100);
        });
    };

})(jQuery);
