package iuh.dhktpm14.cnm.chatappmongo;

import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.InboxMessage;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import iuh.dhktpm14.cnm.chatappmongo.entity.Reaction;
import iuh.dhktpm14.cnm.chatappmongo.entity.ReadBy;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.ReactionType;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.RoomType;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxMessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.MessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.RoomRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
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


    @Override
    public void run(String... args) {

//        Collation collation = Collation.of("de");
//        AggregationOptions options = AggregationOptions.builder().build();

//        var aggregation = Aggregation.newAggregation(
//                Aggregation.match(Criteria.where("inboxId").is("1")),
//                Aggregation.sort(Sort.Direction.DESC, "message.createAt"),
//                Aggregation.lookup("message", "message.id", "_id", "messageResults"),
//                Aggregation.project("messageResults"),
//                Aggregation.unwind("messageResults"),
//                Aggregation.replaceRoot("messageResults")
//        );
//
//        AggregationResults<Message> results = mongoTemplate.aggregate(aggregation, "inbox_message", Message.class);
//        System.out.println(results.getMappedResults());

        userRepository.save(User.builder()
                .id("1")
                .displayName("Mai Kiên Cường")
                .username("maikiencuong")
                .email("maikiencuongiuh@gmail.com")
                .phoneNumber("0961516941")
                .password("$2a$12$TynjW4UAUd2993t5.Rh.X.B/9JU5W6csDFeauOIDjWM8G9cnVdSfO")
                .gender("Nam")
                .dateOfBirth(new Date())
                .active(true)
                .block(false)
                .imageUrl("https://timesofindia.indiatimes.com/photo/67586673.cms")
                .roles("ROLE_USER")
                .enable(true)
                .verificationCode("123456")
                .refreshToken("$2a$12$TynjW4UAUd2993t5.Rh.X.B/9JU5W6csDFeauOIDjWM8G9cnVdSfO")
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
                .active(true)
                .block(false)
                .imageUrl("https://timesofindia.indiatimes.com/photo/67586673.cms")
                .roles("ROLE_USER")
                .enable(true)
                .verificationCode("123456")
                .refreshToken("$2a$12$TynjW4UAUd2993t5.Rh.X.B/9JU5W6csDFeauOIDjWM8G9cnVdSfO")
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
                .active(true)
                .block(false)
                .imageUrl("https://timesofindia.indiatimes.com/photo/67586673.cms")
                .roles("ROLE_USER")
                .enable(true)
                .verificationCode("123456")
                .refreshToken("$2a$12$TynjW4UAUd2993t5.Rh.X.B/9JU5W6csDFeauOIDjWM8G9cnVdSfO")
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
                .active(true)
                .block(false)
                .imageUrl("https://timesofindia.indiatimes.com/photo/67586673.cms")
                .roles("ROLE_USER")
                .enable(true)
                .verificationCode("123456")
                .refreshToken("$2a$12$TynjW4UAUd2993t5.Rh.X.B/9JU5W6csDFeauOIDjWM8G9cnVdSfO")
                .build());

        roomRepository.save(Room.builder()
                .id("1")
                .members(Set.of(
                        Member.builder().userId("1").build(),
                        Member.builder().userId("2").build()))
                .type(RoomType.ONE)
                .build());

        roomRepository.save(Room.builder()
                .id("2")
                .name("Nhóm Công nghệ phần mềm")
                .members(Set.of(
                        Member.builder().userId("1").addTime(new Date(new Date().getTime()+1000)).build(),
                        Member.builder().userId("2").addByUserId("1").addTime(new Date(new Date().getTime()+2000)).build(),
                        Member.builder().userId("3").addByUserId("2").addTime(new Date(new Date().getTime()+3000)).build(),
                        Member.builder().userId("4").addByUserId("2").addTime(new Date(new Date().getTime()+4000)).build()))
                .type(RoomType.GROUP)
                .createByUserId("1")
                .imageUrl("https://timesofindia.indiatimes.com/photo/67586673.cms")
                .build());

        messageRepository.save(Message.builder()
                .id("1")
                .roomId("1")
                .content("chào nha")
                .senderId("1")
                .type("text")
                .createAt(new Date(new Date().getTime()+10000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("2").readAt(new Date(new Date().getTime()+1000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("2").type(ReactionType.HAHA).build()))
                .build());

        messageRepository.save(Message.builder()
                .id("2")
                .roomId("1")
                .content("ừ, hello nha")
                .senderId("2")
                .type("text")
                .createAt(new Date(new Date().getTime()+20000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+2000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("1").type(ReactionType.SAD).build()))
                .build());

        messageRepository.save(Message.builder()
                .id("3")
                .roomId("2")
                .content("chào nha")
                .senderId("1")
                .type("text")
                .createAt(new Date(new Date().getTime()+30000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("2").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("3").readAt(new Date(new Date().getTime()+4000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("2").type(ReactionType.HAHA).build()))
                .build());

        messageRepository.save(Message.builder()
                .id("4")
                .roomId("2")
                .content("ừ, hello nha")
                .senderId("2")
                .type("text")
                .createAt(new Date(new Date().getTime()+40000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+5000)).build(),
                        ReadBy.builder().readByUserId("3").readAt(new Date(new Date().getTime()+6000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("1").type(ReactionType.SAD).build()))
                .build());

        messageRepository.save(Message.builder()
                .id("5")
                .roomId("2")
                .content("chào nha")
                .senderId("3")
                .type("text")
                .createAt(new Date(new Date().getTime()+50000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("2").readAt(new Date(new Date().getTime()+7000)).build(),
                        ReadBy.builder().readByUserId("3").readAt(new Date(new Date().getTime()+8000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("2").type(ReactionType.HAHA).build()))
                .build());

        messageRepository.save(Message.builder()
                .id("6")
                .roomId("2")
                .content("ừ, hello nha")
                .senderId("4")
                .type("text")
                .createAt(new Date(new Date().getTime()+60000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+9000)).build(),
                        ReadBy.builder().readByUserId("3").readAt(new Date(new Date().getTime()+10000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("1").type(ReactionType.SAD).build()))
                .build());

        inboxRepository.save(Inbox.builder()
                .id("1").empty(false)
                .roomId("1")
                .ofUserId("1")
                .build());

        inboxRepository.save(Inbox.builder()
                .id("2").empty(true)
                .roomId("1")
                .ofUserId("2")
                .build());
//
        inboxRepository.save(Inbox.builder()
                .id("3").empty(false)
                .roomId("2")
                .ofUserId("1")
                .build());

        inboxRepository.save(Inbox.builder()
                .id("4").empty(false)
                .roomId("2")
                .ofUserId("2")
                .build());

        inboxRepository.save(Inbox.builder()
                .id("5").empty(false)
                .roomId("2")
                .ofUserId("3")
                .build());

        inboxRepository.save(Inbox.builder()
                .id("6").empty(false)
                .roomId("2")
                .ofUserId("4")
                .build());

        //
        inboxMessageRepository.save(InboxMessage.builder()
                .id("1")
                .inboxId("1")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("1")
                .build());

        inboxMessageRepository.save(InboxMessage.builder()
                .id("2")
                .inboxId("1")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("2")
                .build());

        inboxMessageRepository.save(InboxMessage.builder()
                .id("3")
                .inboxId("2")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("1")
                .build());

        inboxMessageRepository.save(InboxMessage.builder()
                .id("4")
                .inboxId("2")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("2")
                .build());
        //
        inboxMessageRepository.save(InboxMessage.builder()
                .id("5")
                .inboxId("3")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("3")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("6")
                .inboxId("3")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("4")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("7")
                .inboxId("3")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("5")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("8")
                .inboxId("3")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("6")
                .build());
        //
        inboxMessageRepository.save(InboxMessage.builder()
                .id("9")
                .inboxId("4")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("3")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("10")
                .inboxId("4")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("4")
                .build());

        inboxMessageRepository.save(InboxMessage.builder()
                .id("11")
                .inboxId("4")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("5")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("12")
                .inboxId("4")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("6")
                .build());
//
        inboxMessageRepository.save(InboxMessage.builder()
                .id("13")
                .inboxId("5")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("3")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("14")
                .inboxId("5")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("4")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("15")
                .inboxId("5")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("5")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("16")
                .inboxId("5")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("6")
                .build());
        //
        inboxMessageRepository.save(InboxMessage.builder()
                .id("17")
                .inboxId("6")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("3")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("18")
                .inboxId("6")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("4")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("19")
                .inboxId("6")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("5")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("20")
                .inboxId("6")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("6")
                .build());

//        System.out.println(roomRepository.findAllByUserId("1", Pageable.unpaged()));
//        System.out.println(roomRepository.findCommonRoomBetween("1", "2", PageRequest.of(0, 1)));
//        System.out.println(roomRepository.findCommonGroupBetween("1", "2"));
//        System.out.println(roomRepository.findCommonGroupBetween("1", "3"));
//        System.out.println(roomRepository.findCommonGroupBetween("2", "3"));
//
//        System.out.println(messageRepository.findAllByRoomId("1", PageRequest.of(0, 1)));
//        System.out.println(messageRepository.findAllByRoomId("1", Pageable.unpaged()));
//        System.out.println(messageRepository.countNewMessage("4", "1"));
//        System.out.println(messageRepository.countNewMessage("4", "4"));
    }

}
