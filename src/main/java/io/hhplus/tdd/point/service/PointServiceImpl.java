package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.aggregate.entity.PointHistory;
import io.hhplus.tdd.point.aggregate.entity.UserPoint;
import io.hhplus.tdd.point.aggregate.vo.TransactionType;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.util.task.user.UserTaskQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
// 의존성 주입
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {


    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final UserTaskQueue userTaskQueue;


    @Override
    public UserPoint findUserPointByUserId(long userId) {
        return this.userPointRepository.findById(userId);
    }

    @Override
    public List<PointHistory> findPointHistoriesByUserId(long userId) {
        return this.pointHistoryRepository.findById(userId);
    }

    @Override
    public UserPoint chargeUserPoint(long userId, long amount) {

        userTaskQueue.enqueueForUser(userId, () -> {
            // lock 을 얻는 순서가 꼬일 수 있지 않을까?
            // 시간을 통해 락을 관리?
            // amount 는 음수, 최대값을 넘어서는 값 불가
            // userId  도용의 경우 고려
            UserPoint userPoint = userPointRepository.findById(userId);
            long updatedPoint = userPoint.point() + amount;
            System.out.printf(">>> cur <<< %d + %d = %d%n", userPoint.point(), amount, updatedPoint);

            userPointRepository.save(userId, updatedPoint);
            pointHistoryRepository.save(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());
        });

        return userPointRepository.findById(userId);
    }

    @Override
    public UserPoint chargeUserPointWithoutLock(long userId, long amount) {

        userTaskQueue.enqueueForUserWithoutLock(userId, () -> {
//        userTaskQueue.enqueueForUser(userId, () -> {
            // lock 을 얻는 순서가 꼬일 수 있지 않을까?
            // 시간을 통해 락을 관리?
            // amount 는 음수, 최대값을 넘어서는 값 불가
            // userId  도용의 경우 고려
            UserPoint userPoint = userPointRepository.findById(userId);
            long updatedPoint = userPoint.point() + amount;
            System.out.printf(">>> cur <<< %d + %d = %d%n", userPoint.point(), amount, updatedPoint);

            userPointRepository.save(userId, updatedPoint);
            pointHistoryRepository.save(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());
        });

        return userPointRepository.findById(userId);
    }

    @Override
    public UserPoint useUserPoint(long userId, long amount) {

//        userTaskQueue.enqueueForUserWithoutLock(userId, () -> {
        userTaskQueue.enqueueForUser(userId, () -> {
            // amount 는 음수, 최대값을 넘어서는 값 불가
            // userId 도용의 경우 고려
            // 잔고보다 많은 값 사용 불가
            UserPoint userPoint = userPointRepository.findById(userId);
            long updatedPoint = userPoint.point() - amount;
            System.out.printf(">>> cur <<< %d - %d = %d%n", userPoint.point(), amount, updatedPoint);
            userPointRepository.save(userId, updatedPoint);
            pointHistoryRepository.save(userId, amount, TransactionType.USE, System.currentTimeMillis());
        });

        return userPointRepository.findById(userId);
    }

}
