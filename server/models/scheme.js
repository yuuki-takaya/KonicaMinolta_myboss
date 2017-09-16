var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var User = new Schema({
    username : { type: String, require: true },
    userid   : { type: String, require: true },
    team     : { type: String, require: true },
    point    : { type: Number, require: true }
});

module.exports = mongoose.model('user', User);
