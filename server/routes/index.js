var express = require('express');
var router = express.Router();

var User = require('../models/user');

/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index', { Hello: 'Express' });
});

router.post('/login', function(req, res, next) {
  console.log("catch the get request");
  res.setHeader('Content-Type', 'text/plain');
  console.log(req.body);
  


  res.render('index', { Hello: 'Success' });
});


module.exports = router;
