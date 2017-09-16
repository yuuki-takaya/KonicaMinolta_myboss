const express = require('express');
const router  = express.Router();
const mongoose = require('mongoose');
const User = require('../models/userInfo');
const Team = require('../models/team');
const fcm  = require('./modules/fcm');
const timeCompare = require('./modules/timeCompare');
const acceptedPush = require('./modules/acceptedPush');

router.post('/registration', function(request, response){
    console.log('catch the register request');
    response.setHeader('Content-Type', 'application/json');

    var userid = request.query.userid;
    var token  = request.query.token;
    var team   = request.query.team;
    
    User.find({ "userid" : userid }, function(err, result){
	if (err)
	    console.log(err);

	// DBにuserを格納．userの構造は以下の通り
	// user = {
	//     userid : userid
	//     token  : token
	//     team   : team
	// }

	// 新規登録
	if (result.length == 0){
	    var user = new User();

	    user.userid = userid;
	    user.token  = token;
	    user.team   = team;

	    user.save(function(err){
		if (err) console.log(err);
	    });
	}
	response.json({ 'status' : 200 });
    });
});

router.post('/sleeper', function(request, response){
    console.log('catch the sleeper request');    
    response.setHeader('Content-Type', 'application/json');

    var userid = request.query.userid;
    var mesg = {
	message : '眠たくてしょうがない人がいます!\n承認してください!'
    };

    User.find({
	"userid" : { '$ne': userid }
    }, function(err, result){
	if (err)
	    console.log(err);

	// result.tokenを取り出してpush通知
	for (let i = 0; i < result.length; i++) {
	    fcm( result[i].token, mesg );
	}
    });
    response.json({ 'status' : 200 });
});

router.post('/accept', function(request, response){
    console.log("catch the accept request");
    response.setHeader('Content-Type', 'application/json');
    console.log(request.body);

    let userid = request.body.userid;
    let time = request.body.time;

    // teamの構造
    // teamName : String,
    // userNum : Number (Magic Number)
    // user : [
    //     {
    // 	    userid : String,
    // 	    time   : date
    //     },
    // ]

    // useridからteam名を抽出
    User.find({	"userid" : userid }, function(err, userFindResult){
	if (err)
	    console.log(err);

	if (userFindResult.length != 0){
	    // 抽出したteam名からTeamのテーブルを抽出
	    Team.find( { 'teamName' :  userFindResult[0].team }, function(err, teamInfo){
		if (err)
		    console.log(err);

		if ( teamInfo.length != 0 ){
		    // 現在格納されているuser配列の長さがTeamの( 固定ユーザ人数 - 1 )より小さい場合，
		    // DBのuser配列にPOSTリクエストの内容を格納
		    if (teamInfo[0].user.length < teamInfo[0].userNum - 1){
			// push通知の情報をしてpush通知
			var mesg = {
			    message : 'あなたは' + teamInfo[0].user.length - 1 + '番目に承認しました!'
			};
			fcm( userFindResult[0].token, mesg );
			
			var userArray = teamInfo[0].user;
			userArray.push(
			    {
				'userid' : userid,
				'time'   : time
			    }
			);
			Team.update(
			    { 'teamName' :  userFindResult[0].team }, { $set: {'user' : userArray }},
			function(err, result){
			    if (err)
				console.log(err);
			    
			    Team.find({ 'teamName' :  userFindResult[0].team }, function(err, teamInfo){
				// 全員が承認した場合
				if (teamInfo[0].user.length - 1 == teamInfo[0].userNum){
				    // teamInfo[0].userの時間比較および寝たい人へpush通知
				    acceptedPush(teamInfo[0].teamName, teamInfo[0].user, function(err){
					if (err)
					    console.log(err);
				    });
				    timeCompare(teamInfo[0].teamName);
				}
			    });
			    response.json({ 'status' : 200 });
			});
		    }
		}
	    });
	}
    });
});

module.exports = router;
