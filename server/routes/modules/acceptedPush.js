const mongoose = require('mongoose');
const User = require('../../models/userInfo');
const fcm  = require('./fcm');

module.exports = function(teamName, userArray, callback){
    
    // DBのクエリ生成
    var query = [ { 'team' : teamName } ];
    for (let i = 0; i < userArray.length; i++){
	query.push( { 'userid' : { '$ne' : userArray[i].userid }} );
    }

    User.find({ $and: query }, function(err, target){
	if (err)
	    console.log(err);

	if (target.length == 0){
	    fcm(target[0].userid);
	}
	callback(err);
    });
};
