#### Chat-App

```diff
-- truy cập http://localhost:8080/swagger-ui/ để xem danh sách api
```

---
```json
// json mẫu dto UserReport
{
    "id": "61a01e50f4ec10520f1b54b1",
    "from": {
        "id": "2",
        "username": "truongcongcuong",
        "displayName": "Trương Công Cường",
        "gender": "Nam",
        "dateOfBirth": "2021-11-26 00:21:19",
        "phoneNumber": "0961516942",
        "email": "truongcongcuong@gmail.com",
        "imageUrl": "https://img.poki.com/cdn-cgi/image/quality=78,width=600,height=600,fit=cover,g=0.5x0.5,f=auto/b5bd34054bc849159d949d50021d8926.png",
        "onlineStatus": "OFFLINE",
        "lastOnline": "2021-11-26 00:21:19",

        // số lần người dùng này bị báo cáo
        "reportedCount": 0
    },
    "to": {
        "id": "1",
        "username": "maikiencuong",
        "displayName": "Mai Kiên Cường",
        "gender": "Nam",
        "dateOfBirth": "2021-11-26 00:21:18",
        "phoneNumber": "0961516941",
        "email": "maikiencuongiuh@gmail.com",
        "imageUrl": "https://timesofindia.indiatimes.com/photo/67586673.cms",
        "onlineStatus": "OFFLINE",
        "lastOnline": "2021-11-26 00:21:18",
        
        // số lần người dùng này bị báo cáo
        "reportedCount": 3
    },
    "createAt": "2021-11-26 06:37:52",
    // nội dung báo cáo
    "content": "ảnh đại diện không hợp lệ",
    // nếu là báo cáo về tin nhắn thì message != null, còn không thì chỉ quan tâm đến content
    "message": {
        "id": "1",
        "roomId": "1",
        "sender": {
            "id": "2",
            "displayName": "Trương Công Cường",
            "imageUrl": "https://img.poki.com/cdn-cgi/image/quality=78,width=600,height=600,fit=cover,g=0.5x0.5,f=auto/b5bd34054bc849159d949d50021d8926.png",
            "onlineStatus": "OFFLINE",
            "lastOnline": "2021-11-26 00:21:19",
            "friendStatus": "NONE",
            "phoneNumber": "0961516942",
            "blockMe": false,
            "meBLock": false
        },
        "createAt": "2021-08-20 16:17:59",
        "type": "MEDIA",
        //nội dung tin nhắn
        "content": "1. Lorem Ipsum is simply dummy text of the printing and typesetting industry.05481abc-871e-44b3-a4bc-ea43b2d84093 Lorem Ipsum has been the industry's standard dummy text ever since the 1500s",
        "pin": false,
        "deleted": false,
        "reactions": [
            {
                "reactByUserId": "2",
                "type": "WOW"
            }
        ],
        "readbyes": [],
        // danh sách các hình ảnh, video, file nếu đây là tin nhắn type=MEDIA
        // type=TEXT hoặc type=LINK thì media=null
        "media": [
            {
                "url": "https://img.poki.com/cdn-cgi/image/quality=78,width=600,height=600,fit=cover,g=0.5x0.5,f=auto/b5bd34054bc849159d949d50021d8926.png",
                "type": "IMAGE",
                "size": 0,
                "name": "abc"
            },
            {
                "url": "https://timesofindia.indiatimes.com/photo/67586673.cms",
                "type": "IMAGE",
                "size": 0,
                "name": "abc"
            }
        ]
    },
    // báo cáo này đã xem hay chưa
    "seen": false,
    // ảnh người dùng đã đính kèm trong khi báo cáo
    "media": [
        {
            "url": "https://img.poki.com/cdn-cgi/image/quality=78,width=600,height=600,fit=cover,g=0.5x0.5,f=auto/b5bd34054bc849159d949d50021d8926.png",
            "type": "IMAGE",
            "size": 0
        }
    ]
}
```

```json
// json mẫu entity UserReport khi gửi báo cáo
{
    "toId": "1",
    "content": "tin nhắn xúc phạm",
    "messageId": "1",
  
    // nếu muốn đính kèm ảnh, phải upload ảnh lên bằng POST api/file trước
    // sau đó lấy mảng response trả về đó set làm media, rồi mới gọi api báo cáo
    "media": [
        {
            "url": "https://img.poki.com/cdn-cgi/image/quality=78,width=600,height=600,fit=cover,g=0.5x0.5,f=auto/b5bd34054bc849159d949d50021d8926.png",
            "type": "IMAGE"
        }
    ]
}
```
