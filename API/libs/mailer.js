var nodemailer = require('nodemailer');

module.exports = {
    send: function(html, params, callback) {
        var auth = {};
        auth.user = config.mail.support.email;
        auth.pass = config.mail.support.pass;
        params.from = config.mail.support.user;

        var smtpTransport = nodemailer.createTransport(config.mail.transport, {
            service: config.mail.service,
            auth: auth
        });

        smtpTransport.sendMail({
            from        : params.from,
            to          : params.to,
            subject     : params.subject,
            html        : html
        }, function (error, responseStatus) {
            callback(error, responseStatus) 
        });
    }
};