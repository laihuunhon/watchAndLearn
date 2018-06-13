var transactions = {};

exports.findByTransactionId = function(id) {
  return transactions[id];
};


exports.save = function (tid, clientID) {
	transactions[tid] = clientID;
}


exports.delete = function (tid) {
	delete(transactions[tid]);
}