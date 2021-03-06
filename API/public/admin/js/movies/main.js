angular.module('wal.movies', [
    'wal.movies.list',
    'wal.movies.edit',
    'wal.movies.add',
    'ui.router'
]).config(function($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.when('/movies', '/movies/list');

    $stateProvider.state('movies', {
        url: '/movies',
        templateUrl: 'partials/main.html',
        controller: 'MainCtrl'
    });
});