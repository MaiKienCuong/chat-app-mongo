package iuh.dhktpm14.cnm.chatappmongo.enumvalue;

public enum MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    LINK,
    SYSTEM // khi là bạn bè thì tạo room, tạo inbox cho user, sau đó gửi tin nhắn loại này (tin nhắn do hệ thống tạo ra)
}
