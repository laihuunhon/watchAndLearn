angular.module('wal.movies.editManual', [
    'walControllers',
    'ui.router'
]).config(function($stateProvider) {
    $stateProvider.state('movies.editManual', {
        url: '/editManual/:id',
        controller: 'MovieEditManualCtrl',
        templateUrl: 'partials/movieEditManual.html'
    });
});