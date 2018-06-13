angular.module('wal.users', [
    'wal.users.list',
    'wal.users.edit',
    'ui.router'
]).config(function($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.when('/users', '/users/list');

    $stateProvider.state('users', {
        url: '/users',
        templateUrl: 'partials/main.html',
        controller: 'MainCtrl'
    });
});