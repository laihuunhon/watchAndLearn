angular.module('wal.movies.edit', [
    'walControllers',
    'ui.router'
]).config(function($stateProvider) {
    $stateProvider.state('movies.edit', {
        url: '/edit/:id',
        controller: 'MovieEditCtrl',
        templateUrl: 'partials/movieEdit.html'
    });
});