package seondays.shareticon.warmup;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/warmup")
public class WarmupController {

    private static final String WARMUP_TOKEN_HEADER = "X-Warmup-Token";

    private final WarmupService warmupService;
    private final WarmupProperties warmupProperties;

    @PostConstruct
    void validate() {
        if (!warmupProperties.hasToken()) {
            log.warn("warmup.token is not configured. Warmup endpoint will always return 403.");
        }
    }

    @GetMapping("/voucher-list")
    public ResponseEntity<WarmupResult> warmupVoucherList(
            @RequestHeader(value = WARMUP_TOKEN_HEADER, required = false) String warmupToken) {
        if (!isValidToken(warmupToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(warmupService.warmupVoucherList());
    }

    private boolean isValidToken(String warmupToken) {
        return warmupProperties.hasToken() && warmupProperties.token().equals(warmupToken);
    }
}
