var mongoose = require('mongoose');

var movieSchema = new mongoose.Schema({
    title               : {
        type            : String,
        index           : "text"
    },
    description         : String,
    thumbnail           : String,
    total_watched       : {
        type            : Number,
        default         : 0,
        index           : true
    },
    total_liked         : {
        type            : Number,
        default         : 0,
        index           : true
    },
    total_comments      : {
        type            : Number,
        default         : 0,
        index           : true
    },
    series              : {
        type            : Boolean,
        default         : false,
        index           : true
    },
    isFinished          : {
        type            : Boolean,
        default         : true,
        index           : true
    },
    srcUrl              : String,
    videoCategory       : Array,

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

    // dummy fields
    videos              : []
});

movieSchema.index({
    created: 1,
    videoCategory: 1
});

module.exports = mongoose.model('Movies', movieSchema);
