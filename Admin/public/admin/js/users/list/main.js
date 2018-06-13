angular.module('wal.users.list', [
    'walControllers',
    'ui.router'
]).config(function($stateProvider) {
    $stateProvider.state('users.list', {
        url: '/list',
        controller: 'UserListCtrl',
        templateUrl: 'partials/userList.html'
    });
});