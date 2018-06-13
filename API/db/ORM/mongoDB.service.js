var mongoose = require('mongoose');
var utils    = require('../../libs/utils');

var dbErrorResponse = function(message) {
    return {
        error: true,
        message: message
    }
}
exports.dbErrorResponse = dbErrorResponse;

exports.create = function(schema, callback) {
    schema.save(function(error, result) {
        if (error) {
            callback(dbErrorResponse(error));
        } else {
        	callback(result);	
        }
    });
}

exports.getOneItem = function(schema, conditionFields, returnFields, callback) {
    schema.findOne(conditionFields, returnFields, function(error, result) {
        if (error) {
            callback(dbErrorResponse(error));
        } else {
        	callback(result);	
        }
    });
}

exports.getItems = function(schema, conditionFields, returnFields, callback) {
    schema.find(conditionFields, returnFields, function(error, result) {
        if (error) {
            callback(dbErrorResponse(error));
        } else {
        	callback(result);
        }
    });
}

exports.getItemsWithOptions = function(schema, conditionFields, options, returnFields, callback) {
    schema.find(conditionFields, returnFields, options).exec(function(error, result){
        if (error){
            callback(dbErrorResponse(error));   
        } else {
            callback(result);
        }
    });
}

exports.saveItem = function(schema, callback) {
    schema.save(function(error, result) {
        if (error) {
            callback(dbErrorResponse(error));
        } else {
        	callback(result);
        }
    });
}

exports.removeItem = function(schema, conditionFields, callback) {
    schema.remove(conditionFields, function(error) {
        if (error) {
            callback(dbErrorResponse(error));
        } else {
        	callback(null);	
        }
    });
}

exports.removeItemLogical = function(schema, itemIDs, callback){
	ids = itemIDs.split(",");
	schema.update({_id: {$in: ids}}, {deleted: new Date(Date.now())}, {multi: true}, function(error, result){
		if (error){
			callback(dbErrorResponse(error));
		} else {
			callback(result);	
		}
	});	
}

exports.recoverItemLogical = function(schema, itemIDs, callback){
	ids = itemIDs.split(",");
	schema.update({_id: {$in: ids}}, {$unset: {deleted: 1}}, {multi: true}, function(error, result){
		if (error){
			callback(dbErrorResponse(error));
		} else {
			callback(result);	
		}
	});	
}

exports.multipleUpdateItem = function(schema, itemIDs, updateFields, callback) {
    ids = itemIDs.split(",");
    schema.update({_id: {$in: ids}}, updateFields, {multi: true}, function(error, result){
        if (error){
            callback(dbErrorResponse(error));
        } else {
            callback(result);   
        }
    }); 
}

exports.count = function(schema, conditionFields, callback) {
    schema.count(conditionFields, function(error, count) {   
        if (error){
            callback(dbErrorResponse(error));   
        } else {
            callback(count);
        }
    });
}

exports.distinct = function(schema, fieldName, conditions, callback) {	
	schema.distinct(fieldName, conditions, function(error, result) {
		if (error){
            callback(dbErrorResponse(error));   
        } else {
            callback(result);
        }
	});
}

exports.updateAndReturnItem = function(schema, condition, update, options, callback) {
    schema.findOneAndUpdate(condition, update, options, function(error, result) {
        if (error){
            callback(dbErrorResponse(error));
        } else {
            callback(result);
        }
    });
}

exports.updateItem = function(schema, condition, update, options, callback) {
    schema.update(condition, update, options, function(error, result) {
        if (error){
            callback(dbErrorResponse(error));
        } else {
            callback(result);
        }
    });
}