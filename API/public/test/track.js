function parseSrt(srcUrl, tScale, tOffset, tMargin, callback) {
    $.ajax({
        type: 'GET',
        url: srcUrl,
        success: function(response, textStatus, data) {
            var srtText = data.responseText;
            var tScale = tScale || 1;
            var tOffset = tOffset || 0;

            var lines = srtText.split(/\r?\n/);
            var captions = [];
            var timerex = /^(\d\d|\d):(\d\d):(\d\d),(\d{3}) --\> (\d\d|\d):(\d\d):(\d\d),(\d{3})/;
            var lineslen = lines.length;
            var i = 0;
            var t;
            console.log(lineslen);
            while (i < lineslen) {
                t = timerex.exec(lines[i]);
                if (t) {
                    var tStart = 3600 * t[1] + 60 * t[2] + 1 * t[3] + parseFloat('.' + t[4]);
                    var tStop = 3600 * t[5] + 60 * t[6] + 1 * t[7] + parseFloat('.' + t[8]);
                    if (i + 1 < lineslen) {

                        var j = i + 1;
                        var text = "";

                        while(!timerex.exec(lines[j]) && lines[j]) {
                            text = text + lines[j];
                            text = text + '\n';
                            j++;
                        }

                        captions.push({
                            'start'	: tStart,
                            'id'	: parseInt(lines[i - 1]),
                            'text'	: text
                        });
                    }
                }
                i++;
            }
            callback(captions);
        }
    });
}

function initTrack() {
    var enSrc = $('#en').attr('src');
    var viSrc = $('#vi').attr('src');
    parseSrt(enSrc, 1, 0, 0.1, function(enCues) {
        parseSrt(viSrc, 1, 0, 0.1, function(viCues) {
            var len = Math.max(enCues.length, viCues ? viCues.length : 0);
            for (var index = 0; index < len; index++) {
                var container = document.createElement("span");
                container.setAttribute('id', 'cue' + enCues[index].id);
                container.setAttribute('data-start', enCues[index].start);
                container.innerHTML = "<span>" + enCues[index].text + "</span>" + (viCues ? "<br /><small>" + viCues[index].text + "</small>" : "");
                $('#subtitles-container').append(container);
            }
        });
    });

    var highlightSubtitle = function() {
        console.log('testttt ')
        var track = $(this).prop('track');

        track.oncuechange = function() {
            console.log('cue change');
            if (track.activeCues[0]) {
                var cueId = track.activeCues[0].id;
                console.log(cueId);
//                $(display).find('#cue'+cueId).each(function() {
//                    $(display).find('.active-cue').removeClass('active-cue');
//                    $(this).addClass('active-cue');
//                    $(this).prev().nextAll(':lt(2)').scrollIntoView();
//                });
            }
        };
    };

    $('#video')[0].textTracks[0].mode = 'hidden';


    $('#enVtt').bind('cuechange', function() {
        var track = $(this)[0].track;
        if (track.activeCues[0]) {
            var cueId = track.activeCues[0].id;

            $('#videoSubtitle').html($('#cue' + cueId).html())

            $('.active-cue').removeClass('active-cue');
            $('#cue' + cueId).addClass('active-cue');

            var topPost = $('#cue' + cueId)[0].offsetTop;
            console.log(topPost);
            if (topPost > 200) {
                $('#subtitles-container').animate({
                    scrollTop: topPost - 200
                }, 500);
            }
        }
    });
}
