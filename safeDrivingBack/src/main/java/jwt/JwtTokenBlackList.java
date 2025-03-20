package hello.safedrivingback.jwt;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class JwtTokenBlackList {

    private final Set<String> blackList = new HashSet<>();

    public void addBlackList(String token) {
        blackList.add(token);
    }

    public boolean isBlackListed(String token) {
        return blackList.contains(token);
    }
}
