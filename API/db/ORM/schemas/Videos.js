var mongoose = require('mongoose');

var videoSchema = new mongoose.Schema({
    movieId             : {
        type            : String,
        index           : true
    },
    index               : Number,
    url                 : String,
    total_watched       : {
        type            : Number,
        default         : 0
    },
    total_liked         : {
        type            : Number,
        default         : 0
    },
    total_comments      : {
        type            : Number,
        default         : 0
    },
    altUrl              : String,

    created             : {
        type            : Date, 
        default         : Date.now,
        index           : true
    },
    updated             : {
        type            : Date, 
        default         : Date.now
    },
    deleted             : Date,
    // dummy field
    isLiked             : Boolean
});

module.exports = mongoose.model('Videos', videoSchema);
