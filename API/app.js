GLOBAL.utils  = require('./libs/utils');
GLOBAL.studyCookie = 'study_session=eyJpdiI6IjI0RU5IaUhyZm1PdCtZYnZsVDBJOFE9PSIsInZhbHVlIjoiczZBNHZDSGRjNDBNeU5cLzBvQlVHelk5d3RpVGpIXC9VZXVHQVRtTGVyMkFrUHM3ZVwvMzhlN3ZYclJHM0NkWUZCbWhDek40ZjJ2YzJqN1ByeWk2S05IR1E9PSIsIm1hYyI6IjFmMjAxMjc3ZDRlZmQwZmNkZmE0MDY2ZjE1OWU2MTY0OTk2ZGFiNjVkZTE4YjYzOWQ4MjlhMjRjNWQ2MmNhZjAifQ%3D%3D';

// configuration
GLOBAL.env = process.env.NODE_ENV || 'development';
GLOBAL.config = require('./config/config.' + GLOBAL.env + '.js');

var server = require('./server.js');
server.start();
