package vn.hoidanit.springrestwithai.config;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import vn.hoidanit.springrestwithai.feature.auth.RefreshTokenRepository;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private final RefreshTokenRepository refreshTokenRepository;

    public ScheduledTasks(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        int deletedCount = refreshTokenRepository.deleteAllExpiredBefore(Instant.now());
        log.info("Đã xóa {} refresh token hết hạn", deletedCount);
    }
}
