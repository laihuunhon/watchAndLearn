angular.module('wal.users.edit', [
    'walControllers',
    'ui.router'
]).config(function($stateProvider) {
    $stateProvider.state('users.edit', {
        url: '/edit',
        controller: 'UserEditCtrl',
        templateUrl: 'partials/userEdit.html'
    });
});