var walControllers = angular.module('walControllers', []);

walControllers.controller('HomeCtrl', function($scope, $http, $state, AuthService) {
    $scope.isLogin = AuthService.isAuthenticated();

    $scope.submit = function() {
        AuthService.login($scope.signinEmail, $scope.signinPassword, function(error) {
            if (!error) {
                $state.go('movies');
            } else {
                alert(error.message);
            }
        });
    };
});

walControllers.controller('MainCtrl', function($scope, $http, $state, AuthService) {
    $scope.isLogin = AuthService.isAuthenticated();
});

walControllers.controller('MovieListCtrl', function($scope, $modal, $http, AuthService) {
    $scope.isLogin = AuthService.isAuthenticated();
    $scope.pageSize = 20;
    $scope.filter = {};

    $scope.getMovie = function(page) {
        $scope.currentPage = page;
        var offset = (page-1)*$scope.pageSize;

        var params = "?offset=" + offset + "&limit=" + $scope.pageSize;
        if ($scope.filter.title) {
            params += "&title=" + $scope.filter.title
        }
        if ($scope.filter.series) {
            if ($scope.filter.series == 1) {
                params += "&series=" + false;
            } else if ($scope.filter.series == 2) {
                params += "&series=" + true;
            }
        }
        if ($scope.filter.isFinished) {
            if ($scope.filter.isFinished == 1) {
                params += "&isFinished=" + true;
            } else if ($scope.filter.isFinished == 2) {
                params += "&isFinished=" + false;
            }
        }

        $http.get('/api/v1/backend/movies' + params).success(function(data) {
            $scope.movieList = data.movieList;
            $scope.total = data.total;
        });
    };
    $scope.getMovie(1);

    $scope.remove = function(id) {
        $http.delete('/api/v1/movies/' + id).success(function() {
            $scope.getMovie($scope.currentPage);
        });
    }
});

walControllers.controller('MovieAddCtrl', function($scope, $modal, $http, AuthService, $stateParams) {
    $scope.study = {};

    $scope.getUrlData = function() {
        $http.get('/api/v1/url?url=' + $scope.study.url).success(function(data) {
            GibberishAES.size(256);
            for (var i=0; i<data.movie.videos.length; i++) {
                data.movie.videos[i].subtitles.english = 'http://www.studyphim.vn' + GibberishAES.dec(data.movie.videos[i].subtitles.english, "AM0tDdGMmqIHJHyBkS9dTwTJJjyPn3Dj").replace('srtvl', 'srt');
                data.movie.videos[i].subtitles.vietnamese = 'http://www.studyphim.vn' + GibberishAES.dec(data.movie.videos[i].subtitles.vietnamese, "AM0tDdGMmqIHJHyBkS9dTwTJJjyPn3Dj").replace('srtvl', 'srt');
            }
            data.movie.isFinished = true;
            $scope.movie = data.movie;
        }).error(function(error) {
            if (error.code == 'EM00001') {
                alert('Movie existed!');

                $scope.movie = {};
            }
        });
    }

    $scope.submit = function() {
        var category = [];
        for (var i in $scope.movie.category) {
            if ($scope.movie.category[i]) {
                category.push(i);
            }
        }
        if (category.length > 0) {
            category = category.join(',');
        } else {
            category = '';
        }

        $http.post('/api/v1/movies', {
            srcUrl: $scope.study.url.split('?')[0],
            title: $scope.movie.title,
            description: $scope.movie.description,
            thumbnail: $scope.movie.thumbnail,
            videos: $scope.movie.videos,
            videoCategory: category,
            isFinished: $scope.movie.isFinished
        }).
          success(function(data) {
              alert('Add movie success');
              $scope.movie = data.movie;
          }).
          error(function(error) {
              alert(error.message);
          });
    }
});

walControllers.controller('MovieEditCtrl', function($scope, $modal, $http, AuthService, $stateParams) {
    $scope.study = {};

    var getCurrentMovie = function() {
        $http.get('/api/v1/movies/' + $stateParams.id).success(function(data) {
            $scope.movie = data.movie;
            $scope.study.url = data.movie.srcUrl;
        });
    }

    getCurrentMovie();

    $scope.getUrlData = function() {
        $http.get('/api/v1/url?url=' + $scope.study.url).success(function(data) {
            GibberishAES.size(256);
            for (var i=0; i<data.movie.videos.length; i++) {
                data.movie.videos[i].subtitles.english = 'http://www.studyphim.vn' + GibberishAES.dec(data.movie.videos[i].subtitles.english, "AM0tDdGMmqIHJHyBkS9dTwTJJjyPn3Dj");
                data.movie.videos[i].subtitles.vietnamese = 'http://www.studyphim.vn' + GibberishAES.dec(data.movie.videos[i].subtitles.vietnamese, "AM0tDdGMmqIHJHyBkS9dTwTJJjyPn3Dj");
            }
            data.movie.isFinished = false;
            $scope.newMovie = data.movie;
        }).error(function(error) {
           if (error.code == 'EM00001') {
               alert('Movie existed!');

               $scope.newMovie = {};
           }
        });
    }

    $scope.updateMovie = function() {
        var category = [];
        for (var i in $scope.movie.category) {
            if ($scope.movie.category[i]) {
                category.push(i);
            }
        }
        if (category.length > 0) {
            category = category.join(',');
        } else {
            category = '';
        }

        $http.put('/api/v1/movies/' + $stateParams.id, {
            title: $scope.movie.title,
            description: $scope.movie.description,
            thumbnail: $scope.movie.thumbnail,
            videoCategory: category,
            isFinished: $scope.movie.isFinished
        }).
          success(function(data) {
              alert('Update movie success');
          }).
          error(function(error) {
              alert(error.message);
          });
    }

    $scope.updateVideo = function(videoIndex) {
        var video = $scope.newMovie.videos[videoIndex-1];
        $http.post('/api/v1/movies/addVideo', {
            movie_id: $scope.movie._id,
            videoIndex: videoIndex,
            videoUrl: video.url,
            enSubUrl: video.subtitles.english,
            vnSubUrl: video.subtitles.vietnamese
        }).
          success(function(data) {
              getCurrentMovie();
          }).
          error(function(error) {
              alert(error.message);
          });
    }

    $scope.submit = function() {
        var category = [];
        for (var i in $scope.movie.category) {
            if ($scope.movie.category[i]) {
                category.push(i);
            }
        }
        if (category.length > 0) {
            category = category.join(',');
        } else {
            category = '';
        }

        $http.post('/api/v1/movies', {
            srcUrl: $scope.study.url.split('?')[0],
            title: $scope.movie.title,
            description: $scope.movie.description,
            thumbnail: $scope.movie.thumbnail,
            videos: $scope.movie.videos,
            videoCategory: category,
            isFinished: $scope.movie.isFinished
        }).
          success(function(data) {
              alert('Add movie success');
              $scope.movie = data.movie;
          }).
          error(function(error) {
              alert(error.message);
          });
    }
});

walControllers.controller('UserListCtrl', function($scope, $modal, $window, AuthService) {
    $scope.isLogin = AuthService.isAuthenticated();
});

walControllers.controller('UserEditCtrl', function($scope, $modal, $window, AuthService) {
    $scope.isLogin = AuthService.isAuthenticated();
});