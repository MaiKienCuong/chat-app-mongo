package iuh.dhktpm14.cnm.chatappmongo.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public interface Utils {
    String REFRESH_TOKEN = "refresh_token";

    /*
    chuyển tiếng việt có dấu thành không dấu
     */
    static String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        var pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("")
                .replace("đ", "d")
                .replace("Đ", "D");
    }

}
