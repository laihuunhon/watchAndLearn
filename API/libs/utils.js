var moment = require('moment');
var fs     = require('fs');

/**
 * Return a unique identifier with the given `len`.
 *
 *     utils.uid(10);
 *     // => "FDaS435D2z"
 *
 * @param {Number} len
 * @return {String}
 * @api private
 */
exports.uid = function(len) {
  var buf = []
    , chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
    , charlen = chars.length;

  for (var i = 0; i < len; ++i) {
    buf.push(chars[getRandomInt(0, charlen - 1)]);
  }

  return buf.join('');
};

/**
 * Return a unique numeric identifier with the given `len`.
 *
 *     utils.uid(6);
 *     // => "752136"
 *
 * @param {Number} len
 * @return {String}
 * @api private
 */
exports.numid = function(len) {
  var buf = []
    , chars = '0123456789'
    , charlen = chars.length;

  for (var i = 0; i < len; ++i) {
	buf.push(chars[getRandomInt(0, charlen - 1)]);
  }

  return buf.join('');
};

/**
 * Return a random int, used by `utils.uid()`
 *
 * @param {Number} min
 * @param {Number} max
 * @return {Number}
 * @api private
 */

function getRandomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

/**
 * Strip whitespace or tabs from the beginning and end of a string
 *
 * @param {String} value
 * @return {String}
 */

exports.trim = function(value) {
  return value.replace(/^\s+|\s+$/g, '');
};

/**
 * Make a string first character uppercase
 *
 * @param {String} value
 * @return {String}
 */

exports.ucfirst = function(value) {
	value = String(value);
	var str = value.charAt(0).toUpperCase();
	return str + value.substr(1);
};

/**
 * Determine whether a string is not empty
 *
 * @param {String} value
 * @return {Boolean}
 */

exports.notEmpty = function(value) {
	return value !== undefined && value !== "" && value !== "0" ? true : false;
};

/**
 * Check availability of required parameters and correctness of their values
 *
 * @param {Object} params
 * @param {Array} required
 * @return {Boolean}
 */

exports.checkRequired = function(params, required) {
	var result = true;
	required.forEach(function(value) {
		if(!(value in params) || params[value] === undefined || params[value] == "") result = false;
	});
	return result;
};

/**
 * Function for verify email. Now it working with only GMail
 *
 * @param {String} email
 * @param {function} callback function 
 */

exports.verifyEmail = function(email, callback) {
  	var cmds = [
      "HELO 127.0.0.1\n",
      "MAIL FROM:<test@test.com>\n",
      "RCPT TO:<"+email+">\n"
    ];

  	var net = require('net');
  	var check = function() {
	    var i = 0;
	    var c = net.createConnection(25, 'gmail-smtp-in.l.google.com', function() {
	      c.addListener('data', rcv);
	    });
	    
	    var rcv = function(msg) { 
	      	if (i>=0 && i < cmds.length){
	        	c.write(cmds[i]); i++;
	      	} else {
	        	callback(!/does not exist/.test(msg.toString()), email);
	        	i = -1;
	      	}
	    }
  	}
  	check();
};

exports.validateEmail = function (email, callback) {
   
   var querystring = require('querystring');
   var http = require('http');

    var data = querystring.stringify({
      "EmailAddress": email,
      "APIKey": config.emailvalidate.APIKey
    });

    var options = {
      host: config.emailvalidate.host,
      port: '80',
      path: config.emailvalidate.path,
      method: 'POST',
      headers: {
          'Content-Length': data.length,
          'Content-Type': "application/x-www-form-urlencoded"
      }
    };

    var req = http.request(options, function(res) {
        res.setEncoding('utf8');
        res.on('data', function (chunk) {
            var data = JSON.parse(chunk);

            callback( (data.status == 200 || data.status == 207), email);
        });
    });

    req.write(data);
    req.end();
}

/**
 * Function to get current data in format yyyy-mm-dd hh:mm:ss
 *
 */

exports.createdTime = function() {

    var timestamp = Math.round(new Date().getTime() / 1000);
    var date = new Date(timestamp*1000); 

    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    var day = date.getDate();
    var hours = date.getHours();
    var minutes = date.getMinutes();
    var seconds = date.getSeconds();

    if(day<10){day='0'+day}
    if(month<10){month='0'+month}

    var created_time = year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;

	  return created_time;
};


/**
 * Merge two objects/arrays Values of first rewrited by values from second if exists
 */
exports.merge = function(o1, o2) {
  if(!o1) o1 = {};
  for (var p in o2)if(utils.checkProp(o2, p)) {
    try {
      if ( o2[p].constructor==Object )
        o1[p] = this.merge(o1[p], o2[p]);
      else if( o2[p].constructor==Array )
        for(var i=0; i<o2[p].length; i++) 
          o1[p][i] = this.merge(o1[p][i], o2[p][i]);
      else o1[p] = o2[p];
    } catch(e) {
      o1[p] = o2[p];
    }
  }
  return o1;
};

exports.checkProp = function(obj, key){
    return obj.hasOwnProperty(key) && typeof(obj[key]) !== 'function';
  }

exports.sendStubData = function (url, res) {
    
    var client   = require('knox').createClient( config.knox );
    client.get(url).on('response', function(r){
      r.setEncoding('utf8');
      r.on('data', function(chunk) {
        (r.statusCode == 200 ) ? res.send(302, JSON.parse(chunk) ) : res.send(404, error['not_found']);
      });
    }).end();

}
	
/**
 * Function to get file extension 
 *
 * @param {String} value
 */
exports.getFileExtension = function(filename){
    return filename.split('.').pop();
}

/**
 * Function check file format
 *
 * @param {String} filename
 */
exports.checkPictureExtension = function(filename){       
	   var profile_picture_ext = utils.getFileExtension(filename);	
	   if(config.allowed_pictures_format.indexOf(profile_picture_ext)=== -1){
			return false;
	   }else{
			return true;
	   }
}
/**
 * Function to stripslashes
 *
 * @param {String} str
 */
exports.stripslashes  = function(str){  
  return (str + '').replace(/\\(.?)/g, function (s, n1) {
    switch (n1) {
    case '\\':
      return '\\';
    case '0':
      return '\u0000';
    case '':
      return '';
    default:
      return n1;
    }
  });
}

/**
 * Search value in array
 *
 * @param {Array} array
 * @param {Mixed} value
 */

exports.inArray = function (array, value) {
    if (array.indexOf) return array.indexOf(value);
    
    for (var k = 0; k < array.length; k++) {
        if (array[k] === value) return k;
    }
    
	return -1;
};

/**
 * Get Unix timestamp
 *
 */

exports.getTimestamp = function () {
	return Math.round(new Date().getTime() / 1000).toString();
};

/**
 * Convert Unix timestamp to date
 *
 * @param {String} value
 */

exports.timestampToDate = function (value) {
	var date = new Date(value * 1000);
	return date.getFullYear() + '-' + date.getMonth() + '-' + date.getDate() + ' ' + date.getHours() + ':' + date.getMinutes();
};

exports.parseMongoDateTime = function(dateTime) {
    if (dateTime == null) {
      return '';
    }
    return moment(dateTime).format('MM-DD-YYYY');
}

exports.parseActivatedDealDateTime = function(dateTime) {
	if (dateTime == null) {
		return '';
	}
	return moment(dateTime).format('dddd MMM DD YYYY');
}
/**
 * UrlReq - Wraps the http.request function making it nice for unit testing APIs.
 * @param {string} reqUrl The required url in any form
 * @param {object} options An options object (this is optional)
 * @param {Function} cb This is passed the 'res' object from your request
 *
 */
exports.urlGetRequest = function(host, path, params, cb) {
  	// module dependencies
	var http = require('http');
	var options = {
	  host: host,
	  port: 80,
	  path: path
	};
	
	if (params){
		options.path += "?" + exports.serialize(params)
	}

	http.get(options, function(res) {
	  	res.body = '';
        res.setEncoding('utf-8');

        // concat chunks
        res.on('data', function(chunk) {
          	cb(JSON.parse(chunk));
        });
	}).on('error', function(e) {
	 	cb(e);
	});
}

exports.serialize = function(obj) {
  	var str = [];
  	for(var p in obj){
  		if (obj[p].indexOf("File:") === 0){
  			str.push(p + "=File:" + encodeURIComponent(obj[p].substring(5)));
  		} else {
  			str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
  		}
  	}
   		
  	return str.join("&");
}

exports.getPhotoPath = function(path, callback){
	if (!path) {
		callback("");
		return;
	}
	fs.exists('./public' + path, function (exists) {
		if (!exists) {
			paths = path.split('/');
			callback('/images/' + paths[2] + '/deleted/' + paths[3]);
		} else {
			callback(path);	
		}		
	});
}


var dynamicSort = function(property) {
    var sortOrder = 1;
    if (property[0] === "-") {
        sortOrder = -1;
        property = property.substr(1, property.length - 1);
    }
    return function (a,b) {
        var result = (a[property] < b[property]) ? -1 : (a[property] > b[property]) ? 1 : 0;
        return result * sortOrder;
    }
}

var dynamicSortMultiple = function() {
    /*
     * save the arguments object as it will be overwritten
     * note that arguments object is an array-like object
     * consisting of the names of the properties to sort by
     */
    var props = arguments;
    return function (obj1, obj2) {
        var i = 0, result = 0, numberOfProperties = props.length;
        /* try getting a different result from 0 (equal)
         * as long as we have extra properties to compare
         */
        while (result === 0 && i < numberOfProperties) {
            result = dynamicSort(props[i])(obj1, obj2);
            i++;
        }
        return result;
    }
}

exports.dynamicSort = dynamicSort;
exports.dynamicSortMultiple = dynamicSortMultiple;


exports.toTitleCase = function(str) {
    return str.toLowerCase().replace(/(?:^|\s)\w/g, function(match) {
        return match.toUpperCase();
    });
}

exports.dateDiff = function(dateA, dateB) {
    var a = moment(dateA);
    var b = moment(dateB);
    return a.diff(b, 'seconds');
}

exports.dayDiff = function(dateA, dateB) {
    var a = moment(dateA);
    var b = moment(dateB);
    return a.diff(b, 'days');
}

exports.stringToArray = function(str) {
    var des = str.replace('[', '');
    des = des.replace(']', '');
    des = des.replace(/\"/g, '');
    return des.split(',');
}

exports.isObjectId = function(n) {
    var checkForHexRegExp = new RegExp("^[0-9a-fA-F]{24}$");
    return checkForHexRegExp.test(n);
}

exports.arrayUnique = function(inputArray) {
	var a = inputArray.concat();
	for(var i=0; i < a.length; ++i) {
    	for(var j=i+1; j<a.length; ++j) {
        	if(a[i] === a[j])
            	a.splice(j--, 1);
    	}
	}

	return a;
}

exports.arrayTrim = function(inputArray) {
	var a = inputArray.concat();
	for(var i = 0; i < a.length; i++){
		if (typeof a[i] == "string") {
			a[i] = a[i].replace(/^\s+|\s+$/g, '');
		}
	}
	return a;
}

exports.formatPrice = function(input) {
	input = input.toFixed(1);
	if (input % 1 == 0) {
		return Number(input).toFixed(0);
	}
	return input;
}

exports.formatCurrency = function(number) {
	decimals = 2;
	dec_point = ".";
	thousands_sep = ",";
	
    number = (number + '').replace(/[^0-9+\-Ee.]/g, '');
    var n = !isFinite(+number) ? 0 : +number,
        prec = !isFinite(+decimals) ? 0 : Math.abs(decimals),
        sep = (typeof thousands_sep === 'undefined') ? ',' : thousands_sep,
        dec = (typeof dec_point === 'undefined') ? '.' : dec_point,
        s = '',
        toFixedFix = function (n, prec) {
            var k = Math.pow(10, prec);
            return '' + Math.round(n * k) / k;
        };
    // Fix for IE parseFloat(0.55).toFixed(0) = 0;
    s = (prec ? toFixedFix(n, prec) : '' + Math.round(n)).split('.');
    if (s[0].length > 3) {
        s[0] = s[0].replace(/\B(?=(?:\d{3})+(?!\d))/g, sep);
    }
    if ((s[1] || '').length < prec) {
        s[1] = s[1] || '';
        s[1] += new Array(prec - s[1].length + 1).join('0');
    }
    return s.join(dec);
}

exports.checkCreditCard = function(cardnumber, cardname) {
	var ccErrorNo = 0;
  	// Array to hold the permitted card characteristics
  	var cards = new Array();

  	// Define the cards we support. You may add addtional card types as follows.
  
  	//  Name:         As in the selection box of the form - must be same as user's
  	//  Length:       List of possible valid lengths of the card number for the card
  	//  prefixes:     List of possible prefixes for the card
  	//  checkdigit:   Boolean to say whether there is a check digit
  
  	cards [0] = {name: "Visa", 
                 length: "13,16", 
               	 prefixes: "4",
                 checkdigit: true};
  	cards [1] = {name: "MasterCard", 
                 length: "16", 
                 prefixes: "51,52,53,54,55",
                 checkdigit: true};
  	cards [2] = {name: "DinersClub", 
                 length: "14,16", 
                 prefixes: "36,38,54,55",
                 checkdigit: true};
  	cards [3] = {name: "CarteBlanche", 
                 length: "14", 
                 prefixes: "300,301,302,303,304,305",
                 checkdigit: true};
  	cards [4] = {name: "AmEx", 
                 length: "15", 
                 prefixes: "34,37",
                 checkdigit: true};
  	cards [5] = {name: "Discover", 
                 length: "16", 
                 prefixes: "6011,622,64,65",
                 checkdigit: true};
  	cards [6] = {name: "JCB", 
                 length: "16", 
                 prefixes: "35",
                 checkdigit: true};
  	cards [7] = {name: "enRoute", 
                 length: "15", 
                 prefixes: "2014,2149",
                 checkdigit: true};
  	cards [8] = {name: "Solo", 
                 length: "16,18,19", 
                 prefixes: "6334,6767",
                 checkdigit: true};
  	cards [9] = {name: "Switch", 
                 length: "16,18,19", 
                 prefixes: "4903,4905,4911,4936,564182,633110,6333,6759",
                 checkdigit: true};
  	cards [10]={name: "Maestro", 
                length: "12,13,14,15,16,18,19", 
                prefixes: "5018,5020,5038,6304,6759,6761,6762,6763",
                checkdigit: true};
  	cards [11]={name: "VisaElectron", 
                length: "16", 
                prefixes: "4026,417500,4508,4844,4913,4917",
                checkdigit: true};
  	cards [12]={name: "LaserCard", 
                length: "16,17,18,19", 
                prefixes: "6304,6706,6771,6709",
                checkdigit: true};
               
  	// Establish card type
  	var cardType = -1;
  	for (var i=0; i<cards.length; i++) {
    	// See if it is this card (ignoring the case of the string)
    	if (cardname.toLowerCase () == cards[i].name.toLowerCase()) {
      		cardType = i;
      		break;
    	}
  	}
  
  	// If card type not found, report an error
  	if (cardType == -1) {
     	//ccErrorNo = 0;
     	return 0; 
  	}
   
  	// Ensure that the user has provided a credit card number
  	if (cardnumber.length == 0)  {
    	//ccErrorNo = 1;
     	return 1; 
  	}
    
  	// Now remove any spaces from the credit card number
  	cardnumber = cardnumber.replace (/\s/g, "");
  
  	// Check that the number is numeric
  	var cardNo = cardnumber
  	var cardexp = /^[0-9]{13,19}$/;
  	if (!cardexp.exec(cardNo))  {
     	//ccErrorNo = 2;
     	return 2; 
  	}
       
  	// Now check the modulus 10 check digit - if required
  	if (cards[cardType].checkdigit) {
    	var checksum = 0;                                  // running checksum total
    	var mychar = "";                                   // next char to process
    	var j = 1;                                         // takes value of 1 or 2
  
    	// Process each digit one by one starting at the right
    	var calc;
    	for (i = cardNo.length - 1; i >= 0; i--) {
    
      		// Extract the next digit and multiply by 1 or 2 on alternative digits.
      		calc = Number(cardNo.charAt(i)) * j;
    
      		// If the result is in two digits add 1 to the checksum total
      		if (calc > 9) {
        		checksum = checksum + 1;
        		calc = calc - 10;
      		}
    
      		// Add the units element to the checksum total
      		checksum = checksum + calc;
    
      		// Switch the value of j
      		if (j ==1) {j = 2} else {j = 1};
    	} 
  
    	// All done - if checksum is divisible by 10, it is a valid modulus 10.
    	// If not, report an error.
    	if (checksum % 10 != 0)  {
     		//ccErrorNo = 3;
     		return 3; 
    	}
  	}  
  
  	// Check it's not a spam number
  	if (cardNo == '5490997771092064') { 
    	//ccErrorNo = 5;
    	return 5; 
  	}

  	// The following are the card-specific checks we undertake.
  	var LengthValid = false;
  	var PrefixValid = false; 
  	var undefined; 

  	// We use these for holding the valid lengths and prefixes of a card type
  	var prefix = new Array ();
  	var lengths = new Array ();
    
  	// Load an array with the valid prefixes for this card
  	prefix = cards[cardType].prefixes.split(",");
      
  	// Now see if any of them match what we have in the card number
  	for (i=0; i<prefix.length; i++) {
    	var exp = new RegExp ("^" + prefix[i]);
    	if (exp.test (cardNo)) PrefixValid = true;
  	}
      
  	// If it isn't a valid prefix there's no point at looking at the length
  	if (!PrefixValid) {
     	//ccErrorNo = 3;
     	return 3; 
  	}
    
  	// See if the length is valid for this card
  	lengths = cards[cardType].length.split(",");
  	for (j=0; j<lengths.length; j++) {
    	if (cardNo.length == lengths[j]) LengthValid = true;
  	}
  
  	// See if all is OK by seeing if the length was valid. We only check the length if all else was 
  	// hunky dory.
  	if (!LengthValid) {
     	//ccErrorNo = 4;
     	return 4; 
  	};   
  
  	// The credit card is in the required format.
  	return -1;
}

exports.getCombinationFromString = function(srcString) {
    var srcArray = srcString.split(" ");
    var finalArray = [];
    function perm(list, ret) {
        finalArray.push(ret.join(' '));

        for (var i = 0; i < list.length; i++) {
            var x = list.splice(i, 1);
            ret.push(x);
            perm(list, ret);
            ret.pop();
            list.splice(i, 0, x);
        }
    }

    perm(srcArray, []);
    finalArray.splice(0, 1);

    return finalArray;
}

exports.convertMeterToMile = function(meterValue) {
    return Math.round(meterValue * 100 * 0.000621371192)/100;
}

exports.unescapeHTML = function(srcString) {
    return srcString.replace(/&amp;/g, "&")
            .replace(/&lt;/g, "<")
            .replace(/&gt;/g, ">")
            .replace(/&quot;/g, "\"")
            .replace(/&#039;/g, "'");
}