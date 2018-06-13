exports.request = function(inputProtocol) {
    var protocol = require('http');

    if (inputProtocol == 'https') {
        protocol = require('https');
    }

    return {
        get: function(config) {
            this.send('GET', config);
        },
        post: function(config) {
            this.send('POST', config);
        },
        put: function(config) {
            this.send('PUT', config);
        },
        del: function(config) {
            this.send('DELETE', config);
        },
        send: function(method, config, headers) {
            var message = '';

            config.method = method;
            config.headers = headers ? headers : {'Connection':'close', 'Content-Type' : 'text/html, application/json;charset=UTF-8', 'Accept-Charset' : 'UTF-8'};

            try {
                console.log(config)
                var req = protocol.request(config, function(res) {
                    res.setEncoding('utf-8');

                    res.on('data', function (chunk) {
                        message = message + chunk;
                    });

                    res.on('end', function () {
                        if (res.statusCode === 200) {
                            if (config.on && config.on.success) {
                                config.on.success(message);
                            }
                        } else {
                            console.log(res);
                            if (config.on && config.on.failure) {
                                config.on.failure({
                                    error: 'error',
                                    statusCode: res.statusCode,
                                    message: 'error'
                                });
                            }
                        }
                    })
                });

                req.on('error', function(e) {
                    if (config.on && config.on.failure) {
                        config.on.failure({
                            error: 'error',
                            statusCode: '',
                            message: e
                        });
                    }
                });

                if (config.content) {
                    req.write(config.content);
                }

                req.end();
            } catch (e) {
                if (config.on && config.on.failure) {
                    config.on.failure({
                        error: 'error',
                        statusCode: '',
                        message: e
                    });
                }
            }
        }
    };
}