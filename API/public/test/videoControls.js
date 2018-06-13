$(document).ready(function() {
    // Video
    var video = $("#video")[0];
    // Buttons
    var playButton = $("#play-pause");
    var muteButton = $("#mute");
    var fullScreenButton = $("#full-screen");
    // Sliders
    var seekBar = $("#seek-bar");
    var volumeBar = $("#volume-bar");
    // Event listener for the play/pause button
    playButton.bind("click", function() {
        if (video.paused == true) {
            // Play the video
            video.play();
            // Update the button text to 'Pause'
            playButton.html("Pause");
        } else {
            // Pause the video
            video.pause();
            // Update the button text to 'Play'
            playButton.html("Play");
        }
    });
    // Event listener for the mute button
    muteButton.bind("click", function() {
        if (video.muted == false) {
            // Mute the video
            video.muted = true;
            // Update the button text
            muteButton.html("Unmute");
        } else {
            // Unmute the video
            video.muted = false;
            // Update the button text
            muteButton.html("Mute");
        }
    });
    // Event listener for the full-screen button
    fullScreenButton.bind("click", function() {
        if (video.requestFullscreen) {
            video.requestFullscreen();
        } else if (video.mozRequestFullScreen) {
            video.mozRequestFullScreen(); // Firefox
        } else if (video.webkitRequestFullscreen) {
            video.webkitRequestFullscreen(); // Chrome and Safari
        }
    });
    // Event listener for the seek bar
    seekBar.bind("change", function() {
        // Update the video time
        video.currentTime = video.duration * (seekBar.val() / 100);
    });
    // Update the seek bar as the video plays
    $('#video').bind("timeupdate", function() {
        seekBar.val((100 / video.duration) * video.currentTime);
    });
    // Pause the video when the seek handle is being dragged
    seekBar.bind("mousedown", function() {
        video.pause();
    });
    // Play the video when the seek handle is dropped
    seekBar.bind("mouseup", function() {
        video.play();
    });
    // Event listener for the volume bar
    volumeBar.bind("change", function() {
        // Update the video volume
        video.volume = volumeBar.val();
    });
});
