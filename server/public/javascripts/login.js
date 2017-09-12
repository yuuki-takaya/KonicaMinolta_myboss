$(document).ready(function(){
    var src_text;
    var userID;
    var password;
    var shaObj;
    var sha256digest; 
   

    // ログインボタンが押された時の処理
    $("#send").click(function(){
        if($("#userid").val() !="" && $("#password").val() !=""){
            userID = $("#userid").val();
            password = $("#password").val();
            shaObj = new jsSHA("SHA-256", "TEXT", 1);
            src_text=userID+password;
            shaObj.update(src_text);
            //ここにハッシュ生成してDBに送信
            $("#text").text(shaObj.getHash("HEX"));
            
        
        //UserIDが入力されていない場合の処理
        }else if($("#userid").val() =="" && $("#password").val() !=""){
            $("#text").text("Please type UserID");
        
        //PassWordが入力されていない場合の処理
        }else if($("#userid").val() !="" && $("#password").val() ==""){
            $("#text").text("Please type PassWord");
        
        //UserIDとPassWordが入力されていない場合の処理
        }else if($("#userid").val() =="" && $("#password").val() ==""){
            $("#text").text("Please type UserID & PassWord");

        }
    });
});