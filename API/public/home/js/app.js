var walApp = angular.module('walApp', [
    'walControllers',
    'ui.bootstrap',
    'walServices',
    'ui.router',
    "brantwills.paging",
    "components"
]);

//walApp.config(['$routeProvider',
//    function($routeProvider) {
//        $routeProvider.
//            when('/', {
//                templateUrl: 'partials/home.html',
//                controller: 'HomeCtrl'
//            }).
//            when('/movies/:movieId', {
//                templateUrl: 'partials/videoDetail.html',
//                controller: 'VideoDetailCtrl'
//            }).
//            otherwise({
//                redirectTo: '/'
//            });
//    }]);

walApp.config(function($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.deferIntercept();
    $urlRouterProvider.otherwise("/");

    $stateProvider
        .state("app", {
            url: "/",
            templateUrl: "partials/home.html",
            controller: 'HomeCtrl'
        })
        .state('movie', {
            url: '/movies/:id/:sid',
            templateUrl: 'partials/movieDetail.html',
            controller: 'MovieCtrl'
        });
});

walApp.run(function($rootScope, $urlRouter, AuthService, $state) {
    $rootScope.$on('$locationChangeSuccess', function(e) {
        if (AuthService.isAuthenticated()) return;

        e.preventDefault();

        $state.go('app');
    });

    $urlRouter.listen();
});