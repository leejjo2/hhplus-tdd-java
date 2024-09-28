package io.hhplus.tdd.util;
import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class LockServiceTest {

    private final LockService lockService = new LockService();

    @Test
    void should_ExecuteTaskWithLock() {
        // Given
        long userId = 1L;
        Supplier<String> task = () -> "Task Completed";

        // When
        String result = lockService.executeWithLock(userId, task);

        // Then
        assertEquals("Task Completed", result);
    }

    @Test
    void should_UseDifferentLocksForDifferentUsers() {
        // Given
        long userId1 = 1L;
        long userId2 = 2L;

        // When
        lockService.executeWithLock(userId1, () -> "Task for User 1");
        lockService.executeWithLock(userId2, () -> "Task for User 2");

        ReentrantLock lock1 = lockService.userLocks.get(userId1);
        ReentrantLock lock2 = lockService.userLocks.get(userId2);

        // Then
        assertNotSame(lock1, lock2); // 서로 다른 유저는 다른 락을 가져야 함
    }
}
