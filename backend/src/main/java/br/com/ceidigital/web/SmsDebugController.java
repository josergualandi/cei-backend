package br.com.ceidigital.web;

import br.com.ceidigital.debug.SmsStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/__debug/notifications/sms")
public class SmsDebugController {

    private final SmsStore store;

    public SmsDebugController(SmsStore store) {
        this.store = store;
    }

    @GetMapping
    public ResponseEntity<?> list(){
        return ResponseEntity.ok(store.list());
    }

    @DeleteMapping
    public ResponseEntity<?> clear(){
        store.clear();
        return ResponseEntity.noContent().build();
    }
}
