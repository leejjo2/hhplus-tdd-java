package io.hhplus.tdd.util.task.user;

import io.hhplus.tdd.util.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class UserTaskQueue {
    private final ConcurrentHashMap<Long, BlockingQueue<Runnable>> userQueues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, ExecutorService> userExecutors = new ConcurrentHashMap<>();


    // 유저별 작업 처리용 단일 쓰레드 ExecutorService 생성
    private ExecutorService getUserExecutor(long userId) {
        return userExecutors.computeIfAbsent(userId, id -> Executors.newSingleThreadExecutor());
    }

    // 유저별 큐에 작업 추가
    public void enqueueForUserWithoutLock(long userId, Runnable task) {
        BlockingQueue<Runnable> queue = userQueues.computeIfAbsent(userId, id -> new LinkedBlockingQueue<>());
        queue.add(task);
        ExecutorService userExecutor = getUserExecutor(userId);
        userExecutor.submit(() -> TaskExecutor.processQueueWithoutLock(queue));
    }


    private final ConcurrentHashMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();
    // 유저별 큐에 작업 추가
    public void enqueueForUser(long userId, Runnable task) {
        BlockingQueue<Runnable> queue = userQueues.computeIfAbsent(userId, id -> new LinkedBlockingQueue<>());
        queue.add(task);
        ReentrantLock lock = userLocks.computeIfAbsent(userId, id -> new ReentrantLock());
        TaskExecutor.processQueue(queue, lock);
    }


}
