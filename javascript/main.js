$(function() {
    $('.feature a.gallery').click(function(e) {
        e.preventDefault();
        var feature = $(this).parentsUntil(".feature").parent();
        var caption = feature.find("h3").text();
        var images = [];
        var items = feature.find("a.gallery");
        var index = 0;
        for (var i = 0, l = items.length; i < l; ++i) {
            var  item = $(items[i]);
            var label = item.attr('title') || item.attr('alt');
            images.push({
                'href': item.attr('href'),
                'title': label || caption
            });
            if (this === item[0]) {
                index = i;
            }
        }

        $.fancybox(images, {
            'cyclic': true,
            'padding': 0,
            'transitionIn': 'elastic',
            'transitionOut': 'elastic',
            'index': index
        });
    });
});
