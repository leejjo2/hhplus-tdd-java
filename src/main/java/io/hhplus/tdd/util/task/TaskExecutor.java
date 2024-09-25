package io.hhplus.tdd.util.task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class TaskExecutor {

    // 유저 큐에서 순차적으로 작업 실행
    public static void processQueue(BlockingQueue<Runnable> queue, ReentrantLock lock) {

        // 락을 사용해 동시성을 제어
        // 유저 큐에 대한 락을 획득
        lock.lock();
        try {
            Runnable task;
            while ((task = queue.poll()) != null) {
                task.run(); // 작업 실행 하는 중 락 유지
            }
        } finally {
            lock.unlock(); // 작업이 끝난 후 락을 해제
        }

    }

}
