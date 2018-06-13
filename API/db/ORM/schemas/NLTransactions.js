var mongoose = require('mongoose');

var transactionSchema = new mongoose.Schema({
    user_id             : String,
    type_card           : {
        type            : String,
        enum                : ["VMS", "VNP", "VIETTEL"]
    },
    pin_card            : String,
    card_serial         : String,
    error               : String,
    card_amount         : Number,
    nl_transaction_id   : String,
    type                : Array,

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

module.exports = mongoose.model('NLTransactions', transactionSchema);
