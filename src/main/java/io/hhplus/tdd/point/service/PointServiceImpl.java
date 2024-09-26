package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.aggregate.entity.PointHistory;
import io.hhplus.tdd.point.aggregate.entity.UserPoint;
import io.hhplus.tdd.point.aggregate.vo.TransactionType;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Service
// 의존성 주입을 위한 생성자 주입
@RequiredArgsConstructor
@Slf4j
public class PointServiceImpl implements PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Override
    public UserPoint findUserPointByUserId(long userId) {
        return userPointRepository.findById(userId);
    }

    @Override
    public List<PointHistory> findPointHistoriesByUserId(long userId) {
        return pointHistoryRepository.findById(userId);
    }

    @Override
    public UserPoint chargeUserPoint(long userId, long amount) {
        return enqueueAndExecuteTaskForUser(userId, () -> {
            UserPoint userPoint = userPointRepository.findById(userId);
            UserPoint chargedPoint = userPoint.charge(amount);
            userPointRepository.save(chargedPoint);
            pointHistoryRepository.save(userId, amount, TransactionType.CHARGE, chargedPoint.updateMillis());
            return chargedPoint;
        });
    }

    @Override
    public UserPoint chargeUserPointWithoutLock(long userId, long amount) {
        executeTaskForUserWithoutLock(userId, () -> {
            UserPoint userPoint = userPointRepository.findById(userId);
            UserPoint chargedPoint = userPoint.charge(amount);
            userPointRepository.save(chargedPoint);
            pointHistoryRepository.save(userId, amount, TransactionType.CHARGE, chargedPoint.updateMillis());
        });

        return userPointRepository.findById(userId);
    }

    @Override
    public UserPoint useUserPoint(long userId, long amount) {
        return enqueueAndExecuteTaskForUser(userId, () -> {
            UserPoint userPoint = userPointRepository.findById(userId);
            UserPoint usedPoint = userPoint.use(amount);
            userPointRepository.save(usedPoint);
            pointHistoryRepository.save(userId, amount, TransactionType.USE, usedPoint.updateMillis());
            return usedPoint;
        });
    }


    // 사용자별로 ReentrantLock을 저장하기 위한 ConcurrentHashMap
    private final ConcurrentHashMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    /**
     * @param userId 사용자 ID
     * @param task   실행할 작업
     */
    public <T> T enqueueAndExecuteTaskForUser(long userId, Supplier<T> task) {
        // 사용자 ID에 해당하는 Lock을 가져오거나 생성
        ReentrantLock lock = userLocks.computeIfAbsent(userId, id -> new ReentrantLock());
        // 작업을 처리하고 Lock을 사용하여 동시 실행을 방지
        // 락을 사용해 동시성을 제어
        // 유저 큐에 대한 락을 획득
        lock.lock();
        try {
            return task.get();
        } finally {
            lock.unlock(); // 작업이 끝난 후 락을 해제
        }
    }

    // 사용자별 ExecutorService를 저장하기 위한 ConcurrentHashMap
    private final ConcurrentHashMap<Long, ExecutorService> userExecutors = new ConcurrentHashMap<>();

    /**
     * 사용자별 작업을 동기적으로 실행
     * 단일 쓰레드 ExecutorService를 사용하여 작업을 순차적으로 처리
     *
     * @param userId 사용자 ID
     * @param task   실행할 작업
     */
    public void executeTaskForUserWithoutLock(long userId, Runnable task) {
        // 유저별 작업 처리용 단일 쓰레드 ExecutorService 생성
        ExecutorService userExecutor = userExecutors.computeIfAbsent(userId, id -> Executors.newSingleThreadExecutor());
        try {
            // 작업을 제출하고 실행 결과 대기
            userExecutor.submit(task).get();
        } catch (InterruptedException e) {
            log.error("작업이 인터럽트되었습니다: {}", e.getMessage());
            Thread.currentThread().interrupt(); // 인터럽트 상태 복원
        } catch (ExecutionException e) {
            log.error("작업 실행 중 오류 발생: {}", e.getMessage());
        }
    }
}
