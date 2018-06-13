angular.module('wal.movies.list', [
    'walControllers',
    'ui.router'
]).config(function($stateProvider) {
    $stateProvider.state('movies.list', {
        url: '/list',
        controller: 'MovieListCtrl',
        templateUrl: 'partials/movieList.html'
    });
});