var mongoose = require('mongoose');

var userSchema = new mongoose.Schema({
    email               : String,
    hash_password       : String,
    role                : {
        type            : String,
        enum            : ['member', 'admin']
    },
    phone               : String,
    end_date            : {
        type            : Date
    },
    device_uuid         : {
        type            : String,
        index           : true
    },
    facebook_id         : String,
    has_ads             : {
        type            : Boolean,
        default         : true
    },

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

module.exports = mongoose.model('Users', userSchema);
