const express = require('express');
const router  = express.Router();
const util    = require('util');
const spawn   = require('child_process').spawn;
const php     = spawn('php', ['Push_Android.php']);
const fcm     = require('./fcm');

/* GET home page. */
router.get('/', function(req, res, next) {
    
    fcm.fcm();

    res.render('index', { title: 'Express' });

});

module.exports = router;
