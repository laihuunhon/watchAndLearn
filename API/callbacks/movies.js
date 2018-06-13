/**
 * Connecting modules
 */
var async		            = require('async');
var fs                  = require('fs');
var utils 	  	        = require('../libs/utils');
var MoviesSchema        = require('../db/ORM/schemas/Movies');
var VideosSchema        = require('../db/ORM/schemas/Videos');
var LikesSchema         = require('../db/ORM/schemas/Likes');
var CommonCallback      = require('./common.js');
var response            = require('../config/response.js');
var srt2vtt             = require('srt2vtt');
var rmdir               = require('rimraf');
var https               = require('https');
var curl = require('curlrequest');

var writeToFile = function(url, desPath, callback) {
    var enFile = fs.createWriteStream(desPath);
    console.log(url);
    if (url.indexOf('https') > -1) {
        https.get(encodeURI(url), function(response) {
            response.pipe(enFile);

            enFile.on('finish', function() {
                console.log()
                enFile.close(callback);
            });
        });
    } else {
        http.get(encodeURI(url), function(response) {
            response.pipe(enFile);

            enFile.on('finish', function() {
                enFile.close(callback);
            });
        });
    }
}

exports.add = function(req, res, next) {
    CommonCallback.actionDB(req, res, {
        schema: MoviesSchema,
        action: 'getOneItem',
        conditionFields: {
            title: req.params.title
        },
        returnFields: {
            _id: 1,
            title: 1
        }
    }, function(existedMovie) {
        if (existedMovie) {
            return response.fail(req, res, "EM00001");
        }

        var videoCategory = req.params.videoCategory ? req.params.videoCategory.split(',') : [];

        var newMovie = {
            title           : req.params.title,
            description     : req.params.description,
            thumbnail       : req.params.thumbnail,
            series          : req.params.videos.length != 1,
            isFinished      : req.params.isFinished,
            srcUrl          : req.params.srcUrl
        };
        if (videoCategory.length > 0) {
            newMovie.videoCategory = videoCategory;
        }

        CommonCallback.actionDB(req, res, {
            schema: new MoviesSchema(newMovie),
            action: 'create'
        }, function(movieCreated) {
            var baseFolder = './public/resources/' + movieCreated._id;
            fs.stat(baseFolder, function(err, stat) {
                if (err != null) {
                    fs.mkdirSync(baseFolder);
                }

                async.each(req.params.videos, function(video, next) {
                    var desFolder = baseFolder + '/' + video.index;
                    fs.stat(desFolder, function(err, stat) {
                        if (err != null) {
                            fs.mkdirSync(desFolder);
                        }

                        async.parallel([
                            function(callback) {
                                writeToFile(video.subtitles.english, desFolder + '/en.srt', function() {
                                    var srtData = fs.readFileSync(desFolder + '/en.srt');
                                    srt2vtt(srtData, function(err, vttData) {
                                        if (!err) {
                                            fs.writeFileSync(desFolder + '/en.vtt', vttData);
                                            fs.unlinkSync(desFolder + '/en.srt');
                                        }
                                        writeToFile(video.subtitles.vietnamese, desFolder + '/vn.srt', function() {
                                            var srtData = fs.readFileSync(desFolder + '/vn.srt');
                                            srt2vtt(srtData, function(err, vttData) {
                                                if (!err) {
                                                    fs.writeFileSync(desFolder + '/vn.vtt', vttData);
                                                    fs.unlinkSync(desFolder + '/vn.srt');
                                                }
                                                callback();
                                            });
                                        });
                                    });
                                });
                            }, function(callback) {
                                if (video.url.indexOf('http') == -1) {
                                    video.url = movieCreated.srcUrl.replace('?alt', '/photoid/' + video.url + '?alt');
                                }
                                var newVideo = {
                                    movieId         : movieCreated._id,
                                    index           : video.index,
                                    url             : video.url
                                };

                                CommonCallback.actionDB(req, res, {
                                    schema: new VideosSchema(newVideo),
                                    action: 'create'
                                }, function(videoCreated) {
                                    callback();
                                });
                            }
                        ], function() {
                            next(null, null);
                        });
                    });
                }, function() {
                    CommonCallback.actionDB(req, res, {
                        schema: movieCreated,
                        action: 'saveItem'
                    }, function(savedMovie) {
                        response.success(res, 'SM00004', null, {
                            movie: savedMovie
                        });
                    });
                });
            });
        });
    });
}

exports.deleteMovie = function(req, res, next) {
    async.parallel([
        function(callback) {
            CommonCallback.actionDB(req, res, {
                schema: MoviesSchema,
                action: 'removeItem',
                conditionFields: {
                    _id: req.params.movie_id
                }
            }, function() {
                callback();
            }, function() {
                callback();
            });
        },
        function(callback) {
            CommonCallback.actionDB(req, res, {
                schema: VideosSchema,
                action: 'removeItem',
                conditionFields: {
                    movieId: req.params.movie_id
                }
            }, function() {
                callback();
            }, function() {
                callback();
            });
        },
        function(callback) {
            rmdir('./public/resources/' + req.params.movie_id, function() {
                callback(null, null);
            });
        }
    ], function() {
        response.success(res, "SM00007");
    });
}

exports.list = function(req, res, next) {
    var movie_type = req.params.movie_type;
    var conditionFields = {};
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

    if (movie_type === 'single') {
        conditionFields.series = false;
    } else if (movie_type === 'series') {
        conditionFields.series = true;
    } else if (movie_type === 'new') {
        var date = new Date();
        date.setDate(date.getDate() - 30);
        conditionFields.created = {
            $gt: date
        }
    } else {
        conditionFields.videoCategory = {
            $in: movie_type.split(',')
        }
    }

    CommonCallback.actionDB(req, res, {
        schema: MoviesSchema,
        action: 'count',
        conditionFields: conditionFields
    }, function(total) {
        CommonCallback.actionDB(req, res, {
            schema         : MoviesSchema,
            action         : 'getItemsWithOptions',
            conditionFields: conditionFields,
            options        : options,
            returnFields: {
                _id: 1,
                title: 1,
                description: 1,
                thumbnail: 1,
                total_watched: 1,
                total_liked: 1,
                total_comments: 1
            }
        }, function(movieList) {
            response.success(res, 'SM00001', null, {
                movieList: movieList,
                total: total
            });
        });
    });
}

exports.listByAdmin = function(req, res, next) {
    var conditionFields = {
        deleted: {$exists: false}
    };
    if (req.params.title) {
        conditionFields.$text = {
            $search: req.params.title
        }
    }
    if (req.params.series != null) {
        conditionFields.series = req.params.series;
    }
    if (req.params.isFinished != null) {
        conditionFields.isFinished = req.params.isFinished;
    }
    console.log(conditionFields)
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
        schema: MoviesSchema,
        action: 'count',
        conditionFields: conditionFields
    }, function(total) {
        console.log(total);
        CommonCallback.actionDB(req, res, {
            schema: MoviesSchema,
            action: 'getItemsWithOptions',
            conditionFields: conditionFields,
            options: options
        }, function(movieList) {
            response.success(res, 'SM00001', null, {
                movieList: movieList,
                total: total
            });
        });
    });
}

exports.search = function(req, res, next) {
    var conditionFields = {
        $text: {
            $search: req.params.search_text
        }
    }

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
        schema: MoviesSchema,
        action: 'count',
        conditionFields: conditionFields
    }, function(total) {
        console.log(total);

        CommonCallback.actionDB(req, res, {
            schema: MoviesSchema,
            action: 'getItemsWithOptions',
            conditionFields: conditionFields,
            options: options,
            returnFields: {
                _id: 1,
                title: 1
            }
        }, function (movieList) {
            response.success(res, 'SM00001', null, {
                movieList: movieList,
                total: total
            });
        });
    });
}

exports.getMovieDetail = function(req, res, next) {
    var returnMovie = null;
    var returnVideos = [];
    var videoIndex = req.params.videoIndex ? req.params.videoIndex : 1;

    async.parallel([
        function(callback) {
            CommonCallback.actionDB(req, res, {
                schema: MoviesSchema,
                action: 'getOneItem',
                conditionFields: {
                    _id: req.params.movie_id
                },
                returnFields: {
                    _id: 1,
                    title: 1,
                    description: 1,
                    thumbnail: 1,
                    total_watched: 1,
                    total_liked: 1,
                    total_comments: 1,
                    isFinished: 1,
                    videoCategory: 1,
                    srcUrl: 1
                }
            }, function(movie) {
                returnMovie = movie;

                callback();
            });
        },
        function(callback) {
            CommonCallback.actionDB(req, res, {
                schema: VideosSchema,
                action: 'getItems',
                conditionFields: {
                    movieId: req.params.movie_id
                }
            }, function(videos) {
                async.each(videos, function(video, callback) {
                    if (video.url.indexOf('https://picasaweb.google.com') > -1) {
                        var nextStep = function() {
                            var httpsRequest = require('../libs/http').request('https');

                            var requestConfig = {
                                host : 'picasaweb.google.com',
                                port : 443,
                                path : video.url,
                                on   : {
                                    success : function(result) {
                                        result = JSON.parse(result);
                                        var feedVideoList = result.feed.media.content;
                                        async.each(feedVideoList, function(feedVideo, next) {
                                            if (feedVideo.width == 720) {
                                                if (feedVideo.url.indexOf('https://redirector') > -1) {
                                                    video.url = feedVideo.url;

                                                    CommonCallback.actionDB(req, res, {
                                                        schema: VideosSchema,
                                                        action: 'updateItem',
                                                        conditionFields: {
                                                            _id: video._id
                                                        },
                                                        updateFields: {
                                                            altUrl: video.url
                                                        }
                                                    }, function() {
                                                        console.log('store altUrl')
                                                        console.log(video.url);
                                                        next();
                                                    });
                                                } else {
                                                    video.url = feedVideo.url;
                                                    console.log(video.url);
                                                    next();
                                                }
                                            } else {
                                                next();
                                            }
                                        }, function() {
                                            callback();
                                        });
                                    },
                                    failure: function(error) {
                                        console.log(error);
                                        video.url = '';
                                        callback();
                                    }
                                }
                            };

                            httpsRequest.send('GET', requestConfig);
                        }

                        if (video.altUrl) {
                            var currentDate = new Date().getTime();
                            var expired = getMatched(video.altUrl, 'expire=', '&sparams') + '000';
                            if (expired > currentDate) {
                                console.log('expired > currentDate')
                                video.url = video.altUrl;
                                console.log(video.url)
                                callback();
                            } else {
                                nextStep();
                            }
                        } else {
                            nextStep();
                        }
                    } else {
                        callback();
                    }
                }, function() {
                    returnVideos = videos;
                    callback();
                });
            });
        }
    ], function() {
        if (!returnMovie) {
            return response.fail(req, res, "EM00002");
        }
        returnMovie.videos = returnVideos.sort(utils.dynamicSort('index'));

        var watchVideo = returnMovie.videos[videoIndex - 1];
        CommonCallback.actionDB(req, res, {
            schema: LikesSchema,
            action: 'getOneItem',
            conditionFields: {
                video_id: watchVideo._id,
                user_id: req.params.request_user._id
            }
        }, function(like) {
            returnMovie.videos[videoIndex - 1].isLiked = like ? true : false;

            response.success(res, 'SM00005', null, {
                movie: returnMovie
            });
        });
    });
}

exports.getStorageList = function(req, res, next) {
    fs.readdir('./storage', function(error, files) {
        if (error) {
            console.log(error);
            return response.fail(req, res, 'ER00012');
        } else {
            return response.success(res, 'SM00003', null, {
                fileList: files
            });
        }
    });
}

var moveFile = function(srcFilePath, desFolder, callback) {
    fs.exists(desFolder, function(exists) {
        if (!exists) {
            fs.mkdirSync(desFolder);
        }
        fs.rename(srcFilePath, desFolder + srcFilePath.replace('./storage', ''), function(error) {
            if (error) throw error;
            callback();
        });
    });
}

exports.addVideoToMovie = function(req, res, next) {
    CommonCallback.actionDB(req, res, {
        schema: MoviesSchema,
        action: 'getOneItem',
        conditionFields: {
            _id: req.params.movie_id
        }
    }, function(movie) {
        var baseFolder = './public/resources/' + req.params.movie_id;
        fs.exists(baseFolder, function(exists) {
            if (!exists) {
                fs.mkdirSync(baseFolder);
            }
            var desFolder = baseFolder + '/' + req.params.videoIndex;
            fs.exists(desFolder, function(exists) {
                if (!exists) {
                    fs.mkdirSync(desFolder);
                }

                async.parallel([
                    function(callback) {
                        if (req.params.enSubUrl) {
                            writeToFile(req.params.enSubUrl, desFolder + '/en.srt', function() {
                                var srtData = fs.readFileSync(desFolder + '/en.srt');
                                srt2vtt(srtData, function(err, vttData) {
                                    if (!err) {
                                        fs.writeFileSync(desFolder + '/en.vtt', vttData);
                                        fs.unlinkSync(desFolder + '/en.srt');
                                    }
                                    if (req.params.vnSubUrl) {
                                        writeToFile(req.params.vnSubUrl, desFolder + '/vn.srt', function () {
                                            var srtData = fs.readFileSync(desFolder + '/vn.srt');
                                            srt2vtt(srtData, function (err, vttData) {
                                                if (!err) {
                                                    fs.writeFileSync(desFolder + '/vn.vtt', vttData);
                                                    fs.unlinkSync(desFolder + '/vn.srt');
                                                }
                                                callback();
                                            });
                                        });
                                    } else {
                                        callback();
                                    }
                                });
                            });
                        } else {
                            callback();
                        }
                    },
                    function(callback) {
                        CommonCallback.actionDB(req, res, {
                            schema: VideosSchema,
                            action: 'getOneItem',
                            conditionFields: {
                                movieId: req.params.movie_id,
                                index: req.params.videoIndex
                            },
                            returnFields: {
                                _id: 1
                            }
                        }, function(existedVideo) {
                            if (existedVideo) {
                                if (req.params.videoUrl.indexOf('http') == -1) {
                                    existedVideo.url = movie.srcUrl.replace('?alt', '/photoid/' + req.params.videoUrl + '?alt');
                                } else {
                                    existedVideo.url = req.params.videoUrl;
                                }

                                CommonCallback.actionDB(req, res, {
                                    schema: existedVideo,
                                    action: 'saveItem'
                                }, function() {
                                    callback();
                                });
                            } else {
                                var newVideo = {
                                    movieId         : req.params.movie_id,
                                    index           : req.params.videoIndex,
                                    url             : req.params.videoUrl
                                };
                                if (req.params.videoUrl.indexOf('http') == -1) {
                                    newVideo.url = movie.srcUrl.replace('?alt', '/photoid/' + req.params.videoUrl + '?alt');
                                }

                                CommonCallback.actionDB(req, res, {
                                    schema: new VideosSchema(newVideo),
                                    action: 'create'
                                }, function() {
                                    callback();
                                });
                            }
                        });
                    }
                ], function() {
                    response.success(res, 'SM00004');
                });
            });
        });
    });




    //moveFile('./storage/' + req.params.videoFilename, './public/movies/' + req.params.movie_id, function() {
    //    moveFile('./storage/' + req.params.englishSubFilename, './public/movies/' + req.params.movie_id, function() {
    //        moveFile('./storage/' + req.params.vietnameseSubFilename, './public/movies/' + req.params.movie_id, function() {
    //            CommonCallback.actionDB(req, res, {
    //                schema: MoviesSchema,
    //                action: 'updateItem',
    //                conditionFields: {
    //                    _id: req.params.movie_id
    //                },
    //                updateFields: {
    //                    $push: {
    //                        videos: {
    //                            index           : req.params.index,
    //                            url             : '/' + req.params.movie_id + '/' + req.params.videoFilename,
    //                            subtitles       : {
    //                                english     : '/' + req.params.movie_id + '/' + req.params.englishSubFilename,
    //                                vietnamese  : '/' + req.params.movie_id + '/' + req.params.vietnameseSubFilename
    //                            }
    //                        }
    //                    }
    //                }
    //            }, function() {
    //                response.success(res, 'SM00004');
    //            });
    //        });
    //    });
    //});
}

exports.updateMovie = function(req, res, next) {
    var updateFields = {};
    if (req.params.title) {
        updateFields.title = req.params.title;
    }
    if (req.params.description) {
        updateFields.description = req.params.description;
    }
    if (req.params.thumbnail) {
        updateFields.thumbnail = req.params.thumbnail;
    }
    var videoCategory = req.params.videoCategory ? req.params.videoCategory.split(',') : [];
    if (videoCategory.length > 0) {
        updateFields.videoCategory = videoCategory;
    }
    if (req.params.isFinished != null) {
        updateFields.isFinished = req.params.isFinished;
    }

    CommonCallback.actionDB(req, res, {
        schema: MoviesSchema,
        action: 'updateItem',
        conditionFields: {
            _id: req.params.movie_id
        },
        updateFields: updateFields
    }, function() {
        return response.success(res, "SM00008");
    });
}

exports.addWatch = function(req, res, next) {
    async.parallel([
        function (callback) {
            CommonCallback.actionDB(req, res, {
                schema: VideosSchema,
                action: 'updateItem',
                conditionFields: {
                    _id: req.params.video_id
                },
                updateFields: {
                    $inc: {
                        total_watched: 1
                    }
                }
            }, function () {
                console.log('Increase Video Watch Counter success');
                callback();
            }, function (err) {
                console.log('Increase Video Watch Counter failed: ', err);
                callback();
            });
        },
        function (callback) {
            CommonCallback.actionDB(req, res, {
                schema: MoviesSchema,
                action: 'updateItem',
                conditionFields: {
                    _id: req.params.movie_id
                },
                updateFields: {
                    $inc: {
                        total_watched: 1
                    }
                }
            }, function () {
                console.log('Increase Movie Watch Counter success');
                callback();
            }, function (err) {
                console.log('Increase Movie Watch Counter failed: ', err);
                callback();
            });
        }
    ], function() {
        response.success(res, "SW00001");
    });
}

//var path = require("path");
//
//exports.getFile = function(req, res, next) {
//    res.writeHead(302, {
//        'Location': 'http://media.studyphim.vn/VIPN7/Anonymous.2011.720p.BluRay.x264.mp4'
//    });
//    res.end();


//    return res.redirectTo('http://media.studyphim.vn/VIPN7/Anonymous.2011.720p.BluRay.x264.mp4');
//
//    console.log(req.headers['referer']);
//    if (req.headers['referer'] != 'http://localhost:5555/') {
//        return sendResponse(res, 404);
//    }
//
//    var filename = './storage/' + req.params.filename;
//    var stat = fs.statSync(filename);
//    var rangeRequest = readRangeHeader(req.headers['range'], stat.size);
//    var responseHeaders = {};
//
//    if (rangeRequest == null) {
//        responseHeaders['Content-Type'] = getMimeNameFromExt(path.extname(filename));
//        responseHeaders['Content-Length'] = stat.size;  // File size.
//        responseHeaders['Accept-Ranges'] = 'bytes';
//
//        //  If not, will return file directly.
//        sendResponse(res, 200, responseHeaders, fs.createReadStream(filename));
//        return null;
//    }
//
//    var start = rangeRequest.Start;
//    var end = rangeRequest.End;
//
//    // If the range can't be fulfilled.
//    if (start >= stat.size || end >= stat.size) {
//        // Indicate the acceptable range.
//        responseHeaders['Content-Range'] = 'bytes */' + stat.size; // File size.
//
//        // Return the 416 'Requested Range Not Satisfiable'.
//        sendResponse(res, 416, responseHeaders, null);
//        return null;
//    }
//
//    // Indicate the current range.
//    responseHeaders['Content-Range'] = 'bytes ' + start + '-' + end + '/' + stat.size;
//    responseHeaders['Content-Length'] = start == end ? 0 : (end - start + 1);
//    responseHeaders['Content-Type'] = getMimeNameFromExt(path.extname(filename));
//    responseHeaders['Accept-Ranges'] = 'bytes';
//    responseHeaders['Cache-Control'] = 'no-cache';
//
//    // Return the 206 'Partial Content'.
//    sendResponse(res, 206,
//        responseHeaders, fs.createReadStream(filename, { start: start, end: end }));
//}


//function sendResponse(response, responseStatus, responseHeaders, readable) {
//    response.writeHead(responseStatus, responseHeaders);
//
//    if (readable == null)
//        response.end();
//    else
//        readable.on('open', function () {
//            readable.pipe(response);
//        });
//
//    return null;
//}
//
//function getMimeNameFromExt(ext) {
//    var mimeNames = {
//        '.css': 'text/css',
//        '.html': 'text/html',
//        '.js': 'application/javascript',
//        '.mp3': 'audio/mpeg',
//        '.mp4': 'video/mp4',
//        '.ogg': 'application/ogg',
//        '.ogv': 'video/ogg',
//        '.oga': 'audio/ogg',
//        '.txt': 'text/plain',
//        '.wav': 'audio/x-wav',
//        '.webm': 'video/webm'
//    };
//
//    var result = mimeNames[ext.toLowerCase()];
//
//    // It's better to give a default value.
//    if (result == null)
//        result = 'application/octet-stream';
//
//    return result;
//}
//
//function readRangeHeader(range, totalLength) {
//    /*
//     * Example of the method 'split' with regular expression.
//     *
//     * Input: bytes=100-200
//     * Output: [null, 100, 200, null]
//     *
//     * Input: bytes=-200
//     * Output: [null, null, 200, null]
//     */
//
//    if (range == null || range.length == 0)
//        return null;
//
//    var array = range.split(/bytes=([0-9]*)-([0-9]*)/);
//    var start = parseInt(array[1]);
//    var end = parseInt(array[2]);
//    var result = {
//        Start: isNaN(start) ? 0 : start,
//        End: isNaN(end) ? (totalLength - 1) : end
//    };
//
//    if (!isNaN(start) && isNaN(end)) {
//        result.Start = start;
//        result.End = totalLength - 1;
//    }
//
//    if (isNaN(start) && !isNaN(end)) {
//        result.Start = totalLength - end;
//        result.End = totalLength - 1;
//    }
//
//    return result;
//}
//
var httpRequest = require('../libs/http').request();

function getMatched(data, startString, endString) {
    var patt = new RegExp(startString + '(.*?)' + endString, 'i');
    var matched = data.match(patt);
    if (matched == null) {
        return '';
    } else {
        return matched[1];
    }
}

function getMatchedList(data, startString, endString) {
    var patt = new RegExp(startString + '(.*?)' + endString, 'ig');
    var matched = data.match(patt);
    if (matched == null) {
        return '';
    } else {
        return matched;
    }
}

var http = require('http');

var customRequestWithCookie = function(url, callback) {
    var requestConfig = {
        host : 'www.studyphim.vn',
        port : 80,
        path : url,
        on   : {
            success : function(result) {
                callback(null, result);
            },
            failure: function(error) {
                console.log(error);
                callback(true);
            }
        }
    };

    httpRequest.send('GET', requestConfig, {
        Cookie: studyCookie
    });
}

var getUrl = function(url, callback) {
    customRequestWithCookie(url, function(error, result) {
        if (error) {
            return callback(error);
        }
        //console.log(result);
        var videoUrl = getMatched(result, '<source type="video/mp4" src="', '" />');
        var matchList = getMatchedList(result, 'value="U2Fsd', '"');
        var enSubUrl = matchList[0].replace('value="', '').replace('"', '');
        var viSubUrl = matchList[1].replace('value="', '').replace('"', '');

        //var viSubUrl = getMatched(result, 'id="vn"', '/>');
        console.log(enSubUrl);
        //console.log(viSubUrl);

        callback(null, {
            url: videoUrl,
            subtitles: {
                english: enSubUrl,
                vietnamese: viSubUrl
            }
        });
    });
}

var getInfo = function(url, callback) {
    customRequestWithCookie(url, function(error, result) {
        if (error) {
            return callback(error);
        }
        result = result.replace(/[\r\n]/g, '');

        var title = getMatched(result, '<title>', '</title>');
        title = title.split(' | ')[0];

        var description = getMatched(result, '<h4>Giới thiệu phim:</h4>', '</p>');
        console.log(description);
        description = description.replace('Website <a href="http://www.studynhac.vn"> </a><strong><a href="http://www.studynhac.vn">Học tiếng anh qua bài hát</a> </strong> | <a href="http://www.studynhac.vn"> </a><strong><a href="http://www.studynhac.vn">Hoc tieng anh qua bai hat </a> </strong>', '');
        description = description.replace('<a href="http://www.studynhac.vn"> </a><strong><a href="http://www.studynhac.vn">Học tiếng anh qua bài hát</a> </strong> | <a href="http://www.studynhac.vn"> </a><strong><a href="http://www.studynhac.vn">Hoc tieng anh qua bai hat </a> </strong>', '');
        description = description.replace(/<p>/g, '');
        description = description.replace(/\t/g, '');
        description = description.replace(/<strong>/g, '');
        description = description.replace(/<\/strong>/g, '');
        description = description.replace('<a href=\"http://www.studyphim.vn\">', '');
        description = description.replace('<a href=\"http://www.studyphim.vn\">', '');
        description = description.replace(/<\/a>/g, '');


        var thumbnail = getMatched(result, '<meta property="og:image" content="', '" />');
        thumbnail = decodeURIComponent(thumbnail);
        thumbnail = encodeURI(thumbnail);
        //thumbnail = thumbnail.replace(/\+/g, '%20');

        callback(null, {
            title: title,
            description: description,
            thumbnail: thumbnail
        });
    });
}

exports.getUrlData = function(req, res, next) {
    var url = req.params.url;
    url = url.replace('http://www.studyphim.vn', '');

    var match = url.search('episode=');
    var episode = -1;
    var pattern = url;
    if (match > -1) {
        episode = parseInt(url.slice(match).replace('episode=', ''));
        pattern = url.slice(0, match - 1);
    }

    var urlList = [];
    if (episode == -1) {
        urlList.push({
            url: pattern + '/play?episode=1',
            index: 1
        });
    } else {
        for (var i=1; i<=episode; i++) {
            urlList.push({
                url: pattern + '/play?episode=' + i,
                index: i
            });
        }
    }

    getInfo(urlList[0].url, function(error, infoResult) {
        if (error) return response.fail(req, res, 'ER00013');

        var movie = infoResult;
        movie.videos = [];

        async.each(urlList, function(urlItem, next) {
            getUrl(urlItem.url, function(error, result) {
                if (error) return next(error);

                result.index = urlItem.index;
                movie.videos.push(result);

                next(null, null);
            });
        }, function(error, result) {
            if (error) return response.fail(req, res, 'ER00013');

            movie.videos = movie.videos.sort(utils.dynamicSort('index'));

            response.success(res, 'SM00006', null, {
                movie: movie
            });
        });
    });

    //var requestConfig = {
    //    host : 'www.studyphim.vn',
    //    port : 80,
    //    path : url,
    //    on   : {
    //        success : function(result) {
    //            console.log(result);
    //            var videoUrl = getMatched(result, '<source type="video/mp4" src="', '" />');
    //            var enSubUrl = getMatched(result, '<track id="en" kind="subtitles" src="', '" srclang="en"');
    //            var viSubUrl = getMatched(result, '<track id="vi" kind="subtitles" src="', '" srclang="vi"');
    //
    //            response.success(res, 'SM00006', null, {
    //                videoUrl: videoUrl
    //            });
    //
    //            //var writeToFile = function(url, path, callback) {
    //            //    var enFile = fs.createWriteStream(path);
    //            //    http.get(url, function(response) {
    //            //        response.pipe(enFile);
    //					//
    //            //        enFile.on('finish', function() {
    //            //            enFile.close(callback);
    //            //        });
    //            //    });
    //            //}
    //					//
    //            //writeToFile(enSubUrl, './public/test/en.srt', function() {
    //            //    writeToFile(viSubUrl, './public/test/vi.srt', function() {
    //            //        var srtData = fs.readFileSync("./public/test/en.srt");
    //					//
    //            //        srt2vtt(srtData, function(err, vttData) {
    //            //            if (err) throw new Error(err);
    //            //            fs.writeFileSync('./public/test/en.vtt', vttData);
    //					//
    //            //            response.success(res, 'SM00006', null, {
    //            //                videoUrl: videoUrl
    //            //            });
    //            //        });
    //            //    });
    //            //});
    //        },
    //        failure : function(error) {
    //            console.log(error);
    //        }
    //    }
    //};
    //
    //httpRequest.send('GET', requestConfig, {
    //    Cookie: studyCookie
    //});
}