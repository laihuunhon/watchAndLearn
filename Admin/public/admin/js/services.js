var walServices = angular.module('walServices', []);

walServices.service('AuthService', function($http, apiUrl) {
    var LOCAL_TOKEN_KEY = 'userTokenKey';
    var isAuthenticated = false;

    var login = function(email, password, callback) {
        $http.post(apiUrl + '/api/v1/users/login', {
            email: email,
            password: password
        }).
            success(function(data) {
                storeUserCredentials(data.token);
                callback(null);
            }).
            error(function(error) {
                callback(error);
            });
    };

    var logout = function(callback) {
        $http.post(apiUrl + '/api/v1/users/logout').
            success(function() {
                destroyUserCredentials();
                callback(null);
            }).
            error(function(error) {
                callback(error);
            });
    }

    var getToken = function() {
        return window.localStorage.getItem(LOCAL_TOKEN_KEY);
    }

    function loadUserCredentials() {
        var token = window.localStorage.getItem(LOCAL_TOKEN_KEY);
        if (token) {
            useCredentials(token);
        }
    }

    function storeUserCredentials(token) {
        window.localStorage.setItem(LOCAL_TOKEN_KEY, token);
        useCredentials(token);
    }

    function useCredentials(token) {
        isAuthenticated = true;

        // Set the token as header for your requests!
        //$http.defaults.headers.common['Auth'] = token;
    }

    function destroyUserCredentials() {
        isAuthenticated = false;
        //$http.defaults.headers.common['Auth'] = undefined;
        window.localStorage.removeItem(LOCAL_TOKEN_KEY);
    }

    loadUserCredentials();

    return {
        login: login,
        logout: logout,
        getToken: getToken,
        isAuthenticated: function() {return isAuthenticated;}
    };
})