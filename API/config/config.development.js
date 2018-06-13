var config = require('./config.common.js');

config = utils.merge(config, {
    env: 'development',
    server: {
        host		: 'http://localhost',
        port        : '5555'
    },
    db: {
        host        : 'localhost',
        port        : 27017,
        name        : 'watchandlearn_dev',
        user        : 'watchandlearn',
        password    : 'watchandlearn100!'
    }
});

module.exports = config;
