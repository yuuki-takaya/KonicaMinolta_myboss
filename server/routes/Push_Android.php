<?php
	$api_key  = "AAAAGjtC_Sg:APA91bEWDo1snCljw3iR_0UOp-4pJL13eCoarvbfd4Lbuxq6A6kKI56gOJ-FqRC3nrUtCayZDerqbBM5l7TpVGaQuMMbr3URzQsLQBMJGLmePPtgamoSYxvdo6n9hZWZCkiHm3-9-VW1";
	$base_url = "https://fcm.googleapis.com/fcm/send";

	// toに指定しているのはトピック名:testに対して一括送信するという意味
	// 個別に送信したい場合はここに端末に割り振られたトークンIDを指定する
	$data = array(
		"to"           => "/topics/Attendance_Android",
		"priority"     => "high",
		"notification" => array("title" => "テスト送信タイトル","body"  => "テスト送信本文")
	);

	$header = array(
		"Content-Type:application/json",
		"Authorization:key=".$api_key
	);

	$context = stream_context_create(array(
		"http" => array(
			'method' => 'POST',
			'header' => implode("\r\n",$header),
			'content'=> json_encode($data))
		));

	file_get_contents($base_url,false,$context);
?>
