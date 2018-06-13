//INITIALIZE
function initVideo() {
    var video = $('#video');

    setTimeout(function() {
        playpause(video);
    }, 500);

    //remove default control when JS loaded
    video[0].removeAttribute("controls");

    //before everything get started
    video.on('loadedmetadata', function() {
        //set video properties
        $('.current').text(timeFormat(0));
        $('.duration').text(timeFormat(video[0].duration));
        updateVolume(0, 0.7, video);

        //start to get video buffering data
        setTimeout(function() {
            startBuffer(video);
        }, 150);
    });

    //display current video play time
    video.on('timeupdate', function() {
        var currentPos = video[0].currentTime;
        var maxduration = video[0].duration;
        var perc = 100 * currentPos / maxduration;
        $('.timeBar').css('width',perc+'%');
        $('.current').text(timeFormat(currentPos));
    });

    //CONTROLS EVENTS
    //video screen and play button clicked
    video.on('click', function() { playpause(video); } );
    $('.btnPlay').on('click', function() { playpause(video); } );

    //fullscreen button clicked
    $('.btnFS').on('click', function() {
        if($.isFunction(video[0].webkitEnterFullscreen)) {
            video[0].webkitEnterFullscreen();
        }
        else if ($.isFunction(video[0].mozRequestFullScreen)) {
            video[0].mozRequestFullScreen();
        }
        else {
            alert('Your browsers doesn\'t support fullscreen');
        }
    });

    //sound button clicked
    $('.sound').click(function() {
        video[0].muted = !video[0].muted;
        $(this).toggleClass('muted');
        if(video[0].muted) {
            $('.volumeBar').css('width',0);
        }
        else{
            $('.volumeBar').css('width', video[0].volume*100+'%');
        }
    });

    //VIDEO EVENTS

    //video ended event
    video.on('ended', function() {
        $('.btnPlay').removeClass('paused');
        video[0].pause();
    });

    //VIDEO PROGRESS BAR
    //when video timebar clicked
    var timeDrag = false;	/* check for drag event */
    $('.progress').on('mousedown', function(e) {
        timeDrag = true;
        updatebar(e.pageX, video);
    });
    $(document).on('mouseup', function(e) {
        if(timeDrag) {
            timeDrag = false;
            updatebar(e.pageX, video);
        }
    });
    $(document).on('mousemove', function(e) {
        if(timeDrag) {
            updatebar(e.pageX, video);
        }
    });

    //VOLUME BAR
    //volume bar event
    var volumeDrag = false;
    $('.volume').on('mousedown', function(e) {
        volumeDrag = true;
        video[0].muted = false;
        $('.sound').removeClass('muted');
        updateVolume(e.pageX, false, video);
    });
    $(document).on('mouseup', function(e) {
        if(volumeDrag) {
            volumeDrag = false;
            updateVolume(e.pageX, false, video);
        }
    });
    $(document).on('mousemove', function(e) {
        if(volumeDrag) {
            updateVolume(e.pageX, false, video);
        }
    });
}
	
//display video buffering bar
var startBuffer = function(video) {
    var currentBuffer = video[0].buffered.end(0);
    var maxduration = video[0].duration;
    var perc = 100 * currentBuffer / maxduration;
    $('.bufferBar').css('width',perc+'%');

    if(currentBuffer < maxduration) {
        setTimeout(function() {
            startBuffer(video);
        }, 500);
    }
};

var playpause = function(video) {
    if(video[0].paused || video[0].ended) {
        $('.btnPlay').addClass('paused');
        video[0].play();
    }
    else {
        $('.btnPlay').removeClass('paused');
        video[0].pause();
    }
};

var updateVolume = function(x, vol, video) {
    var volume = $('.volume');
    var percentage;
    //if only volume have specificed
    //then direct update volume
    if(vol) {
        percentage = vol * 100;
    }
    else {
        var position = x - volume.offset().left;
        percentage = 100 * position / volume.width();
    }

    if(percentage > 100) {
        percentage = 100;
    }
    if(percentage < 0) {
        percentage = 0;
    }

    //update volume bar and video volume
    $('.volumeBar').css('width',percentage+'%');
    video[0].volume = percentage / 100;

    //change sound icon based on volume
    if(video[0].volume == 0){
        $('.sound').removeClass('sound2').addClass('muted');
    }
    else if(video[0].volume > 0.5){
        $('.sound').removeClass('muted').addClass('sound2');
    }
    else{
        $('.sound').removeClass('muted').removeClass('sound2');
    }
};

//Time format converter - 00:00
var timeFormat = function(seconds){
    var m = Math.floor(seconds/60)<10 ? "0"+Math.floor(seconds/60) : Math.floor(seconds/60);
    var s = Math.floor(seconds-(m*60))<10 ? "0"+Math.floor(seconds-(m*60)) : Math.floor(seconds-(m*60));
    return m+":"+s;
};

var updatebar = function(x, video) {
    var progress = $('.progress');

    //calculate drag position
    //and update video currenttime
    //as well as progress bar
    var maxduration = video[0].duration;
    var position = x - progress.offset().left;
    var percentage = 100 * position / progress.width();
    if(percentage > 100) {
        percentage = 100;
    }
    if(percentage < 0) {
        percentage = 0;
    }
    $('.timeBar').css('width',percentage+'%');
    video[0].currentTime = maxduration * percentage / 100;
};