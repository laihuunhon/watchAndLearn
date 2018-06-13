var mongoose = require('mongoose');

var commentSchema = new mongoose.Schema({
    video_id            : String,
    user_id             : String,
    email               : String,
    text                : String,

    created             : {
        type            : Date, 
        default         : Date.now
    },
    updated             : {
        type            : Date, 
        default         : Date.now
    },
    deleted             : Date
});

module.exports = mongoose.model('Comments', commentSchema);
