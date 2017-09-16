var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var Team = new Schema({
    teamName : { type: String, require: true },
    userNum  : { type: Number, require: true},
    user     : []
});

module.exports = mongoose.model('team', Team);
