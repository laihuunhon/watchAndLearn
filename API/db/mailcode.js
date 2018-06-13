var mailcodes = {};

exports.findByCode = function(id) {
  return mailcodes[id];
};


exports.save = function (id, object) {
	mailcodes[id] = object;
}

exports.delete = function (id) {
	delete(mailcodes[id]);
}