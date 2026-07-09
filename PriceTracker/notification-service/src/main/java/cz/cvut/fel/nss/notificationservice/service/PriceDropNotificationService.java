package cz.cvut.fel.nss.notificationservice.service;

import cz.cvut.fel.nss.notificationservice.dto.NotificationLogResponse;
import cz.cvut.fel.nss.notificationservice.entity.NotificationLog;
import cz.cvut.fel.nss.notificationservice.kafka.PriceDropEvent;
import cz.cvut.fel.nss.notificationservice.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceDropNotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final EmailService emailService;
    private final UserContactResolver userContactResolver;

    @Transactional
    public void handle(PriceDropEvent event) {
        String to = userContactResolver.resolveEmail(event.getUserId())
                .orElse(null);

        String subject = "PriceTracker: price drop on " + event.getProductName();
        String body = buildBody(event);

        boolean sent = false;
        if (to != null) {
            sent = emailService.send(to, subject, body);
        } else {
            log.warn("No email for userId={}, recording notification as NO_CONTACT", event.getUserId());
        }

        NotificationLog logRecord = new NotificationLog();
        logRecord.setUserId(event.getUserId());
        logRecord.setProductId(event.getProductId());
        logRecord.setMessage(body);
        logRecord.setChannel("EMAIL");
        logRecord.setStatus(sent ? "SENT" : (to == null ? "NO_CONTACT" : "FAILED"));
        notificationLogRepository.save(logRecord);
    }

    public List<NotificationLogResponse> listForUser(Long userId) {
        return notificationLogRepository.findByUserIdOrderBySentAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private String buildBody(PriceDropEvent event) {
        return new StringBuilder()
                .append("Hello,\n\n")
                .append("Price of ").append(event.getProductName())
                .append(" dropped to ").append(event.getNewPrice())
                .append(" at ").append(event.getShopName()).append(".\n")
                .append(event.getOldPrice() != null
                        ? "Previous price: " + event.getOldPrice() + "\n" : "")
                .append("Your target/threshold: ").append(event.getThresholdValue()).append("\n")
                .append("Link: ").append(event.getProductUrl() == null ? "-" : event.getProductUrl()).append("\n")
                .append("\n-- PriceTracker")
                .toString();
    }

    private NotificationLogResponse toResponse(NotificationLog log) {
        return NotificationLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .productId(log.getProductId())
                .message(log.getMessage())
                .sentAt(log.getSentAt())
                .status(log.getStatus())
                .channel(log.getChannel())
                .build();
    }
}
