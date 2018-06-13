var config = require('./config.common.js');

config = utils.merge(config, {
    env: 'local',
    server: {
    	host		: 'http://localhost',
        port        : '5555'
    },
    db: {
        host        : 'localhost',
        port        : 27017,
        name        : 'watchandlearn_local',
        user        : '',
        password    : ''
    }
});

module.exports = config;