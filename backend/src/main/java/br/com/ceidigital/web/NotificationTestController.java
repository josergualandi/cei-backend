package br.com.ceidigital.web;

import br.com.ceidigital.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/__debug/notifications")
public class NotificationTestController {

    private final NotificationService notificationService;

    public NotificationTestController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Envia email e/ou sms e/ou whatsapp para testes. Body JSON opcional.
     * Exemplo POST: { "email": "me@local.dev", "phone": "+5511999999999", "subject": "Teste", "body": "mensagem de teste", "sendEmail": true, "sendSms": true, "sendWhatsapp": true }
     */
    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody Map<String, Object> payload) {
        String email = payload.getOrDefault("email", "").toString();
        String phone = payload.getOrDefault("phone", "").toString();
        String subject = payload.getOrDefault("subject", "Teste CEI").toString();
        String body = payload.getOrDefault("body", "Mensagem de teste do sistema CEI").toString();
    boolean sendEmail = Boolean.parseBoolean(payload.getOrDefault("sendEmail", "true").toString());
    boolean sendSms = Boolean.parseBoolean(payload.getOrDefault("sendSms", "true").toString());
    boolean sendWhatsapp = Boolean.parseBoolean(payload.getOrDefault("sendWhatsapp", "false").toString());

        try {
            if (sendEmail && email != null && !email.isBlank()) {
                notificationService.sendEmail(email, subject, body);
            }
            if (sendSms && phone != null && !phone.isBlank()) {
                notificationService.sendSms(phone, body);
            }
            if (sendWhatsapp && phone != null && !phone.isBlank()) {
                notificationService.sendWhatsapp(phone, body);
            }
            return ResponseEntity.ok(Map.of("email", sendEmail, "sms", sendSms, "whatsapp", sendWhatsapp));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
