package br.com.ceidigital.web.debug;

import br.com.ceidigital.debug.WhatsappStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@RestController("whatsappDebugAltController")
@RequestMapping("/__debug/whatsapp")
@ConditionalOnProperty(value = "debug.whatsapp.enabled", havingValue = "true", matchIfMissing = false)
public class WhatsappDebugController {

    private final WhatsappStore store;

    public WhatsappDebugController(WhatsappStore store) {
        this.store = store;
    }

    public record AddRequest(String to, String body) {}

    @GetMapping
    public List<WhatsappStore.Whatsapp> list(){
        return store.list();
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(){
        store.clear();
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<WhatsappStore.Whatsapp> add(@RequestBody AddRequest req){
        if (req == null || req.to() == null || req.to().isBlank() || req.body() == null || req.body().isBlank()){
            return ResponseEntity.badRequest().build();
        }
        store.add(req.to(), req.body());
        // Retorna o último item adicionado como conveniência
        var list = store.list();
        var created = list.isEmpty() ? new WhatsappStore.Whatsapp(req.to(), req.body(), Instant.now()) : list.get(list.size()-1);
        return ResponseEntity.created(URI.create("/__debug/whatsapp")).body(created);
    }
}
