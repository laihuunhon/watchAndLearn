var mongoose = require('mongoose');

var likeSchema = new mongoose.Schema({
    video_id            : String,
    user_id             : String,

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

module.exports = mongoose.model('Likes', likeSchema);
