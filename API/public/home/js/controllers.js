var walControllers = angular.module('walControllers', []);

walControllers.controller('HomeCtrl', function($scope, $http, AuthService) {
    $scope.pageSize = 12;
    $scope.currentPageType = 'newest';

    $scope.getMovie = function(page) {
        $scope.currentPage = page;
        var offset = (page-1)*$scope.pageSize;

        $http.get('/api/v1/movies?movie_type=' + $scope.currentPageType + '&offset=' + offset + '&limit=' + $scope.pageSize).success(function(data) {
            $scope[$scope.currentPageType] = data.movieList;
            $scope.total = parseInt(data.total);
        });
        console.log($scope);
    };

    $scope.setPaging = function(page) {
        $scope.currentPage = 1;
        $scope.currentPageType = page;

        $scope.getMovie(1);
    }
});

walControllers.controller('MovieCtrl', function($scope, $http, $stateParams, $sce) {
    $scope.sid = $stateParams.sid;
    $scope.id = $stateParams.id;
});

walControllers.controller('HeaderCtrl', function($scope, $modal, $window, AuthService) {
    $scope.isLogin = AuthService.isAuthenticated();

    $scope.openSigninForm = function() {
        $modal.open({
            templateUrl: 'modal-signin.html',
            controller : 'SigninCtrl'
        });
    }
    $scope.logout = function() {
        AuthService.logout(function(error) {
            if (!error) {
                $window.location.reload();
            } else {
                alert(error.message);
            }
        });
    }
});

walControllers.controller('SigninCtrl', function($scope, $modalInstance, $http, $window, AuthService) {
    $scope.submit = function() {
        AuthService.login($scope.signinEmail, $scope.signinPassword, function(error) {
            if (!error) {
                $window.location.reload();
            } else {
                alert(error.message);
            }
        });
    };
    $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
    };
});
