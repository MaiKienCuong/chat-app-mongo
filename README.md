#### Chat-App

```diff
-- truy cập http://localhost:8080/swagger-ui/ để xem danh sách api
```

---

#### api/inboxs

lấy danh sách tất cả cuộc trò chuyện, trả về id(id của inbox), room(inbox này thuộc room nào), lastMessage(tin nhắn
cuối), lastMessageReadBy(tin nhắn cuối được ai đọc), countNewMessage(số tin nhắn mới chưa đọc)

- nếu room.type=ONE (chat 1-1) thì tên cuộc trò chuyện=room.to.displayName, ảnh cuộc trò chuyện=room.to.imageUrl

- nếu room.type=GROUP (chat nhóm) thì tên cuộc trò chuyện=room.name, ảnh cuộc trò chuyện=room.imageUrl

```
{
    "content": [
        {
            "id": "1",
            "room": {
                "id": "1",
                "type": "ONE",
                "to": {
                    "id": "2",
                    "displayName": "Trương Công Cường",
                    "imageUrl": "https://timesofindia.indiatimes.com/photo/67586673.cms"
                }
            },
            "lastMessage": {
                "id": "2",
                "sender": {
                    "id": "2",
                    "displayName": "Trương Công Cường",
                    "imageUrl": "https://timesofindia.indiatimes.com/photo/67586673.cms"
                },
                "createAt": "2021-08-28 05:00:36",
                "type": "text",
                "content": "ừ, hello nha"
            },
            "lastMessageReadBy": [
                {
                    "readByUser": {
                        "id": "1",
                        "displayName": "Mai Kiên Cường",
                        "imageUrl": "https://timesofindia.indiatimes.com/photo/67586673.cms"
                    },
                    "readAt": "2021-08-28 05:00:18"
                }
            ],
            "countNewMessage": 0
        },
        {
            "id": "3",
            "room": {
                "id": "2",
                "name": "Nhóm Công nghệ phần mềm",
                "imageUrl": "https://timesofindia.indiatimes.com/photo/67586673.cms",
                "type": "GROUP"
            },
            "lastMessage": {
                "id": "6",
                "sender": {
                    "id": "4",
                    "displayName": "Lưu Tuấn Kha",
                    "imageUrl": "https://timesofindia.indiatimes.com/photo/67586673.cms"
                },
                "createAt": "2021-08-28 05:01:16",
                "type": "text",
                "content": "ừ, hello nha"
            },
            "lastMessageReadBy": [
                {
                    "readByUser": {
                        "id": "3",
                        "displayName": "Nguyễn Công Thành Đạt",
                        "imageUrl": "https://timesofindia.indiatimes.com/photo/67586673.cms"
                    },
                    "readAt": "2021-08-28 05:00:26"
                },
                {
                    "readByUser": {
                        "id": "1",
                        "displayName": "Mai Kiên Cường",
                        "imageUrl": "https://timesofindia.indiatimes.com/photo/67586673.cms"
                    },
                    "readAt": "2021-08-28 05:00:25"
                }
            ],
            "countNewMessage": 1
        }
    ],
    "last": true, //có phải trang cuối hay không
    "totalPages": 1, //tổng số trang
    "totalElements": 2, //tổng số element trong database
    "size": 20, // size của một trang
    "number": 0, // trang hiện tại
    "first": true,
    "numberOfElements": 2, // số element trả về
    "empty": false
}
```