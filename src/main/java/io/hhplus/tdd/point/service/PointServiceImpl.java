package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.aggregate.entity.PointHistory;
import io.hhplus.tdd.point.aggregate.entity.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    @Override
    public UserPoint findUserPointByUserId(long userId) {
        return null;
    }

    @Override
    public List<PointHistory> findPointHistoriesByUserId(long userId) {
        return null;
    }

    @Override
    synchronized public UserPoint chargeUserPoint(long userId, long amount) {
        return null;
    }

    @Override
    synchronized public UserPoint useUserPoint(long userId, long amount) {
        return null;
    }
}
