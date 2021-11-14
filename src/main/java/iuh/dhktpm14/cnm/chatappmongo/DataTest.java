package iuh.dhktpm14.cnm.chatappmongo;

import iuh.dhktpm14.cnm.chatappmongo.entity.Friend;
import iuh.dhktpm14.cnm.chatappmongo.entity.FriendRequest;
import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.InboxMessage;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import iuh.dhktpm14.cnm.chatappmongo.entity.Reaction;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.MessageType;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.OnlineStatus;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.ReactionType;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.RoomType;
import iuh.dhktpm14.cnm.chatappmongo.repository.FriendRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.FriendRequestRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxMessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.MessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.ReadTrackingRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.RoomRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "data.insert")
//chạy xong lần đầu có dữ liệu rồi thì comment @Component
public class DataTest implements CommandLineRunner {

    @Autowired
    UserRepository userRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    InboxMessageRepository inboxMessageRepository;

    @Autowired
    InboxRepository inboxRepository;

    @Autowired
    ReadTrackingRepository readTrackingRepository;

    @Autowired
    FriendRepository friendRepository;

    @Autowired
    FriendRequestRepository friendRequestRepository;

    private final Random random = new Random();

    private Long time = 1629451079000L;

    private static final Logger logger = Logger.getLogger(DataTest.class.getName());

    private final List<String> images = List.of(
            "https://timesofindia.indiatimes.com/photo/67586673.cms",
            "https://img.poki.com/cdn-cgi/image/quality=78,width=600,height=600,fit=cover,g=0.5x0.5,f=auto/b5bd34054bc849159d949d50021d8926.png",
            "https://images-na.ssl-images-amazon.com/images/I/81BES%2BtsVvL.png",
            "https://i.guim.co.uk/img/media/c9b0aad22638133aa06cd68347bed2390b555e63/0_477_2945_1767/master/2945.jpg?width=1200&height=1200&quality=85&auto=format&fit=crop&s=97bf92d90f51da7067d00f8156512925",
            "https://img.webmd.com/dtmcms/live/webmd/consumer_assets/site_images/article_thumbnails/other/scoop_on_cat_poop_other/1800x1200_scoop_on_cat_poop_other.jpg?resize=600px:*"
    );

    private final List<ReactionType> reactionTypes = List.of(ReactionType.SAD,
            ReactionType.WOW,
            ReactionType.LIKE,
            ReactionType.LOVE,
            ReactionType.HAHA,
            ReactionType.ANGRY);

    @Override
    public void run(String... args) {

        insertUser();

//        insertRoom();

//        insertInbox();

//        insertMessage();

//        insertInboxMessage();

        insertFriend();

        insertMoreUser();

        insertFriendRequest();

        logger.log(Level.INFO, "------insert ok------");

    }

    private void insertMoreUser() {
        for (int i = 0; i < 20; i++) {
            var username = "user" + i + 1;
            userRepository.save(User.builder()
                    .displayName(username)
                    .username(username)
                    .email("hoanghuuhuy@gmail.com")
                    .phoneNumber("0961516945")
                    .password("$2a$12$TynjW4UAUd2993t5.Rh.X.B/9JU5W6csDFeauOIDjWM8G9cnVdSfO")
                    .gender("Nam")
                    .dateOfBirth(new Date())
                    .block(false)
                    .imageUrl(images.get(randomInRange(0, 4)))
                    .roles("ROLE_USER")
                    .enable(true)
                    .verificationCode("123456")
                    .refreshToken("$2a$12$TynjW4UAUd2993t5.Rh.X.B/9JU5W6csDFeauOIDjWM8G9cnVdSfO")
                    .onlineStatus(OnlineStatus.OFFLINE)
                    .lastOnline(new Date())
                    .build());
        }
    }

    private void insertFriendRequest() {
        List<User> users = userRepository.findAll();
        for (int i = 4; i < users.size() - 1; i++) {
            for (int j = i; j < users.size(); j++) {
                friendRequestRepository.save(FriendRequest.builder()
                        .fromId(users.get(i).getId())
                        .toId(users.get(randomInRange(0, 4)).getId())
                        .build());
            }
        }
    }

    private void insertFriend() {
        List<User> users = userRepository.findAll();
        for (User u : users) {
            List<Integer> ids = new ArrayList<>();
            ids.add(Integer.valueOf(u.getId()));
            for (var i = 0; i < 4; i++) {
                var randomId = randomNotDuplicate(5, ids);
                var friend = Friend.builder()
                        .userId(u.getId())
                        .friendId(randomId + "")
                        .build();
                ids.add(randomId);
                friendRepository.save(friend);
            }
        }
    }

   /* private void insertReadTracking() {
        List<Room> rooms = roomRepository.findAll();
        for (Room room : rooms) {
            var lastMessage = messageRepository.getLastMessageOfRoom(room.getId());
            for (Member member : room.getMembers()) {
                var tracking = ReadTracking.builder()
                        .roomId(room.getId())
                        .messageId(lastMessage.getId())
                        .unReadMessage(randomInRange(0, 30))
                        .userId(member.getUserId())
                        .build();
                readTrackingRepository.save(tracking);
            }
        }
    }*/

    private void insertInboxMessage() {
        var count = 1L;
        List<Inbox> inboxList = inboxRepository.findAll();
        List<Message> messageList = messageRepository.findAll();
        for (Inbox inbox : inboxList) {
            for (Message message : messageList) {
                inboxMessageRepository.save(InboxMessage
                        .builder()
                        .id((count++) + "")
                        .inboxId(inbox.getId())
                        .messageId(message.getId())
                        .messageCreateAt(message.getCreateAt())
                        .build());
            }
        }
    }

    private void insertMessage() {
        long count = 1;
        for (Room room : roomRepository.findAll()) {
            ArrayList<Member> members = new ArrayList<>(room.getMembers());
            for (var i = 0; i < 50; i++) {
                int sizeMembers = members.size();
                String senderId = members.get(randomInRange(0, sizeMembers - 1)).getUserId();
                List<Reaction> reactions = new ArrayList<>();
                for (var ii = 0; ii < randomInRange(0, 5); ii++) {
                    reactions.add(Reaction
                            .builder()
                            .type(reactionTypes.get(randomInRange(0, 4)))
                            .reactByUserId(members.get(randomInRange(0, sizeMembers - 1)).getUserId())
                            .build());
                }
                var message = Message
                        .builder()
                        .id((count) + "")
                        .roomId(room.getId())
                        .senderId(senderId)
                        .createAt(new Date(time))
                        .type(MessageType.TEXT)
                        .content((count++) + ". Lorem Ipsum is simply dummy text of the printing and typesetting industry."
                                + UUID.randomUUID().toString()
                                + " Lorem Ipsum has been the industry's standard dummy text ever since the 1500s")
                        .reactions(reactions)
                        .deleted(false)
                        .build();
                time += 1000;
                messageRepository.save(message);
            }
            insertSystemMessage(room);
        }
        insertImageMessage();
        insertVideoMessage();
    }

    private void insertSystemMessage(Room room) {
        var message = Message
                .builder()
                .roomId(room.getId())
                .createAt(new Date(time))
                .type(MessageType.SYSTEM)
                .content("Các bạn giờ đã là bạn bè")
                .build();
        time += 1000;
        messageRepository.save(message);
    }

    void insertImageMessage() {
        String senderId = randomInRange(1, 4) + "";
        List<Reaction> reactions = new ArrayList<>();
        for (var ii = 0; ii < randomInRange(0, 5); ii++) {
            reactions.add(Reaction
                    .builder()
                    .type(reactionTypes.get(randomInRange(0, 4)))
                    .reactByUserId(randomInRange(1, 4) + "")
                    .build());
        }
        var message = Message
                .builder()
                .roomId("2")
                .senderId(senderId)
                .createAt(new Date(time))
                .type(MessageType.IMAGE)
                .content("https://chatappmongo.s3.ap-southeast-1.amazonaws.com/1630819495838-20201226_055434.jpg")
                .reactions(reactions)
                .deleted(false)
                .build();
        time += 1000;
        messageRepository.save(message);
    }

    void insertVideoMessage() {
        String senderId = randomInRange(1, 4) + "";
        List<Reaction> reactions = new ArrayList<>();
        for (var ii = 0; ii < randomInRange(0, 5); ii++) {
            reactions.add(Reaction
                    .builder()
                    .type(reactionTypes.get(randomInRange(0, 4)))
                    .reactByUserId(randomInRange(1, 4) + "")
                    .build());
        }
        var message = Message
                .builder()
                .roomId("2")
                .senderId(senderId)
                .createAt(new Date(time))
                .type(MessageType.VIDEO)
                .content("https://chatappmongo.s3.ap-southeast-1.amazonaws.com/Landscapes-+Volume+4K+(UHD)_Trim.mp4")
                .reactions(reactions)
                .deleted(false)
                .build();
        time += 1000;
        messageRepository.save(message);
    }

    private void insertInbox() {
        var j = 1L;
        for (Room room : roomRepository.findAll()) {
            for (Member m : room.getMembers()) {
                inboxRepository.save(
                        Inbox.builder()
                                .id((j++) + "")
                                .ofUserId(m.getUserId())
                                .roomId(room.getId())
                                .empty(false)
                                .build()
                );
            }
        }
    }

    private void insertRoom() {
        // thêm room chat
        for (var i = 1; i <= 40; i++) {
            // room chat 1-1
            if (i % 2 == 1) {
                List<Integer> ids = new ArrayList<>();
                for (var k = 0; k < 2; k++) {
                    ids.add(randomNotDuplicate(5, ids));
                }
                roomRepository.save(Room.builder()
                        .id(i + "")
                        .members(ids.stream().map(x ->
                                Member.builder().userId(x + "")
                                        .build())
                                .collect(Collectors.toSet()))
                        .type(RoomType.ONE)
                        .build());
            } else {
                // room chat group
                Integer createByUserId = randomInRange(1, 5);
                List<Integer> ids = new ArrayList<>();
                ids.add(createByUserId);
                for (var k = 0; k < randomInRange(2, 4); k++) {
                    ids.add(randomNotDuplicate(5, ids));
                }
                Set<Member> members = ids.stream()
                        .map(x -> Member.builder().userId(x + "").build())
                        .collect(Collectors.toSet());
                for (Member m : members) {
                    if (! m.getUserId().equals(createByUserId.toString())) {
                        m.setAddByUserId(createByUserId + "");
                        m.setAddTime(new Date(time));
                        time += 1000L;
                    }
                }
                roomRepository.save(Room.builder()
                        .id(i + "")
                        .name("Nhóm chat " + i)
                        .members(members)
                        .type(RoomType.GROUP)
                        .createByUserId(createByUserId + "")
                        .imageUrl(images.get(randomInRange(0, 4)))
                        .build());
            }
        }
    }

    private void insertUser() {
        userRepository.save(User.builder()
                .id("1")
                .displayName("Mai Kiên Cường")
                .username("maikiencuong")
                .email("maikiencuongiuh@gmail.com")
                .phoneNumber("0961516941")
                .password("$2a$12$TynjW4UAUd2993t5.Rh.X.B/9JU5W6csDFeauOIDjWM8G9cnVdSfO")
                .gender("Nam")
                .dateOfBirth(new Date())
                .block(false)
                .imageUrl("https://timesofindia.indiatimes.com/photo/67586673.cms")
                .roles("ROLE_USER")
                .enable(true)
                .verificationCode("123456")
                .refreshToken("$2a$12$TynjW4UAUd2993t5.Rh.X.B/9JU5W6csDFeauOIDjWM8G9cnVdSfO")
                .onlineStatus(OnlineStatus.OFFLINE)
                .lastOnline(new Date())
                .build());

        userRepository.save(User.builder()
                .id("2")
                .displayName("Trương Công Cường")
                .username("truongcongcuong")
                .email("truongcongcuong@gmail.com")
                .phoneNumber("0961516942")
                .password("$2a$12$TynjW4UAUd2993t5.Rh.X.B/9JU5W6csDFeauOIDjWM8G9cnVdSfO")
                .gender("Nam")
                .dateOfBirth(new Date())
                .block(false)
                .imageUrl("https://img.poki.com/cdn-cgi/image/quality=78,width=600,height=600,fit=cover,g=0.5x0.5,f=auto/b5bd34054bc849159d949d50021d8926.png")
                .roles("ROLE_USER")
                .enable(true)
                .verificationCode("123456")
                .refreshToken("$2a$12$TynjW4UAUd2993t5.Rh.X.B/9JU5W6csDFeauOIDjWM8G9cnVdSfO")
                .onlineStatus(OnlineStatus.OFFLINE)
                .lastOnline(new Date())
                .build());

        userRepository.save(User.builder()
                .id("3")
                .displayName("Nguyễn Công Thành Đạt")
                .username("nguyencongthanhdat")
                .email("nguyencongthanhdat@gmail.com")
                .phoneNumber("0961516943")
                .password("$2a$12$TynjW4UAUd2993t5.Rh.X.B/9JU5W6csDFeauOIDjWM8G9cnVdSfO")
                .gender("Nam")
                .dateOfBirth(new Date())
                .block(false)
                .imageUrl("https://images-na.ssl-images-amazon.com/images/I/81BES%2BtsVvL.png")
                .roles("ROLE_USER")
                .enable(true)
                .verificationCode("123456")
                .refreshToken("$2a$12$TynjW4UAUd2993t5.Rh.X.B/9JU5W6csDFeauOIDjWM8G9cnVdSfO")
                .onlineStatus(OnlineStatus.OFFLINE)
                .lastOnline(new Date())
                .build());

        userRepository.save(User.builder()
                .id("4")
                .displayName("Lưu Tuấn Kha")
                .username("luutuankha")
                .email("luutuankha@gmail.com")
                .phoneNumber("0961516944")
                .password("$2a$12$TynjW4UAUd2993t5.Rh.X.B/9JU5W6csDFeauOIDjWM8G9cnVdSfO")
                .gender("Nam")
                .dateOfBirth(new Date())
                .block(false)
                .imageUrl("https://i.guim.co.uk/img/media/c9b0aad22638133aa06cd68347bed2390b555e63/0_477_2945_1767/master/2945.jpg?width=1200&height=1200&quality=85&auto=format&fit=crop&s=97bf92d90f51da7067d00f8156512925")
                .roles("ROLE_USER")
                .enable(true)
                .verificationCode("123456")
                .refreshToken("$2a$12$TynjW4UAUd2993t5.Rh.X.B/9JU5W6csDFeauOIDjWM8G9cnVdSfO")
                .onlineStatus(OnlineStatus.OFFLINE)
                .lastOnline(new Date())
                .build());

        userRepository.save(User.builder()
                .id("5")
                .displayName("Hoàng Hữu Huy")
                .username("hoanghuuhuy")
                .email("hoanghuuhuy@gmail.com")
                .phoneNumber("0961516945")
                .password("$2a$12$TynjW4UAUd2993t5.Rh.X.B/9JU5W6csDFeauOIDjWM8G9cnVdSfO")
                .gender("Nam")
                .dateOfBirth(new Date())
                .block(false)
                .imageUrl("https://img.webmd.com/dtmcms/live/webmd/consumer_assets/site_images/article_thumbnails/other/scoop_on_cat_poop_other/1800x1200_scoop_on_cat_poop_other.jpg?resize=600px:*")
                .roles("ROLE_USER")
                .enable(true)
                .verificationCode("123456")
                .refreshToken("$2a$12$TynjW4UAUd2993t5.Rh.X.B/9JU5W6csDFeauOIDjWM8G9cnVdSfO")
                .onlineStatus(OnlineStatus.OFFLINE)
                .lastOnline(new Date())
                .build());
    }

    private Integer randomNotDuplicate(int bound, List<Integer> inputToNotDuplicateArray) {
        int i = random.nextInt(bound) + 1;
        while (inputToNotDuplicateArray.contains(i)) {
            i = random.nextInt(bound) + 1;
        }
        return i;
    }

    private int randomInRange(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

  /*  Collation collation = Collation.of("de");
    AggregationOptions options = AggregationOptions.builder().build();

    var aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("inboxId").is("1")),
            Aggregation.sort(Sort.Direction.DESC, "message.createAt"),
            Aggregation.lookup("message", "message.id", "_id", "messageResults"),
            Aggregation.project("messageResults"),
            Aggregation.unwind("messageResults"),
            Aggregation.replaceRoot("messageResults")
    );

    AggregationResults<Message> results = mongoTemplate.aggregate(aggregation, "inbox_message", Message.class);
        System.out.println(results.getMappedResults());*/

    public void insertFewRowData() {
        Set<Member> members = new HashSet<>();
        members.add(Member.builder().isAdmin(true).userId("1").build());
        members.add(Member.builder().userId("2").addByUserId("1").addTime(new Date(time += 1000)).build());
        members.add(Member.builder().userId("3").addByUserId("1").addTime(new Date(time += 1000)).build());
        members.add(Member.builder().userId("4").addByUserId("1").addTime(new Date(time += 1000)).build());
        members.add(Member.builder().userId("5").addByUserId("1").addTime(new Date(time += 1000)).build());
        var room1 = Room.builder()
                .type(RoomType.GROUP)
                .imageUrl(images.get(randomInRange(0, 4)))
                .createByUserId("1")
                .name("Nhóm chat 1")
                .members(members)
                .build();
        roomRepository.save(room1);

        Set<Member> members2 = new HashSet<>();
        members2.add(Member.builder().userId("1").build());
        members2.add(Member.builder().userId("2").build());
        var room2 = Room.builder()
                .members(members2)
                .type(RoomType.ONE)
                .build();
        roomRepository.save(room2);

        for (Member m : room1.getMembers()) {
            inboxRepository.save(Inbox.builder().roomId(room1.getId()).ofUserId(m.getUserId()).build());
        }

        for (Member m : room2.getMembers()) {
            inboxRepository.save(Inbox.builder().roomId(room2.getId()).ofUserId(m.getUserId()).build());
        }

        messageRepository.save(Message.builder()
                .roomId(room1.getId())
                .content("Hai bạn giờ đã là bạn bè")
                .type(MessageType.SYSTEM)
                .build());
        for (int i = 0; i < 5; i++) {
            messageRepository.save(Message.builder()
                    .content(room1.getId() + "_hello " + i + 1)
                    .senderId("1")
                    .type(MessageType.TEXT)
                    .roomId(room1.getId())
                    .build());
        }

        messageRepository.save(Message.builder()
                .roomId(room2.getId())
                .content("Hai bạn giờ đã là bạn bè")
                .type(MessageType.SYSTEM)
                .build());
        for (int i = 0; i < 2; i++) {
            messageRepository.save(Message.builder()
                    .content(room2.getId() + "_hello " + i + 1)
                    .senderId("1")
                    .type(MessageType.TEXT)
                    .roomId(room2.getId())
                    .build());
        }

        List<Message> messages = messageRepository.findAll();
        List<Inbox> inboxes = inboxRepository.findAll();
        for (int i = 0; i < 7; i++) {
            inboxMessageRepository.save(InboxMessage.builder()
                    .messageCreateAt(messages.get(i).getCreateAt())
                    .messageId(messages.get(i).getId())
                    .inboxId(inboxes.get(i).getId())
                    .build());

        }

       /* for (Room room : roomRepository.findAll()) {
            for (Member member : room.getMembers()) {
                readTrackingRepository.save(ReadTracking.builder()
                        .roomId(room.getId())
                        .userId(member.getUserId())
                        .messageId(messageRepository.getLastMessageOfRoom(room.getId()).getId())
                        .readAt(new Date(time += 1000))
                        .unReadMessage(randomInRange(1, 10))
                        .build());
            }
        }*/

        friendRepository.save(Friend.builder().userId("1").friendId("2").build());
        friendRepository.save(Friend.builder().userId("2").friendId("1").build());

        Set<Member> members3 = new HashSet<>();
        members3.add(Member.builder().userId("1").build());
        members3.add(Member.builder().userId("3").build());
        var room3 = Room.builder()
                .members(members3)
                .type(RoomType.ONE)
                .build();
        roomRepository.save(room3);

        for (Member m : room3.getMembers()) {
            inboxRepository.save(Inbox.builder().roomId(room3.getId()).ofUserId(m.getUserId()).build());
        }
        messageRepository.save(Message.builder()
                .roomId(room3.getId())
                .content("Hai bạn giờ đã là bạn bè")
                .type(MessageType.SYSTEM)
                .build());
        friendRepository.save(Friend.builder().userId("1").friendId("3").build());
        friendRepository.save(Friend.builder().userId("3").friendId("1").build());

        Set<Member> members4 = new HashSet<>();
        members4.add(Member.builder().userId("1").build());
        members4.add(Member.builder().userId("4").build());
        var room4 = Room.builder()
                .members(members4)
                .type(RoomType.ONE)
                .build();
        roomRepository.save(room4);

        for (Member m : room4.getMembers()) {
            inboxRepository.save(Inbox.builder().roomId(room4.getId()).ofUserId(m.getUserId()).build());
        }
        messageRepository.save(Message.builder()
                .roomId(room4.getId())
                .content("Hai bạn giờ đã là bạn bè")
                .type(MessageType.SYSTEM)
                .build());
        friendRepository.save(Friend.builder().userId("1").friendId("4").build());
        friendRepository.save(Friend.builder().userId("4").friendId("1").build());

        friendRequestRepository.save(FriendRequest.builder().fromId("5").toId("1").build());

        friendRequestRepository.save(FriendRequest.builder().fromId("4").toId("2").build());
        friendRequestRepository.save(FriendRequest.builder().fromId("2").toId("3").build());

        friendRequestRepository.save(FriendRequest.builder().fromId("5").toId("2").build());
    }
}
