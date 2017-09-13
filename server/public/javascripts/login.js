$(document).ready(function(){
    var src_text;
    var userID;
    var password;
    var shaObj;
    var sha256digest; 
   
    //SignUpボタンが押された時の処理
     $("#signup").click(function(){
        document.location = '/registration';
    });
    // ログインボタンが押された時の処理
    $("#login").click(function(){
        if($("#userid").val() !="" && $("#password").val() !=""){
            userID = $("#userid").val();
            password = $("#password").val();

            loginData = {
                userid: userID,
                password: password
            }

            $.post('/login',loginData);
            
        
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