
var codes = require('./codes');

module.exports = {
    
    /**
     * Success Server answer
     */
    
    success: function(res, code, params, data) {
        var resultMessage = codes.success[code];
        
        if (typeof resultMessage === 'undefined') return res.send(404, {success: 0, code: code, message: 'Unknown error code'});
        
        var result = {
            success: 1,
            code: code,
            message: resultMessage
        };
        if (data != 'undefined') {
            for (var attr in data) { 
                result[attr] = data[attr]; 
            }
        }
        if (typeof params === 'object') for (key in params) result[key] = params[key];
              
        res.send( 200,  result);
    },

    /**
     * Fail Server answer
     */
    
    fail: function(req, res, code, httpCode, data) {
        var result = codes.error[code];
        if (req.params && req.params.locale == 'vi_VN') {
            result = codes.error_vn[code];
        }
        
        if ( typeof result === 'undefined' ) return res.send(404, {success: 0, code:code, message:'Unknown error code'});
    
        result = {
            success: 0,
            code: code,
            message: result
        }
        if (data != 'undefined') {
            for (var attr in data) {
                result[attr] = data[attr];
            }
        }

        res.send( httpCode ? httpCode : 500,  result);
    }
};