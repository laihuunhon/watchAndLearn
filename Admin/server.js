var express = require('express');
var app = express();

var basicAuth = require('basic-auth');

var auth = function(req, res, next) {
    function unauthorized(res) {
        res.set('WWW-Authenticate', 'Basic realm=Authorization Required');
        return res.sendStatus(401);
    };

    var user = basicAuth(req);

    if (!user || !user.name || !user.pass) {
        return unauthorized(res);
    };

    if (user.name === 'nhon' && user.pass === '0934106510') {
        return next();
    } else {
        return unauthorized(res);
    }
};

app.use('/admin/*', auth);
app.use(express.static('public'));

var server = app.listen(5556, function () {
    var host = server.address().address;
    var port = server.address().port;

    console.log('Example app listening at http://%s:%s', host, port);
});