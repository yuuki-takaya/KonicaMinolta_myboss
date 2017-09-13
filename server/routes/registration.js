var express = require('express');
var bodyParser = require('body-parser');
var router = express.Router();
var mongoose = require('mongoose');


//モデルの宣言
var User = require('../models/user');

/* GET home page. */
router.get('/', function(req, res, next) {
    console.log("registration.js");
    res.render('registration', { Hello: 'Express' });

});


// POSTリクエストがきた時の処理
/* POST内容はJSON形式で飛ばされる
 * JSON format
 * {
 *  "username" : String
 *  "userid"   : String
 *  "password" : String
 *  "hash"     : String
 * }
 */
router.post('/', function(req, res, next) {
    console.log("catch the post request");
    res.setHeader('Content-Type', 'text/plain');

    console.log(req.body.username);
    console.log(req.body.userid);
    console.log(req.body.password);
    
    res.redirect('/main');
    var username = req.body.username;
    var userid = req.body.userid;
    var password = req.body.password
    

    User.find({ "userid" : userid }, function(err, result){
        if (err)console.log(err);
        
        if(result.length == 0){
            var user = new User();
            user.username = username;
            user.userid = userid;
            user.password = password;
            
            user.save(function(err){
                if(err)console.log(err);

            });

            console.log("regist to DB");
            res.redirect('/main');
        }else{
            console.log("Already registrated");
            res.render('registration', { Hello: 'Already registrated' });

        }
    });
});

module.exports = router;
