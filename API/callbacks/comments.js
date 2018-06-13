/**
 * Connecting modules
 */
var async		  = require('async');
var utils 	  	  = require('../libs/utils');
var CommentsSchema     = require('../db/ORM/schemas/Comments');
var MoviesSchema        = require('../db/ORM/schemas/Movies');
var VideosSchema     = require('../db/ORM/schemas/Videos');
var CommonCallback      = require('./common.js');
var response  = require('../config/response.js');

exports.add = function(req, res, next) {
    var newComment = {
        video_id   : req.params.video_id,
        user_id    : req.params.request_user._id,
        email      : req.params.request_user.email,
        text       : req.params.text
    };

    CommonCallback.actionDB(req, res, {
        schema: new CommentsSchema(newComment),
        action: 'create'
    }, function(commentCreated) {
        response.success(res, "SC00002", null, {
            comment: commentCreated
        });

        CommonCallback.actionDB(req, res, {
            schema: VideosSchema,
            action: 'updateItem',
            conditionFields: {
                _id: req.params.video_id
            },
            updateFields: {
                $inc: {
                    total_comments: 1
                }
            }
        }, function() {
            console.log('Increase Video Comment Counter success');
        }, function(err) {
            console.log('Increase Video Comment Counter failed: ', err);
        });

        CommonCallback.actionDB(req, res, {
            schema: MoviesSchema,
            action: 'updateItem',
            conditionFields: {
                _id: req.params.movie_id
            },
            updateFields: {
                $inc: {
                    total_comments: 1
                }
            }
        }, function() {
            console.log('Increase Movie Comment Counter success');
        }, function(err) {
            console.log('Increase Movie Comment Counter failed: ', err);
        });
    });
}

exports.list = function(req, res, next) {
    var conditionFields = {
        video_id: req.params.video_id
    };
    var limit = 20;
    if (req.params.limit) {
        if (parseInt(req.params.limit) < limit) {
            limit = parseInt(req.params.limit);
        }
    }
    var options = {
        limit: limit,
        skip: req.params.offset ? req.params.offset : 0,
        sort: {
            created: -1
        }
    };

    CommonCallback.actionDB(req, res, {
        schema: CommentsSchema,
        action: 'count',
        conditionFields: conditionFields
    }, function(total) {
        CommonCallback.actionDB(req, res, {
            schema: CommentsSchema,
            action: 'getItemsWithOptions',
            conditionFields: conditionFields,
            options: options
        }, function (commentList) {
            response.success(res, 'SC00001', null, {
                commentList: commentList,
                total: total
            });
        });
    });
}