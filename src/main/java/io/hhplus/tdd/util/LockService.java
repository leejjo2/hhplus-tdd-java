package io.hhplus.tdd.util;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Component
public class LockService {

    // 사용자별로 ReentrantLock을 저장하기 위한 ConcurrentHashMap
     protected final ConcurrentHashMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    /**
     * 사용자별로 작업을 순차적으로 실행하기 위한 메서드
     *
     * @param userId 사용자 ID
     * @param task   실행할 작업
     * @return 작업 결과
     */
    public <T> T executeWithLock(long userId, Supplier<T> task) {
        ReentrantLock lock = userLocks.computeIfAbsent(userId, id -> new ReentrantLock());
        lock.lock();
        try {
            return task.get();
        } finally {
            lock.unlock();
        }
    }
}
