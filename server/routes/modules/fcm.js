const request = require('request');
const utils = require('date-utils');

module.exports = function(query, mesg){

    const date = new Date();
    const dateString = date.toFormat("YYYY-MM-DD HH24:MI:SS");
    const url = 'https://fcm.googleapis.com/fcm/send';
    const serverkey = 'AAAAGjtC_Sg:APA91bEWDo1snCljw3iR_0UOp-4pJL13eCoarvbfd4Lbuxq6A6kKI56gOJ-FqRC3nrUtCayZDerqbBM5l7TpVGaQuMMbr3URzQsLQBMJGLmePPtgamoSYxvdo6n9hZWZCkiHm3-9-VW1';

    const token = query;

    // data payload for notification
    const data = {
	title: 'Hello!',
	body: dateString
    };

    // data payload for data
    // const data = {
    // 	message : 'Hello!',
    // 	date    : dateString
    // };

    // HTTP header
    var header = {
	'Content-Type': 'application/json',
	'Authorization': 'key=' + serverkey
    };

    // request options
    var options = {
	uri: url,
	method: 'POST',
	headers: header,
	json: {
	    // 'to': '/topics/Attendance_Android',
	    'to': token,
	    'notification': data // for notification
	    // 'data': data      // for data
	}
    };

    request(options, function (error, response, body) {

	if (body) {
	    console.log(body);
	}
	if (error) {
	    console.log(error);
	}
    });
    return;
};
