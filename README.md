# コニカミノルタハッカソン

## ブランチの切り方
- テスト環境 : test
- バックエンド : develop_back
- フロントエンド : develop_front
## マージ規約
- 各人開発後のマージはテスト環境（testブランチ）へマージすること．
- masterへのマージはtestでの動作確認およびチームメンバ全員の合意が取れ次第行う

## リクエスト
### 登録
 - http://_ip_:3000/registration
 ~~~ json
 {
    userid: String,
    token : String,
    team  : String
 }
 ~~~
### 眠たい
 - http://_ip_:3000/sleeper
 ~~~ json
 {
    userid: String
 }
 ~~~
### 承認
 - http://_ip_:3000/accept
 ~~~ json
 {
    userid: String,
    time  : date
 }
 ~~~
 
## DB設計(*はprimary)
### User

|		|userid*	|password	|username	|
|:--	|:--		|:--		|:--		|
|型 	|String		|String  	|String		|
|一意性	|unique		|unique		|ununique	|
