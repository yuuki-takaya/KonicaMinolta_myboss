var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var User = new Schema({
    username : { type: String },
    userid   : { type: String, require: true },
    team     : { type: String, require: true },
    point    : { type: Number, default: 0 },
    token    : { type: String, require: true }
});

module.exports = mongoose.model('user', User);
