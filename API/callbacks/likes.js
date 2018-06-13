/**
 * Connecting modules
 */
var async		  = require('async');
var utils 	  	  = require('../libs/utils');
var LikesSchema     = require('../db/ORM/schemas/Likes');
var MoviesSchema        = require('../db/ORM/schemas/Movies');
var VideosSchema     = require('../db/ORM/schemas/Videos');
var CommonCallback      = require('./common.js');
var response  = require('../config/response.js');

exports.add = function(req, res, next) {
    var newLike = {
        video_id   : req.params.video_id,
        user_id    : req.params.request_user._id
    };

    CommonCallback.actionDB(req, res, {
        schema: new LikesSchema(newLike),
        action: 'create'
    }, function() {
        response.success(res, "SL00001");

        CommonCallback.actionDB(req, res, {
            schema: VideosSchema,
            action: 'updateItem',
            conditionFields: {
                _id: req.params.video_id
            },
            updateFields: {
                $inc: {
                    total_liked: 1
                }
            }
        }, function() {
            console.log('Increase Video Like Counter success');
        }, function(err) {
            console.log('Increase Video Like Counter failed: ', err);
        });

        CommonCallback.actionDB(req, res, {
            schema: MoviesSchema,
            action: 'updateItem',
            conditionFields: {
                _id: req.params.movie_id
            },
            updateFields: {
                $inc: {
                    total_liked: 1
                }
            }
        }, function() {
            console.log('Increase Movie Like Counter success');
        }, function(err) {
            console.log('Increase Movie Like Counter failed: ', err);
        });
    });
}