# コニカミノルタハッカソン

## ブランチの切り方
- テスト環境 : test
- バックエンド : develop_back
- フロントエンド : develop_front
## マージ規約
- 各人開発後のマージはテスト環境（testブランチ）へマージすること．
- masterへのマージはtestでの動作確認およびチームメンバ全員の合意が取れ次第行う


## DB設計(*はprimary)
### User
|		|userid*	|password	|username	|
|:--	|:--		|:--		|:--		|
|型 	|String		|String  	|String		|
|一意性	|unique		|unique		|ununique	|
