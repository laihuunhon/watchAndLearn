/**
 * Connecting modules
 */
var fs            = require('fs');
var async		  = require('async');
var facebook      = require('fb');
facebook.options({
    appId: config.facebook.appId,
    appSecret: config.facebook.appSecret
});

var utils 	  	  = require('../libs/utils');
var validate      = require('../libs/validate');
var bcrypt        = require('bcryptjs');

var UsersSchema     = require('../db/ORM/schemas/Users');
var TokensSchema    = require('../db/ORM/schemas/Tokens');

var CommonCallback      = require('./common.js');

var response  = require('../config/response.js');

exports.isExistedUser = function(req, res, callback) {
    CommonCallback.actionDB(req, res, {
        schema: UsersSchema,
        action: 'getOneItem',
        conditionFields: {
            email: req.params.email,
            deleted: {$exists: false}
        }
    }, function(user) {
        if (!user) return callback();

        return response.fail(req, res, 'EU00008');
    });
}

exports.isExistedDeviceUUID = function(req, res, callback) {
    CommonCallback.actionDB(req, res, {
        schema: UsersSchema,
        action: 'getOneItem',
        conditionFields: {
            device_uuid: req.params.device_uuid,
            deleted: {$exists: false}
        }
    }, function(user) {
        if (!user) return callback();

        return response.fail(req, res, 'EU00012');
    });
}

exports.createNewToken = function(req, res, user_id, callback) {
    CommonCallback.actionDB(req, res, {
        schema: TokensSchema,
        action: 'removeItem',
        conditionFields: {
            user_id: user_id
        }
    }, function() {
        var newToken = new TokensSchema({
            token: utils.uid(64),
            user_id: user_id
        });

        CommonCallback.actionDB(req, res, {
            schema: new TokensSchema(newToken),
            action: 'create'
        }, function (tokenCreated) {
            callback(tokenCreated);
        });
    });
}

/**
 * verify email
 * url: POST /api/v1/users/verifyEmail
 */

exports.verifyEmail = function(req, res, next) {
    exports.isExistedUser(req, res, function() {
        return response.success(res, 'SU00012');
    });
}

/**
 * User registration method
 * url: POST /v1/users
 */

exports.registration = function(req, res, next) {
    exports.isExistedUser(req, res, function() {
        exports.isExistedDeviceUUID(req, res, function() {
            bcrypt.hash(req.params.password, config.bcrypt.pass_salt_len, function(error, hash_password) {
                if (error || !hash_password) return response.fail(req, res, 'EU00011');

                var end_date = new Date();
                var newUser = {
                    hash_password   : hash_password,
                    role            : 'member',
                    email           : req.params.email,
                    phone           : req.params.phone,
                    end_date        : end_date,
                    device_uuid     : req.params.device_uuid
                };

                CommonCallback.actionDB(req, res, {
                    schema: new UsersSchema(newUser),
                    action: 'create'
                }, function(userCreated) {
                    exports.createNewToken(req, res, userCreated._id, function(token) {
                        response.success(res, "SU00001", null, {
                            user: userCreated,
                            token: token.token
                        });
                    });
                });
            });
        });
    });
}

/**
 * User login method
 * url: POST /v1/users/login
 */

exports.login = function(req, res, next) {
    CommonCallback.actionDB(req, res, {
        schema: UsersSchema,
        action: 'getOneItem',
        conditionFields: {
            email: req.params.email,
            deleted: {$exists: false}
        }
    }, function(user) {
        if (!user) return response.fail(req, res, "EU00002");

        bcrypt.compare(req.params.password, user.hash_password, function(error, isMatch) {
            if (!isMatch) {
                response.fail(req, res, "EU00002");
            } else {
                exports.createNewToken(req, res, user._id, function(token) {
                    user.has_ads = user.end_date < new Date();

                    response.success(res, 'SU00005', null , {
                        user: user,
                        token: token.token
                    });
                });
            }
        });
    });
}

/**
 * User logout method
 * url: GET /v1/users/logout
 */

exports.logout = function(req, res, next) {
    CommonCallback.actionDB(req, res, {
        schema: TokensSchema,
        action: 'removeItem',
        conditionFields: {
            token: req.headers.authorization
        }
    }, function() {
        response.success(res, 'SU00002');
    });
}

exports.validateToken = function(req, res, callback) {
    CommonCallback.actionDB(req, res, {
        schema: TokensSchema,
        action: 'getOneItem',
        conditionFields: {
            token: req.headers.authorization
        }
    }, function(token) {
        if (!token) return response.fail(req, res, "ER00003");

        var diff = utils.dateDiff(new Date(), token.last_access_date);
        if (diff > config.token.expire) {
            return response.fail(req, res, "ER00009");
        } else {
            return callback(token);
        }
    });
}

exports.getUserByOptions = function(req, res, params, callback) {
    CommonCallback.actionDB(req, res, {
        schema: UsersSchema,
        action: 'getOneItem',
        conditionFields: params
    }, function(user) {
        if (!user) return response.fail(req, res, "EU00002");

        callback(user);
    });
}

exports.getUserProfile = function(req, res, callback) {
    if (req.params.user_id === 'me') {
        req.params.user_id = req.params.request_user._id;
    }
    exports.getUserByOptions(req, res, {
        _id: req.params.user_id,
        deleted: {$exists: false}
    }, function(user) {
        user.has_ads = user.end_date < new Date();

        response.success(res, 'SU00005', null, {
            user: user
        });
    });
}

exports.changePassword = function(req, res, callback) {
    CommonCallback.actionDB(req, res, {
        schema: UsersSchema,
        action: 'getOneItem',
        conditionFields: {
            _id: req.params.request_user._id,
            deleted: {$exists: false}
        }
    }, function(user) {
        if (!user) return response.fail(req, res, "EU00002");

        bcrypt.compare(req.params.currentPassword, user.hash_password, function(error, isMatch) {
            if (!isMatch) {
                response.fail(req, res, "EU00013");
            } else {
                bcrypt.hash(req.params.newPassword, config.bcrypt.pass_salt_len, function(error, hash_password) {
                    if (error || !hash_password) return response.fail(req, res, 'EU00011');

                    user.hash_password = hash_password;

                    CommonCallback.actionDB(req, res, {
                        schema: user,
                        action: 'saveItem'
                    }, function() {
                        response.success(res, 'SU00006');
                    });
                });
            }
        });
    });
}

exports.list = function(req, res, next) {
    CommonCallback.actionDB(req, res, {
        schema: UsersSchema,
        action: 'getItems'
    }, function(userList) {
        response.success(res, 'SU00008', null, {
            userList: userList
        });
    });
}

exports.resetpassword = function(req, res, next) {
    CommonCallback.actionDB(req, res, {
        schema: UsersSchema,
        action: 'getOneItem',
        conditionFields: {
            email: req.params.email,
            phone: req.params.phone,
            deleted: {$exists: false}
        }
    }, function(user) {
        if (!user) return response.fail(req, res, 'EU00002');

        exports.sendResetPasswordEmail(user.email, function(newPassword) {
            bcrypt.hash(newPassword, config.bcrypt.pass_salt_len, function(error, hash_password) {
                user.hash_password = hash_password;

                CommonCallback.actionDB(req, res, {
                    schema: user,
                    action: 'saveItem'
                }, function() {
                    response.success(res, 'SU00003');
                });
            });
        });
    });
}

exports.sendResetPasswordEmail = function(email, callback) {
    var newPassword  = utils.uid(10);

    CommonCallback.sendEmail('resetPassword', {
        user_name: email,
        new_password: newPassword
    }, {
        "to"        : email,
        "subject"   : "Password Reset"
    }, function() {
        callback(newPassword);
    });
}

exports.checkEndDate = function(req, res, next) {
    var currentDate = new Date();
    return response.success(res, 'SU00015', null, {
        isExpired: req.params.request_user.end_date < currentDate
    });
}

exports.support = function(req, res, next) {
    console.log(req.params.request_user);
    CommonCallback.sendEmail('contactUs', {
        email: req.params.request_user.email,
        support_type: req.params.support_type,
        support_detail: req.params.support_detail
    }, {
        "to"        : config.mail.support.email,
        "subject"   : "Need Support From " + req.params.request_user.email
    }, function() {
        return response.success(res, 'SU00016');
    });
}

exports.loginWithFacebook = function (req, res, next) {
    var existedUserCallback = function(user) {
        exports.createNewToken(req, res, user._id, function(token) {
            response.success(res, 'SU00005', null , {
                user: user,
                token: token.token
            });
        });
    }

    var notExistedUserCallback = function(fbUserInfo) {
        exports.isExistedDeviceUUID(req, res, function() {
            var end_date = new Date();
            var newUser = {
                role: 'member',
                email: fbUserInfo.email ? fbUserInfo.email : 'fb_' + new Date().getTime() + '@fb.com',
                end_date: end_date,
                device_uuid: req.params.device_uuid,
                facebook_id: fbUserInfo.id
            };

            CommonCallback.actionDB(req, res, {
                schema: new UsersSchema(newUser),
                action: 'create'
            }, function (userCreated) {
                exports.createNewToken(req, res, userCreated._id, function(token) {
                    response.success(res, "SU00001", null, {
                        user: userCreated,
                        token: token.token
                    });
                });
            });
        });
    }

    facebook.setAccessToken(req.params.access_token);

    facebook.api('/me', {
        fields: 'email'
    }, function(fbUserInfo) {
        if (fbUserInfo.error || !fbUserInfo) return response.fail(req, res, 'EU00010');

        CommonCallback.actionDB(req, res, {
            schema: UsersSchema,
            action: 'getOneItem',
            conditionFields: {
                'facebook_id': fbUserInfo.id
            }
        }, function(user) {
            if (!user) {
                CommonCallback.actionDB(req, res, {
                    schema: UsersSchema,
                    action: 'getOneItem',
                    conditionFields: {
                        'email': fbUserInfo.email
                    }
                }, function(user) {
                    if (!user) {
                        notExistedUserCallback(fbUserInfo);
                    } else {
                        return response.fail(req, res, 'EU00008');
                    }
                });
            } else {
                existedUserCallback(user);
            }
        });
    });
}