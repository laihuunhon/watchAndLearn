var walApp = angular.module('walApp', [
    'walControllers',
    'ui.bootstrap',
    'walServices',
    'ui.router',
    "brantwills.paging",
    "wal.movies",
    "wal.users"
]);

walApp.constant("apiUrl", "http://128.199.164.220:5555");

walApp.config(function($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.deferIntercept();
    $urlRouterProvider.otherwise("/");

    $stateProvider
        .state("app", {
            url: "/",
            templateUrl: "partials/home.html",
            controller: 'HomeCtrl'
        })
});

walApp.run(function($rootScope, $urlRouter, AuthService, $state) {
    $rootScope.$on('$locationChangeSuccess', function(e) {
        if (AuthService.isAuthenticated()) return;

        e.preventDefault();

        $state.go('app');
    });

    $urlRouter.listen();
});