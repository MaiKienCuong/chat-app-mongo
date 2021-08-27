# Chat-App

```diff
-- truy cập http://localhost:8080/swagger-ui/ để xem danh sách api
```

---
---
---
---

# api check sdt
/api/auth/signup/check_phone_number [POST]

# api lưu thông tin user đăng ký
/api/auth/signup/save_information [POST - PUT] 
```

    {
       "displayName": "Mai Kiên Cường",
        "password": "123456",
        "phoneNumber": "07611234645"
    }

    return
        thành công - > 
            {
                "id" : 20,
               "displayName": "Mai Kiên Cường",
                "password": "123456",
                "phoneNumber": "07611234645"
             }
             
         thất bại -> số điện thoại đã tồn tại
```

# api gửi mã xác thực
/api/auth/signup/send_vetification_code [POST] 
data mẫu :

```

{
    "phoneNumber": "0961516941",
    "email": "maikiencuongiuh@gmail.com"
}

return

        thất bại -> email đã tồn tại
        thành công -> gửi mã xác thực thành công
```



# api xác thực email
/api/auth/signup/vetify [POST]
```
{
    "email": "maikiencuongiuh@gmail.com",
    "verificationCode":"720268"
    
}

return
    thất bại -> Mã xác nhận không chính xác
    thành công -> Xác thực thành công
   ```
# api check email
/api/auth/signup/check_email [POST]
```
    thành công -> Email hợp lệ
    thất bại -> Email đã tồn tại
```

# api set mật khẩu
/api/auth/signup/password  [POST]
```
    thành công -> SDT hợp lệ
    thất bại -> SDT đã tồn tại

```
## Lấy tất cả tin nhắn của một cuộc hội thoại
/api/messages/conversation/{id} [GET], có thể thêm 3 tham số page=?, size=?, sort=?, vd: /api/messages/conversation/1?page=1,size=1,sort=id,desc. tham số sort đang bị lỗi
> trả về
```
{
    // nội dung
    "content": [
        // tin nhắn file
        {
            "id": 12,
            "createAt": "2021-08-20 23:51:09",
            "sender": {
                "id": 4,
                "username": "luutuankha",
                "displayName": "Lưu Tuấn Kha",
                "imageUrl": null
            },
            "pin": false,
            "type": "FILE",
            "reactions": [
                {
                    "id": 1,
                    "user": {
                        "id": 1,
                        "username": "maikiencuong",
                        "displayName": "Mai Kiên Cường",
                        "imageUrl": null
                    },
                    "type": "SAD"
                }
            ],
            "url": "https://file-bao-cao-bai-tap-lon",
            "fileName": "File báo cáo bài tập lớn",
            "size": 123456
        },
        //tin nhắn text
        {
            "id": 11,
            "createAt": "2021-08-20 23:51:08",
            // ...sender 
            "pin": false,
            "type": "TEXT",
            // ...reactions
            "content": "để anh lo"
        },
        // tin nhắn hình ảnh
        {
            "id": 24,
            "createAt": "2021-08-20 23:51:21",
            // ... sender 
            "pin": false,
            "type": "IMAGE",
            // ... reactions
            "url": "https://hinh-anh-bao-cao-bai-tap-lon"
        }
        
    ],
    "pageable": {
        ....
    },
    // có phải trang cuối hay không
    "last": false,
    // tổng số phần tử
    "totalElements": 3,
    // tổng số trang
    "totalPages": 3,
    // số lượng phần tử trên 1 trang
    "size": 1,
    // trang hiện tại, bắt đầu từ 0
    "number": 0,
    "sort": {
        ...
    },
    // có phải trang đầu hay không
    "first": true,
    // size of content
    "numberOfElements": 1,
    // danh sách có trống hay không
    "empty": false
}
```


## Lấy tất cả hội thoại của người dùng hiện tại đang đăng nhập
/api/conversations [GET]
> trả về
```
{
    // nội dung
    "content": [
        {
            "id": 1,
            "name": "Lập trình di động",
            // tin nhắn cuối cùng
            "lastMessage": {
                "id": 12,
                "createAt": "2021-08-20 23:51:09",
                "sender": {
                    "id": 4,
                    "username": "luutuankha",
                    "displayName": "Lưu Tuấn Kha",
                    "imageUrl": null
                },
                "pin": false,
                "type": "FILE",
                "url": "https://file-bao-cao-bai-tap-lon",
                "fileName": "File báo cáo bài tập lớn",
                "size": 123456
            }
        }
    ],
    "pageable": {
        ...
    },
    // có phải trang cuối hay không
    "last": false,
    // tổng số phần tử
    "totalElements": 3,
    // tổng số trang
    "totalPages": 3,
    // số lượng phần tử trên 1 trang
    "size": 1,
    // trang hiện tại, bắt đầu từ 0
    "number": 0,
    "sort": {
        ...
    },
    // có phải trang đầu hay không
    "first": true,
    // size of content
    "numberOfElements": 1,
    // danh sách có trống hay không
    "empty": false
}
```

## Đăng nhập
> /api/auth/signin [POST]
```
{
    "email":"maikiencuong",
    "password":"123456"
}
```
> hoặc
```
{
    "username":"maikiencuong",
    "password":"123456"
}
```
> hoặc
```
{
    "phone":"maikiencuong",
    "password":"123456"
}
```
> đăng nhập thành công trả về trả về thông tin user và lưu token vào cookie tên access_token
```
{
    "id": 1,
    "username": "maikiencuong",
    "displayName": "Mai Kiên Cường",
    "gender": "Nam",
    "dateOfBirth": "2021-08-20",
    "phoneNumber": "0961516941",
    "email": "maikiencuongiuh@gmail.com",
    "createAt": "2021-08-20T11:01:00.413+00:00",
    "active": true,
    "block": false,
    "imageUrl": null,
    "roles": "ROLE_USER"
}
```
