package br.com.ceidigital.service;

import br.com.ceidigital.debug.SmsStore;
import br.com.ceidigital.debug.WhatsappStore;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final String mailFrom;
    private final SmsStore smsStore;
    private final WhatsappStore whatsappStore;

    private final String twilioSid;
    private final String twilioToken;
    private final String twilioFrom;
    private final String twilioWhatsappFrom;
    private final boolean whatsappEnabled;

    public NotificationService(
            @Nullable JavaMailSender mailSender,
            @Value("${spring.mail.from:no-reply@localhost}") String mailFrom,
            SmsStore smsStore,
            WhatsappStore whatsappStore,
            @Value("${twilio.account-sid:}") String twilioSid,
            @Value("${twilio.auth-token:}") String twilioToken,
            @Value("${twilio.from-number:}") String twilioFrom,
            @Value("${twilio.whatsapp-from:}") String twilioWhatsappFrom,
            @Value("${notification.whatsapp.enabled:false}") boolean whatsappEnabled
    ) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
        this.smsStore = smsStore;
        this.whatsappStore = whatsappStore;
        this.twilioSid = twilioSid;
        this.twilioToken = twilioToken;
        this.twilioFrom = twilioFrom;
        this.twilioWhatsappFrom = twilioWhatsappFrom;
        this.whatsappEnabled = whatsappEnabled;
    }

    public void sendEmail(String to, String subject, String body){
        if (mailSender == null) {
            log.info("[EMAIL:DEV] para={} subject={} body={}", to, subject, body);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("[EMAIL] enviado para={} subject={}", to, subject);
        } catch (Exception e) {
            log.warn("[EMAIL] falha ao enviar para {}: {}. Caindo para log.", to, e.getMessage());
            log.info("[EMAIL:DEV] para={} subject={} body={}", to, subject, body);
        }
    }

    public void sendSms(String phone, String body){
        String toE164 = toE164(phone);
        if (twilioSid == null || twilioSid.isBlank() || twilioToken == null || twilioToken.isBlank() || twilioFrom == null || twilioFrom.isBlank()) {
            log.info("[SMS:DEV] para={} body={}", toE164, body);
            smsStore.add(toE164, body);
            return;
        }
        try {
            // Envia via REST API do Twilio para evitar depender do SDK no build
            String url = String.format("https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json", twilioSid);
            RestTemplate rest = new RestTemplate();
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("To", toE164);
            form.add("From", twilioFrom);
            form.add("Body", body);
            // Basic Auth usando SID:TOKEN
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setBasicAuth(twilioSid, twilioToken);
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
            org.springframework.http.HttpEntity<MultiValueMap<String, String>> req = new org.springframework.http.HttpEntity<>(form, headers);
            rest.postForEntity(url, req, String.class);
            log.info("[SMS] enviado para={}", toE164);
            // Mesmo em produção, armazenar cópia local ajuda no debug
            smsStore.add(toE164, body);
        } catch (Exception e) {
            log.warn("[SMS] falha ao enviar para {}: {}. Caindo para log.", toE164, e.getMessage());
            log.info("[SMS:DEV] para={} body={}", toE164, body);
            smsStore.add(toE164, body);
        }
    }

    public boolean isWhatsappEnabled() {
        return whatsappEnabled;
    }

    public boolean hasTwilioSid() { return twilioSid != null && !twilioSid.isBlank(); }
    public boolean hasTwilioToken() { return twilioToken != null && !twilioToken.isBlank(); }
    public boolean hasTwilioSmsFrom() { return twilioFrom != null && !twilioFrom.isBlank(); }
    public boolean hasTwilioWhatsappFrom() { return twilioWhatsappFrom != null && !twilioWhatsappFrom.isBlank(); }

    public void sendWhatsapp(String phone, String body) {
        String toE164 = toE164(phone);
        // Twilio WhatsApp usa prefixo 'whatsapp:' em To/From
        if (!whatsappEnabled || twilioSid == null || twilioSid.isBlank() || twilioToken == null || twilioToken.isBlank() || twilioWhatsappFrom == null || twilioWhatsappFrom.isBlank()) {
            log.info("[WHATSAPP:DEV] para={} body= {}", toE164, body);
            whatsappStore.add(toE164, body);
            return;
        }
        try {
            String url = String.format("https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json", twilioSid);
            RestTemplate rest = new RestTemplate();
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("To", "whatsapp:" + toE164);
            form.add("From", "whatsapp:" + toE164(twilioWhatsappFrom));
            form.add("Body", body);
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setBasicAuth(twilioSid, twilioToken);
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
            org.springframework.http.HttpEntity<MultiValueMap<String, String>> req = new org.springframework.http.HttpEntity<>(form, headers);
            rest.postForEntity(url, req, String.class);
            log.info("[WHATSAPP] enviado para={}", toE164);
            whatsappStore.add(toE164, body);
        } catch (Exception e) {
            log.warn("[WHATSAPP] falha ao enviar para {}: {}. Caindo para log.", toE164, e.getMessage());
            log.info("[WHATSAPP:DEV] para={} body= {}", toE164, body);
            whatsappStore.add(toE164, body);
        }
    }

    private String toE164(String phone){
        if (phone == null) return "";
        String trimmed = phone.trim();
        // Se já vier em E.164 (ex.: +14155238886 ou +5511999999999), apenas normaliza removendo espaços
        if (trimmed.startsWith("+")) {
            String digits = trimmed.replaceAll("[^0-9]", "");
            return "+" + digits;
        }
        String digits = trimmed.replaceAll("[^0-9]", "");
        if (digits.isBlank()) return "";
        // Se já começar com 55, mantém (Brasil)
        if (digits.startsWith("55")) {
            return "+" + digits;
        }
        // Heurística: 10-11 dígitos -> número nacional (Brasil) sem DDI, prefixa 55
        if (digits.length() == 10 || digits.length() == 11) {
            return "+55" + digits;
        }
        // Caso contrário, assume que o chamador passou o DDI sem '+', apenas prefixa '+'
        return "+" + digits;
    }
}
