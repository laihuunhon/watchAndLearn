// var mailRegExp = "^[-a-zA-Z0-9!#$%&'*+/=?^_`{|}~]+(\.[-a-zA-Z0-9!#$%&'*+/=?^_`{|}~]+)*@([a-zA-Z0-9]([-a-zA-Z0-9]{0,61}[a-zA-Z0-9])?\.)+([a-zA-Z0-9]{2,6})+$";
var mailRegExp = "^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/";

/**
 * Validate email
 */

this.email = function(value) {
	return /^[a-z0-9._-]{1,25}@[a-z0-9._-]{1,10}\.[a-z0-9_-]{1,10}([a-z]{2,4})?$/i.test(value) ? true : false;
};

/**
 * Validate nickname
 */

this.nickname = function(value) {
	return /^[a-z0-9_-]{1,100}$/i.test(value) ? true : false;
};

var isNumber = function(value) {
    return /^\d+$/.test(value) ? true : false;
}

var isValidDateFormat = function(value) {
    var IsoDateRe = new RegExp("^([0-9]{2})-([0-9]{2})-([0-9]{4})$");
    var matches = IsoDateRe.exec(value);
    if (!matches) return false;  

    var composedDate = new Date(matches[3], (matches[1] - 1), matches[2])
    // console.log(matches[2]);
    // console.log(matches[1]);
    // console.log(matches[3]);
    // console.log(composedDate);

    return ((composedDate.getMonth() == (matches[1] - 1)) &&
            (composedDate.getDate() == matches[2]) &&
            (composedDate.getFullYear() == matches[3]));
}

var isValidPhoneNumber = function(value) {
    return /^\([0-9]{3}\) [0-9]{3}-[0-9]{4}$/.test(value) ? true : false;
}

exports.input = function(req, method, next) {
	var routes = require('../config/routes.json');

	// Check Required fields
	var rules = routes[method].required;
	if (rules && rules.length){
        var result = false;
        var fields = [];
        for (var i=0; i<rules.length; i++) {
            var gres = true; 		// group result
            var gfields = [];  	    // group fields
            for (var j in rules[i]) {
                if (utils.checkProp(rules[i], j)) {
                    var param = rules[i][j];
                    var b = !!req.params[param];
                    if (!b) {
                        gfields.push(param);
                    }
                    gres &= b;
                }
            }
            if (gfields.length) {
                fields.push(gfields);
            }
            result |= gres;
            if (gres) {
                fields = rules[i];
                break;
            }
        }

        if (!result) {
            for (var i in fields) {
                if (utils.checkProp(fields, i)) {
                    fields[i] = '( '+fields[i].join(', ')+' )';
                }
            }   
            fields = fields.join(' or ');
            return next('ER00001', {'{fields}': fields});
        }
	}
	//Check matches
	rules = routes[method].matches;
	if (rules) {
 		result = true;
		for (var field in rules) {
            if (utils.checkProp(rules, field)) {
                if (!req.params[field]) {
                    continue;
                } else if (undefined == req.params[field]) {
                    continue;                    
                }
    			var rule = rules[field];
    			if (typeof rule == 'object' && rule.length > 1){
    				if (rule.indexOf(req.params[field]) == -1){
    					return next('ER00005', {"{field}": field});
    				}
    				continue;	
    			}
    			
    			if (rule == 'number'){
    				if (!isNumber(req.params[field])){
    					return next('ER00004', {"{field}": field});
    				}
    			}
    			
                if (rule == 'email') {
                    var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
                    if (!re.test(req.params[field])) {
                        return next('ER00004', { "{field}": field });
                    }
                } 
                
                if (rule == 'url') {
                	var re = /[\w\-_]+(\.[\w\-_]+)+([\w\-\.,@?^=%&amp;:/~\+#]*[\w\-\@?^=%&amp;/~\+#])?/;
                    if (!re.test(req.params[field])) {
                        return next('ER00007', { "{field}": field });
                    }
                }
                
                if (rule == 'date') {
                    if (!isValidDateFormat(req.params[field])) {
                        return next('ER00004', { "{field}": field });
                    }
                } else if (rule == 'phone') {
                    if (!isValidPhoneNumber(req.params[field])) {
                        return next('ER00004', { "{field}": field });   
                    }
                }

                if (rule == 'password') {
                    var re =  /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$/;
                    if (!re.test(req.params[field])) {
                        return next('ER00010', { "{field}": field });
                    }
                }
            }					
		}
	}
	next(null, req.params);
}