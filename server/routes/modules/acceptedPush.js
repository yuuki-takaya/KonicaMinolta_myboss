const mongoose = require('mongoose');
const User = require('../../models/userInfo');
const fcm  = require('./fcm');

module.exports = function(teamName, userArray, callback){
    
    var mesg = {
	id : '3',
	label : 'あなたのお昼寝は承認されました',
	text  : '存分に寝て生産性向上に努めましょう'
    };

    // DBのクエリ生成
    var query = [ { 'team' : teamName } ];
    for (let i = 0; i < userArray.length; i++){
	query.push( { 'userid' : { '$ne' : userArray[i].userid }} );
    }

    User.find({ $and: query }, function(err, target){

	console.log(target);

	if (err)
	    console.log(err);

	if (target.length == 1){
	    fcm( target[0].token, mesg );
	}
	else if (target.length > 1){
	    for (let i = 0; i < target.length; i++){
		fcm( target[i].token, mesg );
	    }
	}

	callback(err);
    });
};
