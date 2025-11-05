package br.com.ceidigital.debug;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Component
public class SmsStore {
    public static record Sms(String to, String body, Instant createdAt) {}

    private final Deque<Sms> messages = new ArrayDeque<>();
    private final int maxSize = 200;

    public synchronized void add(String to, String body){
        if (messages.size() >= maxSize) {
            messages.removeFirst();
        }
        messages.addLast(new Sms(to, body, Instant.now()));
    }

    public synchronized List<Sms> list(){
        return new ArrayList<>(messages);
    }

    public synchronized void clear(){
        messages.clear();
    }
}
