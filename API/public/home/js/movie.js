angular.module('components', [])
  .directive('movie', function($timeout) {
      return {
          // Restrict to elements and attributes
          restrict: 'E',
          // Assign the angular scope attribute formatting
          scope: {
              id: '@',
              sid: '@'
          },
          // Assign the angular directive template HTML
          templateUrl: 'partials/videoTemplate.html',
          controller: function($scope, $http, $sce) {
              $http.get('/api/v1/movies/' +  $scope.id).success(function(data) {
                  $scope.movie = data.movie;
                  $scope.selectedSubtitle = 3;

                  $scope.playVideo($scope.sid);
              });

              $scope.playVideo = function(index) {
                  index = index - 1;
                  $scope.play_video = $scope.movie.videos[index];
                  $scope.play_video.enVtt = $scope.movie.videos[index].subtitleFolder + '/en.vtt';
                  $scope.play_video.enSrt = $scope.movie.videos[index].subtitleFolder + '/en.srt';
                  $scope.play_video.vnSrt = $scope.movie.videos[index].subtitleFolder + '/vn.srt';
                  $scope.play_video.url = $sce.trustAsResourceUrl($scope.play_video.url);

                  $scope.initSubtitle();
              }

              $scope.parseSrt = function(srcUrl, tScale, tOffset, tMargin, callback) {
                  $http.get(srcUrl).success(function(data) {
                      var lines = data.split(/\r?\n/);
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
                  });
              }

              $scope.initSubtitle = function() {
                  $scope.parseSrt($scope.play_video.enSrt, 1, 0, 0.1, function(enCues) {
                      $scope.parseSrt($scope.play_video.vnSrt, 1, 0, 0.1, function(viCues) {
                          var len = Math.max(enCues.length, viCues.length);
                          for (var index = 0; index < len; index++) {
                              var container = document.createElement("span");
                              container.setAttribute('id', 'cue' + enCues[index].id);
                              container.setAttribute('data-start', enCues[index].start);
                              container.innerHTML = "<span>" + enCues[index].text + "</span>" + (viCues ? "<br /><small>" + viCues[index].text + "</small>" : "");
                              jQuery('#subtitles-container').append(container);
                          }
                      });
                  });
              }
          },

          // Link the directive to enable our scope watch values
          link: function (scope, element, attrs) {
              console.log(element);
              var $video = element.find('video');
              $video[0].textTracks[0].mode = 'hidden';

              $video.bind('contextmenu',function() { return false; });

              $('#selectSubtitle').bind('change', function() {
                  var value = this.value;
                  if (value == 0) {
                      $('#videoSubtitle').hide();
                  } else {
                      $('#videoSubtitle').show();
                      $('#videoSubtitle').removeAttr('class');

                      if (value == 1) {
                          $('#videoSubtitle').addClass('en');
                      } else if (value == 2) {
                          $('#videoSubtitle').addClass('vn');
                      }
                  }
              });

              var track = element.find('track');
              track.bind('cuechange', function() {
                  var track = this.track;
                  if (track.activeCues[0]) {
                      var cueId = track.activeCues[0].id;

                      jQuery('#videoSubtitle').html($('#cue' + cueId).html())

                      jQuery('.active-cue').removeClass('active-cue');
                      jQuery('#cue' + cueId).addClass('active-cue');

                      var topPost = jQuery('#cue' + cueId)[0].offsetTop;
                      if (topPost > 200) {
                          jQuery('#subtitles-container').animate({
                              scrollTop: topPost - 200
                          }, 500);
                      }
                  }
              });

              setTimeout(function() {
                  playpause();
              }, 500);

              $video.on('click', function() { playpause(); } );
              $('.btnPlay').on('click', function() { playpause(); } );

              //before everything get started
              $video.on('loadedmetadata', function() {
                  //set video properties
                  jQuery('.current').text(timeFormat(0));
                  jQuery('.duration').text(timeFormat($video[0].duration));
                  updateVolume(0, 0.7, $video);

                  //start to get video buffering data
                  setTimeout(function() {
                      startBuffer();
                  }, 150);
              });

              //display current video play time
              $video.on('timeupdate', function() {
                  var currentPos = $video[0].currentTime;
                  var maxduration = $video[0].duration;
                  var perc = 100 * currentPos / maxduration;
                  $('.timeBar').css('width',perc+'%');
                  $('.current').text(timeFormat(currentPos));
              });

              //video ended event
              $video.on('ended', function() {
                  $('.btnPlay').removeClass('paused');
                  $video[0].pause();
              });

              //VIDEO PROGRESS BAR
              //when video timebar clicked
              var timeDrag = false;	/* check for drag event */
              $('.progress').on('mousedown', function(e) {
                  timeDrag = true;
                  updatebar(e.pageX, $video);
              });
              $(document).on('mouseup', function(e) {
                  if(timeDrag) {
                      timeDrag = false;
                      updatebar(e.pageX, $video);
                  }
              });
              $(document).on('mousemove', function(e) {
                  if(timeDrag) {
                      updatebar(e.pageX, $video);
                  }
              });

              //VOLUME BAR
              //volume bar event
              var volumeDrag = false;
              $('.volume').on('mousedown', function(e) {
                  volumeDrag = true;
                  $video[0].muted = false;
                  $('.sound').removeClass('muted');
                  updateVolume(e.pageX, false);
              });
              $(document).on('mouseup', function(e) {
                  if(volumeDrag) {
                      volumeDrag = false;
                      updateVolume(e.pageX, false);
                  }
              });
              $(document).on('mousemove', function(e) {
                  if(volumeDrag) {
                      updateVolume(e.pageX, false);
                  }
              });

              $('.btnFS').on('click', function() {
                  if($.isFunction($video[0].webkitEnterFullscreen)) {
                      $video[0].webkitEnterFullscreen();
                  }  else if ($.isFunction($video[0].mozRequestFullScreen)) {
                      $video[0].mozRequestFullScreen();
                  } else {
                      alert('Your browsers doesn\'t support fullscreen');
                  }
              });

              //sound button clicked
              $('.sound').click(function() {
                  $video[0].muted = !$video[0].muted;
                  $(this).toggleClass('muted');
                  if($video[0].muted) {
                      $('.volumeBar').css('width',0);
                  }
                  else{
                      $('.volumeBar').css('width', $video[0].volume*100+'%');
                  }
              });

              //display video buffering bar
              var startBuffer = function() {
                  var currentBuffer = $video[0].buffered.end(0);
                  var maxduration = $video[0].duration;
                  var perc = 100 * currentBuffer / maxduration;
                  $('.bufferBar').css('width',perc+'%');

                  if(currentBuffer < maxduration) {
                      setTimeout(function() {
                          startBuffer();
                      }, 500);
                  }
              };

              var playpause = function() {
                  if($video[0].paused || $video[0].ended) {
                      $('.btnPlay').addClass('paused');
                      $video[0].play();
                  }
                  else {
                      $('.btnPlay').removeClass('paused');
                      $video[0].pause();
                  }
              };

              var updateVolume = function(x, vol) {
                  var volume = $('.volume');
                  var percentage;
                  //if only volume have specificed
                  //then direct update volume
                  if (vol) {
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
                  $video[0].volume = percentage / 100;

                  //change sound icon based on volume
                  if($video[0].volume == 0){
                      $('.sound').removeClass('sound2').addClass('muted');
                  }
                  else if($video[0].volume > 0.5){
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

              var updatebar = function(x) {
                  var progress = $('.progress');

                  //calculate drag position
                  //and update video currenttime
                  //as well as progress bar
                  var maxduration = $video[0].duration;
                  var position = x - progress.offset().left;
                  var percentage = 100 * position / progress.width();
                  if(percentage > 100) {
                      percentage = 100;
                  }
                  if(percentage < 0) {
                      percentage = 0;
                  }
                  $('.timeBar').css('width',percentage+'%');
                  $video[0].currentTime = maxduration * percentage / 100;
              };
          }
      };
  });
