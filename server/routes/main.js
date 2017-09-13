var express = require('express');
var router = express.Router();

var User = require('../models/user');

/* GET home page. */
router.get('/', function(req, res, next) {
  console.log('come here!');
  res.render('main');
});



module.exports = router;
