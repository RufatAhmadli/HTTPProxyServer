package az.edu.example.web.httpproxyserver.controller;

import az.edu.example.web.httpproxyserver.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/proxy")
public class ProxyController {

    private final ProxyService proxyService;

    public ProxyController(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyRequest(
            @RequestParam String targetUrl,
            @RequestBody(required = false) String body,
            HttpMethod method,
            HttpServletRequest request
    ) {
        return proxyService.forwardRequest(targetUrl, method, body, request);
    }
}






