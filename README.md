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
