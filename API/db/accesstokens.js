var AWS  = require('aws-sdk');

AWS.config.update( config["aws"] );
var db = new AWS.DynamoDB();

var tokens = {};


exports.find = function(key, done) {
  var token = tokens[key];
  return done(null, token);
};

exports.save = function(token, userID, clientID, done) {
  tokens[token] = { userID: userID, clientID: clientID };
  return done(null);
};

exports.getByToken = function (token, callback) {

	var query = {
		"TableName": "Tokens",
		"Select": "SPECIFIC_ATTRIBUTES",
		"AttributesToGet": ["user_id", "nickname"],
		"ScanFilter": {
				"token": {
				"AttributeValueList": [
					{
						"S": token
					}
				],
				"ComparisonOperator": "EQ"
				}
		},
		"ReturnConsumedCapacity": "TOTAL"
	};
	
	db.scan(query, function(error, data) {	
		callback(error, data);
	});
}
