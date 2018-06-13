/**
 * Connecting modules
 */
var async		  = require('async');
var utils 	  	  = require('../libs/utils');
var TransactionsSchema     = require('../db/ORM/schemas/Transactions');
var NLTransactionsSchema     = require('../db/ORM/schemas/NLTransactions');
var UsersSchema     = require('../db/ORM/schemas/Users');
var CommonCallback      = require('./common.js');
var response  = require('../config/response.js');
var restler = require('restler');
var crypto    = require('crypto');
var request = require('request');
var curl = require('curlrequest');

exports.add = function(req, res, next) {
    if (config.cardService === 'nganluong') {
        var newNLTransaction = {
            user_id      : req.params.request_user._id,
            type_card    : req.params.card_id,
            pin_card     : req.params.pin_field,
            card_serial  : req.params.seri_field
        };

        CommonCallback.actionDB(req, res, {
            schema: new NLTransactionsSchema(newNLTransaction),
            action: 'create'
        }, function(createdTransaction) {
            var params = {
                func: config.nganluong.func,
                version: config.nganluong.version,
                merchant_id: config.nganluong.merchant_id,
                merchant_account: config.nganluong.merchant_account,
                merchant_password: exports.md5(config.nganluong.merchant_id + '|' + config.nganluong.merchant_password),
                pin_card: createdTransaction.pin_card,
                card_serial: createdTransaction.card_serial,
                type_card: createdTransaction.type_card,
                ref_code: createdTransaction.user_id,
                client_fullname: req.params.request_user.email,
                client_email: req.params.request_user.email,
                client_mobile: req.params.request_user.phone
            }
            console.log('params:', params)

            restler.post(config.nganluong.serviceUrl, {
                data: params
            }).on("complete", function(result, responseRest) {
                var successCallback = function(arrResult) {
                    var newEndDate = req.params.request_user.end_date;
                    var currentDate = new Date();

                    if (newEndDate < currentDate) {
                        newEndDate = currentDate;
                    }
                    newEndDate = newEndDate.setDate(newEndDate.getDate() + parseInt(arrResult[10]/1000));

                    async.parallel([
                        function(callback) {
                            CommonCallback.actionDB(req, res, {
                                schema: UsersSchema,
                                action: 'updateAndReturnItem',
                                conditionFields: {
                                    _id: createdTransaction.user_id
                                },
                                updateFields: {
                                    end_date: newEndDate
                                },
                                options: {
                                    'new': true
                                }
                            }, function(updatedUser) {
                                console.log(updatedUser);
                                newEndDate = updatedUser.end_date;
                                callback(null, null);
                            }, function() {
                                callback(null, null);
                            });
                        },
                        function(callback) {
                            CommonCallback.actionDB(req, res, {
                                schema: NLTransactionsSchema,
                                action: 'updateItem',
                                conditionFields: {
                                    _id: createdTransaction._id
                                },
                                updateFields: {
                                    status: "success",
                                    card_amount: arrResult[10],
                                    nl_transaction_id: arrResult[12]
                                }
                            }, function() {
                                callback(null, null);
                            }, function() {
                                callback(null, null);
                            });
                        }
                    ], function() {
                        return response.success(res, 'ST00001', null, {
                            end_date: newEndDate
                        });
                    });
                }
                var errorCallback = function(errMessage) {
                    errMessage = 'ET0000' + errMessage;

                    CommonCallback.actionDB(req, res, {
                        schema: NLTransactionsSchema,
                        action: 'updateItem',
                        conditionFields: {
                            _id: createdTransaction._id
                        },
                        updateFields: {
                            status: "failed",
                            error: errMessage
                        }
                    }, function() {
                        return response.fail(req, res, errMessage);
                    }, function(err) {
                        response.fail(req, res, errMessage);
                    });
                }

                if (responseRest && responseRest.statusCode == 200) {
                    console.log('-------RESULT ------')
                    console.log(result);
                    result = result.split('|');
                    var errorCode = result[0];
                    if (errorCode === '00') {
                        successCallback(result);
                    } else {
                        errorCallback(errorCode);
                    }
                } else {
                    errorCallback('98');
                }
            });
        });
    } else {
        var newTransaction = {
            user_id    : req.params.request_user._id,
            card_id    : req.params.card_id,
            pin_field  : req.params.pin_field,
            seri_field : req.params.seri_field
        };

        CommonCallback.actionDB(req, res, {
            schema: new TransactionsSchema(newTransaction),
            action: 'create'
        }, function(createdTransaction) {
            var url = "https://www.baokim.vn/the-cao/restFul/send";
            var query = {
                algo_mode: 'hmac',
                api_password: config.baokim.api_password,
                api_username: config.baokim.api_username,
                card_id: createdTransaction.card_id,
                merchant_id: config.baokim.merchant_id,
                pin_field: createdTransaction.pin_field,
                seri_field: createdTransaction.seri_field,
                transaction_id: createdTransaction._id
            }
            var queryString = "";
            for (var i in query) {
                queryString += query[i];
            }
            console.log(queryString);
            query.data_sign = exports.hashData(queryString);
            console.log(query);

            curl.request({
                url: url,
                data: query,
                include: true
            }, function(err, parts) {
                parts = parts.split('\r\n');
                var data = parts.pop(),
                  head = parts;
                data = JSON.parse(data);
                console.log('----Header--')
                console.log(head);
                console.log('----stdout--')
                console.log(data);

                response.success(res, 'SC00001', null, {
                    test: data
                })
            });
        });
    }


        //Baokim = rest.service(function(u, p) {
        //    this.defaults.username = u;
        //    this.defaults.password = p;
        //}, {
        //    baseURL: 'http://twitter.com'
        //}, {
        //    update: function(message) {
        //        return this.post('/statuses/update.json', { data: { status: message } });
        //    }
        //});




        //request.post({
        //    url: url,
        //    form: query
        //}, function(err, httpResponse, body) {
        //    console.log('----ERROR--')
        //    console.log(err);
        //    console.log('----httpResponse--')
        //    console.log(httpResponse);
        //    console.log('----body--')
        //    console.log(body);
        //});





    //    var digest = require('http-digest-client')('merchant_19196', '19196pTorPsV9Wk4618A9eStiw63GCEmZ4j', true);
    //    digest.request({
    //        host: 'www.baokim.vn',
    //        path: '/the-cao/restFul/send',
    //        method: 'POST',
    //        headers: { "User-Agent": "Test" } // Set any headers you want
    //    }, function(res) {
    //        res.on('data', function (data) {
    //            console.log(data.toString());
    //        });
    //        res.on('error', function (err) {
    //            console.log('oh noes');
    //        });
    //    });
}

exports.hashData = function(text) {
    var hash, hmac;
    hmac = crypto.createHmac('sha1', config.baokim.site_password);
    // change to 'binary' if you want a binary digest
    hmac.setEncoding('hex');
    // write in the text that you want the hmac digest for
    hmac.write(text);
    // you can't read from the stream until you call end()
    hmac.end();
    // read out hmac digest
    hash = hmac.read();

    return hash;
}

exports.md5 = function(str) {
    return crypto.createHash('md5').update(str).digest('hex');
}