var config = require('./config.common.js');

config = utils.merge(config, {
    env: 'production',
    server: {
        host		    : 'http://128.199.164.220',
        port        : '5555'
    },
    db: {
        host        : 'localhost',
        port        : 27017,
        name        : 'watchandlearn_prod',
        user        : 'watchandlearn',
        password    : 'watchandlearn100!'
    }
});

module.exports = config;