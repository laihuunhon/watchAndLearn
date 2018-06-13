angular.module('wal.movies.addManual', [
    'walControllers',
    'ui.router'
]).config(function($stateProvider) {
    $stateProvider.state('movies.addManual', {
        url: '/addManual',
        controller: 'MovieAddManualCtrl',
        templateUrl: 'partials/movieAddManual.html'
    });
});