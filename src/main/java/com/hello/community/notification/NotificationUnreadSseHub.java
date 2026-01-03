// NotificationUnreadSseHub.java
package com.hello.community.notification;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

@Component
public class NotificationUnreadSseHub {

    private final ConcurrentHashMap<Long, CopyOnWriteArraySet<SseEmitter>> emitters = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("notification-sse-heartbeat");
        return t;
    });

    public NotificationUnreadSseHub() {
        scheduler.scheduleAtFixedRate(this::sendHeartbeatAll, 15, 15, TimeUnit.SECONDS);
    }

    public SseEmitter connect(Long memberId, long timeoutMs) {
        SseEmitter emitter = new SseEmitter(timeoutMs);

        if (memberId == null) {
            emitter.complete();
            return emitter;
        }

        emitters.computeIfAbsent(memberId, k -> new CopyOnWriteArraySet<>()).add(emitter);

        Runnable cleanup = () -> remove(memberId, emitter);

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        return emitter;
    }

    public void sendUnreadCount(Long memberId, long unreadCount) {
        if (memberId == null) {
            return;
        }

        Set<SseEmitter> set = emitters.get(memberId);
        if (set == null || set.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : set) {
            try {
                emitter.send(SseEmitter.event()
                        .name("unread-count")
                        .data(Map.of(
                                "unreadCount", unreadCount,
                                "ts", Instant.now().toString()
                        )));
            } catch (IOException e) {
                remove(memberId, emitter);
            } catch (Exception e) {
                remove(memberId, emitter);
            }
        }
    }

    private void sendHeartbeatAll() {
        for (Long memberId : emitters.keySet()) {
            Set<SseEmitter> set = emitters.get(memberId);
            if (set == null || set.isEmpty()) {
                continue;
            }

            for (SseEmitter emitter : set) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("keep-alive")
                            .data(Map.of("ts", Instant.now().toString())));
                } catch (IOException e) {
                    remove(memberId, emitter);
                } catch (Exception e) {
                    remove(memberId, emitter);
                }
            }
        }
    }

    private void remove(Long memberId, SseEmitter emitter) {
        if (memberId == null || emitter == null) {
            return;
        }

        Set<SseEmitter> set = emitters.get(memberId);
        if (set == null) {
            return;
        }

        set.remove(emitter);

        if (set.isEmpty()) {
            emitters.remove(memberId);
        }
    }
}
