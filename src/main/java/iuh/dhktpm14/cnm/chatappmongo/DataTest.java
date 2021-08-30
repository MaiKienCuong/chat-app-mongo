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

    @Override
    public void run(String... args) {

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
                        Member.builder().userId("4").addByUserId("2").addTime(new Date(new Date().getTime()+4000)).build(),
                        Member.builder().userId("5").addByUserId("2").addTime(new Date(new Date().getTime()+4000)).build()))
                .type(RoomType.GROUP)
                .createByUserId("1")
                .imageUrl("https://timesofindia.indiatimes.com/photo/67586673.cms")
                .build());

        //
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
                .id("7")
                .roomId("1")
                .content("ừ, hello nha1")
                .senderId("2")
                .type("text")
                .createAt(new Date(new Date().getTime()+20000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+2000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("1").type(ReactionType.SAD).build()))
                .build());

        messageRepository.save(Message.builder()
                .id("8")
                .roomId("1")
                .content("ừ, hello nha2")
                .senderId("2")
                .type("text")
                .createAt(new Date(new Date().getTime()+20000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+2000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("1").type(ReactionType.SAD).build()))
                .build());

        messageRepository.save(Message.builder()
                .id("9")
                .roomId("1")
                .content("ừ, hello nha3")
                .senderId("2")
                .type("text")
                .createAt(new Date(new Date().getTime()+20000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+2000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("1").type(ReactionType.SAD).build()))
                .build());

        messageRepository.save(Message.builder()
                .id("10")
                .roomId("1")
                .content("ừ, hello nha4")
                .senderId("2")
                .type("text")
                .createAt(new Date(new Date().getTime()+20000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+2000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("1").type(ReactionType.SAD).build()))
                .build());

        messageRepository.save(Message.builder()
                .id("11")
                .roomId("1")
                .content("chào nha1")
                .senderId("1")
                .type("text")
                .createAt(new Date(new Date().getTime()+20000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+2000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("1").type(ReactionType.SAD).build()))
                .build());

        messageRepository.save(Message.builder()
                .id("12")
                .roomId("1")
                .content("chào nha2")
                .senderId("1")
                .type("text")
                .createAt(new Date(new Date().getTime()+20000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+2000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("1").type(ReactionType.SAD).build()))
                .build());

        messageRepository.save(Message.builder()
                .id("13")
                .roomId("1")
                .content("chào nha3")
                .senderId("1")
                .type("text")
                .createAt(new Date(new Date().getTime()+20000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+2000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("1").type(ReactionType.SAD).build()))
                .build());

        messageRepository.save(Message.builder()
                .id("14")
                .roomId("1")
                .content("chào nha4")
                .senderId("1")
                .type("text")
                .createAt(new Date(new Date().getTime()+20000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+2000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("1").type(ReactionType.SAD).build()))
                .build());
        //
        messageRepository.save(Message.builder()
                .id("3")
                .roomId("2")
                .content("chào nha")
                .senderId("1")
                .type("text")
                .createAt(new Date(new Date().getTime()+30000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("2").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("3").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("4").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("5").readAt(new Date(new Date().getTime()+4000)).build()))
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
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("2").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("3").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("4").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("5").readAt(new Date(new Date().getTime()+4000)).build()))
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
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("2").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("3").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("4").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("5").readAt(new Date(new Date().getTime()+4000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("2").type(ReactionType.HAHA).build()))
                .build());

        messageRepository.save(Message.builder()
                .id("15")
                .roomId("2")
                .content("chào nha15")
                .senderId("3")
                .type("text")
                .createAt(new Date(new Date().getTime()+50000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("2").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("3").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("4").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("5").readAt(new Date(new Date().getTime()+4000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("2").type(ReactionType.HAHA).build()))
                .build());

        messageRepository.save(Message.builder()
                .id("16")
                .roomId("2")
                .content("chào nha16")
                .senderId("3")
                .type("text")
                .createAt(new Date(new Date().getTime()+50000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("2").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("3").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("4").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("5").readAt(new Date(new Date().getTime()+4000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("2").type(ReactionType.HAHA).build()))
                .build());
        messageRepository.save(Message.builder()
                .id("17")
                .roomId("2")
                .content("chào nha17")
                .senderId("3")
                .type("text")
                .createAt(new Date(new Date().getTime()+50000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("2").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("3").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("4").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("5").readAt(new Date(new Date().getTime()+4000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("2").type(ReactionType.HAHA).build()))
                .build());

        messageRepository.save(Message.builder()
                .id("18")
                .roomId("2")
                .content("chào nha18")
                .senderId("1")
                .type("text")
                .createAt(new Date(new Date().getTime()+50000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("2").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("3").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("4").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("5").readAt(new Date(new Date().getTime()+4000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("2").type(ReactionType.HAHA).build()))
                .build());

        messageRepository.save(Message.builder()
                .id("19")
                .roomId("2")
                .content("chào nha19")
                .senderId("1")
                .type("text")
                .createAt(new Date(new Date().getTime()+50000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("2").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("3").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("4").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("5").readAt(new Date(new Date().getTime()+4000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("2").type(ReactionType.HAHA).build()))
                .build());

        messageRepository.save(Message.builder()
                .id("19")
                .roomId("2")
                .content("chào nha19")
                .senderId("1")
                .type("text")
                .createAt(new Date(new Date().getTime()+50000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("2").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("3").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("4").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("5").readAt(new Date(new Date().getTime()+4000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("2").type(ReactionType.HAHA).build()))
                .build());

        messageRepository.save(Message.builder()
                .id("20")
                .roomId("2")
                .content("chào nha20")
                .senderId("2")
                .type("text")
                .createAt(new Date(new Date().getTime()+50000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("2").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("3").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("4").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("5").readAt(new Date(new Date().getTime()+4000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("2").type(ReactionType.HAHA).build()))
                .build());

        messageRepository.save(Message.builder()
                .id("21")
                .roomId("2")
                .content("chào nha21")
                .senderId("2")
                .type("text")
                .createAt(new Date(new Date().getTime()+50000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("2").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("3").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("4").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("5").readAt(new Date(new Date().getTime()+4000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("2").type(ReactionType.HAHA).build()))
                .build());

        messageRepository.save(Message.builder()
                .id("22")
                .roomId("2")
                .content("chào nha22")
                .senderId("4")
                .type("text")
                .createAt(new Date(new Date().getTime()+50000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("2").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("3").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("4").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("5").readAt(new Date(new Date().getTime()+4000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("2").type(ReactionType.HAHA).build()))
                .build());
        messageRepository.save(Message.builder()
                .id("23")
                .roomId("2")
                .content("chào nha23")
                .senderId("4")
                .type("text")
                .createAt(new Date(new Date().getTime()+50000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("2").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("3").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("4").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("5").readAt(new Date(new Date().getTime()+4000)).build()))
                .reactions(List.of(Reaction.builder().reactByUserId("2").type(ReactionType.HAHA).build()))
                .build());
        messageRepository.save(Message.builder()
                .id("24")
                .roomId("2")
                .content("chào nha24")
                .senderId("4")
                .type("text")
                .createAt(new Date(new Date().getTime()+50000))
                .readByes(Set.of(
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("2").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("3").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("4").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("5").readAt(new Date(new Date().getTime()+4000)).build()))
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
                        ReadBy.builder().readByUserId("1").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("2").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("3").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("4").readAt(new Date(new Date().getTime()+3000)).build(),
                        ReadBy.builder().readByUserId("5").readAt(new Date(new Date().getTime()+4000)).build()))
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

        inboxMessageRepository.save(InboxMessage.builder()
                .id("21")
                .inboxId("1")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("7")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("22")
                .inboxId("1")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("8")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("23")
                .inboxId("1")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("9")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("24")
                .inboxId("1")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("10")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("25")
                .inboxId("1")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("11")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("26")
                .inboxId("1")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("12")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("27")
                .inboxId("1")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("13")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("28")
                .inboxId("1")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("14")
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
        //
        inboxMessageRepository.save(InboxMessage.builder()
                .id("29")
                .inboxId("6")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("15")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("30")
                .inboxId("6")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("16")
                .build());

        inboxMessageRepository.save(InboxMessage.builder()
                .id("31")
                .inboxId("6")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("17")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("32")
                .inboxId("6")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("18")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("33")
                .inboxId("6")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("19")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("34")
                .inboxId("6")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("20")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("35")
                .inboxId("6")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("21")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("36")
                .inboxId("6")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("22")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("37")
                .inboxId("6")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("23")
                .build());
        inboxMessageRepository.save(InboxMessage.builder()
                .id("38")
                .inboxId("6")
                .messageCreateAt(new Date(new Date().getTime()+1000))
                .messageId("24")
                .build());

//        Collation collation = Collation.of("de");
//        AggregationOptions options = AggregationOptions.builder().build();
//
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
    }
}
