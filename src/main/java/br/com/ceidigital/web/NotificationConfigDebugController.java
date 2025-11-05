package br.com.ceidigital.web;

import br.com.ceidigital.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/__debug/notifications/config")
public class NotificationConfigDebugController {

    private final NotificationService notificationService;

    public NotificationConfigDebugController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<?> config(){
        return ResponseEntity.ok(
                Map.of(
                        "whatsappEnabled", notificationService.isWhatsappEnabled(),
                        "hasTwilioSid", notificationService.hasTwilioSid(),
                        "hasTwilioToken", notificationService.hasTwilioToken(),
                        "hasWhatsappFrom", notificationService.hasTwilioWhatsappFrom(),
                        "hasSmsFrom", notificationService.hasTwilioSmsFrom()
                )
        );
    }
}
