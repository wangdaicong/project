package com.yibiai.thesis.service;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

@Component
public class ConcurrencyManager {

    private final int maxUsers = 2;
    private final Semaphore semaphore = new Semaphore(maxUsers, true);

    private final Deque<String> queue = new ArrayDeque<>();
    private final Set<String> active = new HashSet<>();

    private final Object lock = new Object();

    public boolean acquire(String sessionId) {
        synchronized (lock) {
            if (!queue.contains(sessionId) && !active.contains(sessionId)) {
                queue.addLast(sessionId);
            }
        }

        while (true) {
            synchronized (lock) {
                if (!queue.isEmpty() && sessionId.equals(queue.peekFirst()) && semaphore.tryAcquire()) {
                    queue.removeFirst();
                    active.add(sessionId);
                    return true;
                }
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    public void release(String sessionId) {
        synchronized (lock) {
            if (active.remove(sessionId)) {
                semaphore.release();
            }
        }
    }

    public Map<String, Object> getStatus(String sessionId) {
        synchronized (lock) {
            int queueLength = queue.size();
            Integer yourPosition = null;
            if (sessionId != null) {
                int idx = 0;
                for (String id : queue) {
                    if (id.equals(sessionId)) {
                        yourPosition = idx + 1;
                        break;
                    }
                    idx++;
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("current_users", active.size());
            result.put("max_users", maxUsers);
            result.put("queue_length", queueLength);
            result.put("your_position", yourPosition);
            result.put("estimated_wait_time", yourPosition == null ? null : yourPosition * 10);
            return result;
        }
    }
}
