package az.edu.example.web.httpproxyserver.service;

import az.edu.example.web.httpproxyserver.cache.ProxyCache;
import az.edu.example.web.httpproxyserver.filter.UrlFilterService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

@Service
public class ProxyService {

    private final UrlFilterService filterService;
    private final ProxyCache proxyCache;

    public ProxyService(UrlFilterService filterService, ProxyCache proxyCache) {
        this.filterService = filterService;
        this.proxyCache = proxyCache;
    }

    public ResponseEntity<?> forwardRequest(String targetUrl, HttpMethod method, String body, HttpServletRequest originalRequest) {
        System.out.println("[" + method + "] Forwarding to: " + targetUrl);

        // Only cache GET requests (common pattern)
        boolean isCacheable = method == HttpMethod.GET;

        if (!filterService.isAllowed(targetUrl)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header("X-Proxy-Cache", "BLOCKED")
                    .body("This URL is blocked by the proxy.");
        }

        // STEP 1: Try to serve from cache
        if (isCacheable) {
            String cached = proxyCache.get(targetUrl);
            if (cached != null) {
                System.out.println("SERVED FROM CACHE: " + targetUrl);
                return ResponseEntity.ok()
                        .header("X-Proxy-Cache", "HIT")
                        .body(cached);
            }
        }

        try {
            HttpRequest.BodyPublisher bodyPublisher = getBodyPublisher(method, body);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .method(method.name(), bodyPublisher);

            Collections.list(originalRequest.getHeaderNames()).forEach(headerName -> {
                String headerValue = originalRequest.getHeader(headerName);
                if (!isRestrictedHeader(headerName)) {
                    builder.header(headerName, headerValue);
                }
            });

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());

            String responseBody = response.body();

            // STEP 2: Save to cache if cacheable
            if (isCacheable && response.statusCode() == 200) {
                proxyCache.put(targetUrl, responseBody);
                System.out.println("📦 CACHED: " + targetUrl);
            }

            return ResponseEntity.status(response.statusCode())
                    .header("X-Proxy-Cache", "MISS")
                    .body(responseBody);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Proxy-Cache", "ERROR")
                    .body("Proxy error: " + e.getMessage());
        }
    }
    private HttpRequest.BodyPublisher getBodyPublisher(HttpMethod method, String body) {
        // For methods like GET and DELETE or if there's no body to send
        if (method == HttpMethod.GET || method == HttpMethod.DELETE || body == null || body.isBlank()) {
            return HttpRequest.BodyPublishers.noBody(); // No body in request
        }

        // For methods like POST and PUT, send the body as a UTF-8 string
        return HttpRequest.BodyPublishers.ofString(body);
    }

    private boolean isRestrictedHeader(String header) {
        return List.of(
                "connection",
                "content-length",
                "host",
                "expect",
                "upgrade",
                "date",
                "via",
                "transfer-encoding"
        ).contains(header.toLowerCase());
    }


}


