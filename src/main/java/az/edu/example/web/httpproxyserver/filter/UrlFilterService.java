package az.edu.example.web.httpproxyserver.filter;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UrlFilterService {
    private final List<String> blockedDomains = List.of("facebook.com", "tiktok.com");

    public boolean isAllowed(String url) {
        return blockedDomains.stream().noneMatch(url::contains);
    }

}
