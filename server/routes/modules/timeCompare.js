const mongoose = require('mongoose');
const User = require('../../models/userInfo');
const Team = require('../../models/team');

module.exports = function(teamName){

    Team.find({ 'teamName' : teamName }, function(err, result){
	if (err)
	    console.log(err);

	User.find({ 'userid' : result[0].userid }, function(err, firstUser){
	    if(err)
		console.log(err);

	    var point = firstUser[0].point + 1;
	    User.update(
		{ 'userid' : result[0].userid },
		{ $set: {'point': point }},
		function(err, updatedResult){
		    if (err)
			console.log(err);
		}
	    );
	});
	
	var user = [];
	Team.update({ 'teamName' : teamName }, 
		    { $set:{ 'user' : user }},
		    function(err){
			if(err)
			    console.log(err);
		    }
		   );
    });
};
