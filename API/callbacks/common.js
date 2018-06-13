/**
 * Connecting modules
 */
var fs            = require('fs');
var async         = require('async');
var ejs           = require('ejs');

var MongoService = require('../db/ORM/mongoDB.service');

var utils         = require('../libs/utils');
var response  = require('../config/response.js');
var mailer        = require('../libs/mailer');

var _ = require('underscore');

exports.redirectToIndexPage = function(req, res, next) {
    if (req.params.page) {
        res.writeHead(302, {
            'Location': '/resources/' + req.params.page
        });
    }
    res.end();
}

exports.generalPagingOption = function(req) {
    // build options (skip, limit, sort)
    var options = {
        sort : {
            created: -1
        }
    }
    if (parseInt(req.params.limit) > 0) {
        options.limit = req.params.limit;
    }
    if (parseInt(req.params.offset) >= 0) {
        options.skip = req.params.offset;
    }
    
    if (req.params.sort && req.params.order) {
        options.sort = {};
        if (req.params.sort == 'full_name') {
            options.sort['first_name'] = parseInt(req.params.order); 
            options.sort['last_name'] = parseInt(req.params.order); 
        } else {
            options.sort[req.params.sort] = parseInt(req.params.order);     
        }        
    }

    return options;
}

//exports.validateToken = function(req, res, callback) {
//    TokensService.getToken(TokensSchema, {
//        token : req.headers.authorization
//    }, null, function(result) {
//        if (result) {
//            if (result.error) {
//                response.fail(res, "ER00002");
//            } else {
//                var diff = utils.dateDiff(new Date(), result.last_access_date);
//                if (diff > config.token.expire) {
//                    response.fail(res, "ER00009");
//                } else {
//                    callback(result);
//                }
//            }
//        } else {
//            response.fail(res, "ER00003");
//        }
//    });
//}

exports.actionDB = function(req, res, params, success, error) {
    var errorCallback = function(resultError) {
        if (error) {
            return error(resultError);
        } else {
            return response.fail(req, res, "ER00002");
        }
    }

    if (params.action == 'create') {
        MongoService.create(params.schema, function(result) {
            if (result && result.error) return errorCallback(result.error);

            success(result);
        });
    } else if (params.action == 'getItems') {
        MongoService.getItems(params.schema, params.conditionFields, params.returnFields, function(result) {
            if (result && result.error) return errorCallback(result.error);

            success(result);
        });
    } else if (params.action == 'getItemsWithOptions') {
        MongoService.getItemsWithOptions(params.schema, params.conditionFields, params.options, params.returnFields, function(result) {
            if (result && result.error) return errorCallback(result.error);

            success(result);
        });
    } else if (params.action == 'getOneItem') {
        MongoService.getOneItem(params.schema, params.conditionFields, params.returnFields, function(result) {
            if (result && result.error) return errorCallback(result.error);

            success(result);
        });
    } else if (params.action == 'removeItem') {
        MongoService.removeItem(params.schema, params.conditionFields, function(result) {
            if (result && result.error) return errorCallback(result.error);

            success();
        });
    } else if (params.action == 'updateItem') {
        MongoService.updateItem(params.schema, params.conditionFields, params.updateFields, params.options, function(result) {
            if (result && result.error) return errorCallback(result.error);

            success();
        });
    } else if (params.action == 'updateAndReturnItem') {
        MongoService.updateAndReturnItem(params.schema, params.conditionFields, params.updateFields, params.options, function(result) {
            if (result && result.error) return errorCallback(result.error);

            success(result);
        });
    } else if (params.action == 'saveItem') {
        MongoService.saveItem(params.schema, function(result) {
            if (result && result.error) return errorCallback(result.error);

            success(result);
        });
    } else if (params.action == 'count') {
        MongoService.count(params.schema, params.conditionFields, function(result) {
            if (result && result.error) return errorCallback(result.error);

            success(result);
        });
    } else {
        return response.fail(req, res, "ER00002");
    }
}

exports.sendEmail = function(emailName, params, options, callback) {
    params.filename = __dirname + '/../templates/email/' + emailName + '.ejs';
    fs.readFile(__dirname + '/../templates/email/' + emailName + '.ejs', 'utf8', function(err, tpl) {
        var html  = ejs.render(tpl, params);

        mailer.send(html, options, function(error, result) {
            if (error) {
                console.log('Sent email fail: ' + options.subject);
                console.log(error);
            } else {
                console.log('Sent email success: ' + options.subject);
            }
            if (callback) {
                callback();
            }
        });
    });
}

