var mongoose = require('mongoose');

var tokenSchema = new mongoose.Schema({
    token               : String,
    user_id             : String,
    last_access_date    : {
        type            : Date, 
        default         : Date.now
    }
});

module.exports = mongoose.model('Tokens', tokenSchema);