# Network Programing Example

Giao thức giao tiếp giữa client và server:

**Client**: localhost			// Nhập địa chỉ server

**Client**: namph 				// Nhập userID

**Server**: new user connected
user online: 
 	namph
	hainn

**Client**: [hainn] hello  			// gửi message hello đến userID: hainn

**Server**: ~hainn~ hi Nam		// nhận message từ hainn

**Client**: [havenotuser] test user 	// gửi message đến havenotuser

**Server**: user not online		// userId người nhận tin nhắn

**Client**: bye				 // thoát chương trình

**Server**: namph has quitted
	user online:
	hainn

