package br.com.ceidigital.web;

import br.com.ceidigital.debug.WhatsappStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/__debug/notifications/whatsapp")
public class WhatsappDebugController {

    private final WhatsappStore store;

    public WhatsappDebugController(WhatsappStore store) {
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
