package iuh.dhktpm14.cnm.chatappmongo;

import iuh.dhktpm14.cnm.chatappmongo.entity.Friend;
import iuh.dhktpm14.cnm.chatappmongo.entity.FriendRequest;
import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.InboxMessage;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
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
import iuh.dhktpm14.cnm.chatappmongo.util.Utils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    @Value("${data.path:src/main/resources/DHKTPM14A_Danhsach.xlsx}")
    private String dataPathFile;

    private final List<String> images = List.of(
            "https://timesofindia.indiatimes.com/photo/67586673.cms",
            "https://img.poki.com/cdn-cgi/image/quality=78,width=600,height=600,fit=cover,g=0.5x0.5,f=auto/b5bd34054bc849159d949d50021d8926.png",
            "https://images-na.ssl-images-amazon.com/images/I/81BES%2BtsVvL.png",
            "https://i.guim.co.uk/img/media/c9b0aad22638133aa06cd68347bed2390b555e63/0_477_2945_1767/master/2945.jpg?width=1200&height=1200&quality=85&auto=format&fit=crop&s=97bf92d90f51da7067d00f8156512925",
            "https://img.webmd.com/dtmcms/live/webmd/consumer_assets/site_images/article_thumbnails/other/scoop_on_cat_poop_other/1800x1200_scoop_on_cat_poop_other.jpg?resize=600px:*",
            "https://lifetimemix.com/wp-content/uploads/2021/06/1800x1200_cat_relaxing_on_patio_other.jpg",
            "https://i.natgeofe.com/n/3861de2a-04e6-45fd-aec8-02e7809f9d4e/02-cat-training-NationalGeographic_1484324_square.jpg",
            "https://hips.hearstapps.com/hmg-prod.s3.amazonaws.com/images/close-up-of-cat-wearing-sunglasses-while-sitting-royalty-free-image-1571755145.jpg",
            "https://th-thumbnailer.cdn-si-edu.com/bZAar59Bdm95b057iESytYmmAjI=/1400x1050/filters:focal(594x274:595x275)/https://tf-cmsv2-smithsonianmag-media.s3.amazonaws.com/filer/95/db/95db799b-fddf-4fde-91f3-77024442b92d/egypt_kitty_social.jpg",
            "https://media.npr.org/assets/img/2021/08/11/gettyimages-1279899488_wide-f3860ceb0ef19643c335cb34df3fa1de166e2761-s1100-c50.jpg",
            "https://kitcat.com.sg/wp-content/uploads/2020/07/Kit-Cat.png",
            "https://cdn.britannica.com/q:60/91/181391-050-1DA18304/cat-toes-paw-number-paws-tiger-tabby.jpg",
            "https://images.unsplash.com/photo-1615807713086-bfc4975801d0?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxzZWFyY2h8MXx8Y2F0JTIwZmFjZXxlbnwwfHwwfHw%3D&w=1000&q=80",
            "https://images.theconversation.com/files/350865/original/file-20200803-24-50u91u.jpg?ixlib=rb-1.1.0&q=45&auto=format&w=1200&h=1200.0&fit=crop",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ5a5UAuYWM7rnRmTUFzSt137Dk19FrzvEGKA&usqp=CAU"
    );

    private final List<ReactionType> reactionTypes = List.of(ReactionType.SAD,
            ReactionType.WOW,
            ReactionType.LIKE,
            ReactionType.LOVE,
            ReactionType.HAHA,
            ReactionType.ANGRY);

    private List<User> users = new ArrayList<>();

    @Override
    public void run(String... args) {

        insertUser();

        if (! users.isEmpty()) {
            insertFriend();

            insertFriendRequest();

            insertOneGroup();
        }

        logger.log(Level.INFO, "------insert ok------");

    }

    private void insertOneGroup() {
        String createByUser = "25";
        Set<Member> members = new HashSet<>();
        List<Inbox> inboxes = new ArrayList<>();
        for (int i = 20; i < 70; i++) {
            members.add(Member.builder().userId(users.get(i).getId()).build());
        }
        for (Member m : members) {
            if (! m.getUserId().equals(createByUser)) {
                m.setAddByUserId(createByUser);
                m.setAddTime(new Date(time));
                time += 1000L;
            }
        }
        Room room = roomRepository.save(Room.builder()
                .id("1")
                .name("Lớp DHKTPM 14")
                .members(members)
                .type(RoomType.GROUP)
                .createAt(new Date())
                .createByUserId(createByUser)
                .imageUrl(images.get(randomInRange(0, images.size() - 1)))
                .build());
        int count = 1;
        for (Member m : room.getMembers()) {
            Inbox ib = Inbox.builder()
                    .id(String.valueOf(count))
                    .ofUserId(m.getUserId())
                    .roomId(room.getId())
                    .empty(false)
                    .lastTime(new Date())
                    .build();
            inboxes.add(ib);
            inboxRepository.save(ib);
            count++;
        }
        var message = Message
                .builder()
                .roomId(room.getId())
                .createAt(new Date(time))
                .type(MessageType.SYSTEM)
                .content("Mai Kiên Cường đã tạo nhóm. Hãy trò chuyện cùng nhau.")
                .build();
        time += 1000;
        messageRepository.save(message);
        count = 1;
        for (Inbox ibox : inboxes) {
            inboxMessageRepository.save(InboxMessage
                    .builder()
                    .id(String.valueOf(count))
                    .inboxId(ibox.getId())
                    .messageId(message.getId())
                    .messageCreateAt(message.getCreateAt())
                    .build());
            count++;
        }
    }

    private void insertFriendRequest() {
        int count = 1;
        for (int i = 0; i < 29; i++) {
            User u1 = users.get(i);
            for (int j = 30; j < 45; j++) {
                User u2 = users.get(j);
                friendRequestRepository.save(FriendRequest.builder()
                        .createAt(new Date(time += 10000L))
                        .id(String.valueOf(count))
                        .fromId(u1.getId())
                        .toId(u2.getId())
                        .build());
                count++;
            }
        }
        for (int i = 0; i < 29; i++) {
            User u1 = users.get(i);
            for (int j = 45; j < 60; j++) {
                User u2 = users.get(j);
                friendRequestRepository.save(FriendRequest.builder()
                        .id(String.valueOf(count))
                        .createAt(new Date(time += 10000L))
                        .fromId(u2.getId())
                        .toId(u1.getId())
                        .build());
                count++;
            }
        }
    }

    private void insertFriend() {
        int count = 1;
        for (int i = 0; i < 29; i++) {
            User u1 = users.get(i);
            for (int j = i + 1; j < 30; j++) {
                User u2 = users.get(j);
                var friend = Friend.builder()
                        .id(String.valueOf(count))
                        .userId(u1.getId())
                        .createAt(new Date())
                        .friendId(u2.getId())
                        .build();
                friendRepository.save(friend);
                count++;
                var friend2 = Friend.builder()
                        .id(String.valueOf(count))
                        .userId(u2.getId())
                        .createAt(new Date())
                        .friendId(u1.getId())
                        .build();
                friendRepository.save(friend2);
                count++;
            }
        }
    }

    private void insertUser() {
        List<String> notUsername = new ArrayList<>();
        notUsername.add("maikiencuong");
        notUsername.add("truongcongcuong");
        notUsername.add("nguyencongthanhdat");
        notUsername.add("luutuankha");
        int i = 1;
        try {
//            Resource resource=new ClassPathResource("DHKTPM14A_Danhsach.xlsx");
            File f = new File(dataPathFile);
            FileInputStream file = new FileInputStream(f);
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Cell cellName = row.getCell(1);
                Cell cellPhone = row.getCell(3);
                Cell cellGender = row.getCell(6);
                if (cellName != null) {
                    String name = cellName.getStringCellValue();
                    Date date = new Date(time += 10000000);
                    User user = User.builder()
                            .id(String.valueOf(i))
                            .displayName(name)
                            .phoneNumber(cellPhone.getStringCellValue())
                            .password("$2a$12$TynjW4UAUd2993t5.Rh.X.B/9JU5W6csDFeauOIDjWM8G9cnVdSfO")
                            .gender(cellGender.getStringCellValue())
                            .dateOfBirth(date)
                            .block(false)
                            .imageUrl(images.get(randomInRange(0, images.size() - 1)))
                            .roles("ROLE_USER")
                            .enable(true)
                            .onlineStatus(OnlineStatus.OFFLINE)
                            .lastOnline(date)
                            .createAt(date)
                            .build();
                    i++;
                    String username = Utils.removeAccent(name).replaceAll("\\s+", "");
                    if (! notUsername.contains(username.toLowerCase()))
                        user.setUsername(username);
                    user.setEmail(username + "@gmail.com");
                    users.add(user);
                    userRepository.save(user);
                }
            }
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

}
