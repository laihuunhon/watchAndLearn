angular.module('wal.movies.add', [
    'walControllers',
    'ui.router'
]).config(function($stateProvider) {
    $stateProvider.state('movies.add', {
        url: '/add',
        controller: 'MovieAddCtrl',
        templateUrl: 'partials/movieAdd.html'
    });
});