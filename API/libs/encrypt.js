var crypto = require('crypto');

var md5 = function(str) {
    return crypto.createHash('md5').update(str).digest('hex');
}

exports.generateSalt = function() {
    var set = '0123456789abcdefghijklmnopqurstuvwxyzABCDEFGHIJKLMNOPQURSTUVWXYZ';
    var salt = '';
    for (var i = 0; i < 11; i++) {
        var p = Math.floor(Math.random() * set.length);
        salt += set[p];
    }
    return salt;
}

exports.saltAndHash = function(salt, password) {
    return (salt + md5(password + salt));
}

exports.validatePassword = function(plainPassword, hashedPassword) {
    var salt = hashedPassword.substr(0, 11);
    var validHash = salt + md5(plainPassword + salt);
    if (hashedPassword === validHash) {
        return true;
    }
    return false;
}

exports.md5 = function(str){
	return md5(str);
}
