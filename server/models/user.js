var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var User = new Schema({
    username : { type: String, require: true }, 
    userid : {type: String, require: true},
    password : { type: String, require: true },
    hash : {type: String, require: true,unique:true}
});

module.exports = mongoose.model('user', User);