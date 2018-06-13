var config = require('./config.common.js');

config = utils.merge(config, {
    env: 'staging',
    server: {
        host		: 'http://localhost',
        port        : '7878'
    },
    db: {
        host        : 'localhost',
        port        : 27017,
        name        : 'watchandlearn_stage',
        user        : 'watchandlearn',
        password    : 'watchandlearn100!'
    }
});

module.exports = config;