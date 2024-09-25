package io.hhplus.tdd.util.task.user;

import io.hhplus.tdd.util.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class UserTaskQueue {
    private final ConcurrentHashMap<Long, BlockingQueue<Runnable>> userQueues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    // 유저별 큐에 작업 추가
    public void enqueueForUser(long userId, Runnable task) {
        BlockingQueue<Runnable> queue = userQueues.computeIfAbsent(userId, id -> new LinkedBlockingQueue<>());
        queue.add(task);
        ReentrantLock lock = userLocks.computeIfAbsent(userId, id -> new ReentrantLock());
        TaskExecutor.processQueue(queue, lock);
    }


    private final ConcurrentHashMap<Long, ExecutorService> userExecutors = new ConcurrentHashMap<>();

    // 유저별 큐에 작업 추가
    @Deprecated
    public void enqueueForUserWithoutLock(long userId, Runnable task) {
        // 유저별 작업 처리용 단일 쓰레드 ExecutorService 생성
        // 작업 추가와 동시에 실행되므로 여러 작업이 동시에 큐에 추가되면 이전 작업의 처리가 완료되지 않고 또 다른 작업이 들어가버림
        // 동시성 해결하지 못함
        // 해결함 . queue 에 넣을 필요가 없었음.
        ExecutorService userExecutor = userExecutors.computeIfAbsent(userId, id -> Executors.newSingleThreadExecutor());
        try {
            userExecutor.submit(task).get();
        } catch (InterruptedException e) {
            System.out.println("error");
        } catch (ExecutionException e) {
            System.out.println("error");
        }
    }


}
