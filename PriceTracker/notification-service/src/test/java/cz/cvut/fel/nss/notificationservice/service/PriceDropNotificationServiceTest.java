package cz.cvut.fel.nss.notificationservice.service;

import cz.cvut.fel.nss.notificationservice.entity.NotificationLog;
import cz.cvut.fel.nss.notificationservice.kafka.PriceDropEvent;
import cz.cvut.fel.nss.notificationservice.repository.NotificationLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceDropNotificationServiceTest {

    @Mock
    private NotificationLogRepository notificationLogRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private UserContactResolver userContactResolver;

    @InjectMocks
    private PriceDropNotificationService service;

    private PriceDropEvent event() {
        return PriceDropEvent.builder()
                .eventId("e1")
                .productId(1L)
                .productName("RAM")
                .userId(42L)
                .oldPrice(new BigDecimal("2500"))
                .newPrice(new BigDecimal("1900"))
                .thresholdValue(new BigDecimal("2000"))
                .shopName("Alza")
                .productUrl("https://alza.cz/ram")
                .triggeredAt(System.currentTimeMillis())
                .build();
    }

    @Test
    void handle_sendsEmail_andLogsSent_whenEmailResolvedAndDelivered() {
        when(userContactResolver.resolveEmail(42L)).thenReturn(Optional.of("user@x.cz"));
        when(emailService.send(eq("user@x.cz"), anyString(), anyString())).thenReturn(true);

        service.handle(event());

        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository).save(captor.capture());
        NotificationLog log = captor.getValue();
        assertThat(log.getStatus()).isEqualTo("SENT");
        assertThat(log.getUserId()).isEqualTo(42L);
        assertThat(log.getProductId()).isEqualTo(1L);
        assertThat(log.getChannel()).isEqualTo("EMAIL");
    }

    @Test
    void handle_logsFailed_whenEmailResolvedButDeliveryFails() {
        when(userContactResolver.resolveEmail(42L)).thenReturn(Optional.of("user@x.cz"));
        when(emailService.send(anyString(), anyString(), anyString())).thenReturn(false);

        service.handle(event());

        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("FAILED");
    }

    @Test
    void handle_logsNoContact_andSkipsEmail_whenEmailUnresolved() {
        when(userContactResolver.resolveEmail(42L)).thenReturn(Optional.empty());

        service.handle(event());

        verify(emailService, never()).send(anyString(), anyString(), anyString());
        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("NO_CONTACT");
    }

    @Test
    void handle_buildsBodyContainingPriceDetails() {
        when(userContactResolver.resolveEmail(42L)).thenReturn(Optional.of("user@x.cz"));
        when(emailService.send(anyString(), anyString(), anyString())).thenReturn(true);

        service.handle(event());

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(emailService).send(eq("user@x.cz"), anyString(), body.capture());
        assertThat(body.getValue()).contains("RAM", "1900", "Alza");
    }

    @Test
    void listForUser_mapsLogs() {
        NotificationLog log = new NotificationLog();
        log.setId(5L);
        log.setUserId(42L);
        log.setStatus("SENT");
        when(notificationLogRepository.findByUserIdOrderBySentAtDesc(42L)).thenReturn(java.util.List.of(log));

        var result = service.listForUser(42L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("SENT");
    }
}
