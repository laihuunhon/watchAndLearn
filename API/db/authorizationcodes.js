var codes = {};


exports.find = function(key, done) {
  return codes[key];
};

exports.save = function(code, json) {
  codes[code] = json;
};
