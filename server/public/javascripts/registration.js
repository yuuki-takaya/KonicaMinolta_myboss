$(document).ready(function(){
    var src_text;
    var userID;
    var password;
    var shaObj;
    var sha256digest; 
    var postData;

    
    // 登録ボタンが押された時の処理
    $("#send").click(function(){
        if($("#userid").val() !="" && $("#password").val() !="" && $("#username").val() !=""){
            username = $("#username").val();
            userid = $("#userid").val();
            password = $("#password").val();
            shaObj = new jsSHA("SHA-256", "TEXT", 1);
            src_text=userID+password;
            shaObj.update(src_text);
            //ここにハッシュ生成してDBに送信
            $("#text").text(shaObj.getHash("HEX"));
            postData = {
                "username":username,
		        "userid":userid,
                "password":password,
                "hash":shaObj.getHash("HEX")
            }

            $.post('/registration',postData);
            
        
        //UserIDが入力されていない場合の処理
        }else if($("#userid").val() =="" && $("#password").val() !="" && $("#username").val() !=""){
            $("#text").text("Please type UserID");
        
        //PassWordが入力されていない場合の処理
        }else if($("#userid").val() !="" && $("#password").val() =="" && $("#username").val() !=""){
            $("#text").text("Please type PassWord");
        
        //UserNameが入力されていない場合の処理
        }else if($("#userid").val() !="" && $("#password").val() !="" && $("#username").val() ==""){
            $("#text").text("Please type UserName");

        //UserIDとPassWordが入力されていない場合の処理
        }else if($("#userid").val() =="" && $("#password").val() =="" && $("#username").val() !=""){
            $("#text").text("Please type UserID & PassWord");

        //UserNameとPassWordが入力されていない場合の処理
        }else if($("#userid").val() !="" && $("#password").val() =="" && $("#username").val() ==""){
            $("#text").text("Please type UserName & PassWord");

        //UserIDとUserNameが入力されていない場合の処理
        }else if($("#userid").val() =="" && $("#password").val() !="" && $("#username").val() ==""){
            $("#text").text("Please type UserName & UserID");

        //UserIDとPassWordとUserNameが入力されていない場合の処理
        }else if($("#userid").val() =="" && $("#password").val() =="" && $("#username").val() ==""){
            $("#text").text("Please type UserName & UserID & PassWord");

        }
    });
});
