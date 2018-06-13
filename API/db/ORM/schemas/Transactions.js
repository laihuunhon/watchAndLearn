var mongoose = require('mongoose');

var transactionSchema = new mongoose.Schema({
    user_id             : String,
    card_id             : {
        type            : String,
        enum            : ["VINA", "MOBI", "VIETEL"]
    },
    type_card           : {
        type            : String,
        enum                : ["VMS", "VNP", "VIETTEL"]
    },
    pin_field           : String,
    seri_field          : String,
    status              : {
        type            : String,
        enum            : ["pending", "success", "failed"],
        default         : "pending"
    },

    created             : {
        type            : Date, 
        default         : Date.now
    }
});

module.exports = mongoose.model('Transactions', transactionSchema);
