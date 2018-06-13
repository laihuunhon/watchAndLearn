// server creating
var restify   = require('restify');
var response  = require('./config/response.js'); 
var mongoose  = require('mongoose');
var utils     = require('./libs/utils.js');

exports.start = function() {
    var server = restify.createServer({ 
        name: 'WatchAndLearn API project'
    });
    server.acceptable.push('text/html');
    server.use(restify.acceptParser(server.acceptable));
    server.use(restify.dateParser());
    server.use(restify.queryParser());
    server.use(restify.bodyParser());
    server.use(function crossOrigin(req,res,next) {
        res.header("Access-Control-Allow-Origin", "*");
        res.header("Access-Control-Allow-Headers", "'Accept', 'Accept-Version', 'Content-Type', 'Api-Version', 'Origin', 'X-Requested-With', 'authorization', 'Range'");
        return next();
    });

    server.get(/\/resources\/?.*/, restify.serveStatic({
        directory: __dirname + '/public'
    }));

    // database init
    mongoose.connect('mongodb://' + config.db.user + ':' + config.db.password + '@' + config.db.host + ':' + config.db.port + '/' + config.db.name);

    // callbacks classes
    var common = require('./callbacks/common');
    var users = require('./callbacks/users');
    var comments = require('./callbacks/comments');
    var movies = require('./callbacks/movies');
    var likes = require('./callbacks/likes');
    var transactions = require('./callbacks/transactions');

    // routing
    var routes = require('./config/routes.json');
    var route;

    var addRoute = function(apiMethod, route) {

        var requestsCounter = 0; 
        // callback binding

        server[route.method](route.url, function(req, res, next) {
            if (config.debug) {
                console.log(new Date().toTimeString(), route.url, req.params);
            }

            var versionValidate = function(next) {
                delete require.cache[require.resolve('./public/config/config.json')];
                var code = require('./public/config/config.json');
                if (req.params.device == 'A') {
                    console.log(code[GLOBAL.env].androidVersion)
                    if (req.params.versionCode < code[GLOBAL.env].androidVersion) {
                        return response.fail(req, res, "ER00014");
                    } else {
                        next();
                    }
                } else {
                    next();
                }
            }
			
            var inputValidate = function(next) {
                require('./libs/validate').input(req, apiMethod, function(err, data) {
                    if (err) return response.fail(req, res, err, null, data);
                    return next();
                });
            }

            var authValidate = function(next) {
                if (!route.auth) return next();
            
                var token = req.headers.authorization;
                if (!token) return response.fail(req, res, "ER00003");
                
                users.validateToken(req, res, function(tokenResult) {
                    users.getUserByOptions(req, res, {
                        _id: tokenResult.user_id
                    }, function(userResult) {
                        req.params.request_user = userResult;

                        if (!route.role) return next();

                        if (route.role.indexOf(userResult.role) == -1) {
                            return response.fail(req, res, "ER00008");
                        }
                        return next();
                    });
                });
            }

            var callbackRun = function() {
                eval(route.callback) (req, res, next);
            }

            versionValidate(function() {
                inputValidate(function() {
                    authValidate(callbackRun);
                });
            });
        });
    }
    
    for (var apiMethod in routes) {
        if (utils.checkProp(routes, apiMethod)) {
            route = routes[apiMethod];
            addRoute(apiMethod, route);
        }
    }

    server.listen(config.server.port, function() {
        console.log('%s listening at %s', server.name, server.url);
        console.log(config.env + ' environment applied\n');
    });

    var f404 = function(req, res, next) {
        if (req.method.toLowerCase() === 'options') {
            var allowHeaders = ['Accept', 'Accept-Version', 'Content-Type', 'Api-Version', 'Origin', 'X-Requested-With', 'authorization', 'Range'];

            if (res.methods && res.methods.indexOf('OPTIONS') === -1) {
                res.methods.push('OPTIONS');
                res.header('Access-Control-Allow-Methods', res.methods.join(', '));
            } else {
                res.header('Access-Control-Allow-Methods', 'OPTIONS, PUT');
            }

            res.header('Access-Control-Allow-Credentials', true);
            res.header('Access-Control-Allow-Headers', allowHeaders.join(', '));
            
            res.header('Access-Control-Allow-Origin', req.headers.origin);

            return res.send(204);
        } else {
            response.fail(req, res, 'E000501');
        }

    }

    server.on('NotFound', f404).on('MethodNotAllowed', f404);

    process.on('uncaughtException', function (err) {
       console.log('Caught exception: ' + err);
       console.log(err.stack);
    });
}

