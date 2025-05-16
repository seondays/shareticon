package seondays.shareticon.group;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class RandomCodeFactory {

    private final SecureRandom secureRandom = new SecureRandom();
    private static final String CHARSET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 10;

    public String createInviteCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARSET.charAt(secureRandom.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }
}
