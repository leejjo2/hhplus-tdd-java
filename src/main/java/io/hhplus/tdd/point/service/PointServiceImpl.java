package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.aggregate.entity.PointHistory;
import io.hhplus.tdd.point.aggregate.entity.UserPoint;
import io.hhplus.tdd.point.aggregate.vo.TransactionType;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.util.LockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
// 의존성 주입을 위한 생성자 주입
@RequiredArgsConstructor
@Slf4j
public class PointServiceImpl implements PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final LockService lockService; // LockService  주입

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
        return lockService.executeWithLock(userId, () -> {
            UserPoint userPoint = userPointRepository.findById(userId);
            UserPoint chargedPoint = userPoint.charge(amount);
            userPointRepository.save(chargedPoint);
            pointHistoryRepository.save(userId, amount, TransactionType.CHARGE, chargedPoint.updateMillis());
            return chargedPoint;
        });
    }


    @Override
    public UserPoint useUserPoint(long userId, long amount) {
        return lockService.executeWithLock(userId, () -> {
            UserPoint userPoint = userPointRepository.findById(userId);
            UserPoint usedPoint = userPoint.use(amount);
            userPointRepository.save(usedPoint);
            pointHistoryRepository.save(userId, amount, TransactionType.USE, usedPoint.updateMillis());
            return usedPoint;
        });
    }

}
