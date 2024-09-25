package io.hhplus.tdd.point.service;


import io.hhplus.tdd.point.aggregate.entity.PointHistory;
import io.hhplus.tdd.point.aggregate.entity.UserPoint;

import java.util.List;

public interface PointService {

    UserPoint findUserPointByUserId(long userId);

    List<PointHistory> findPointHistoriesByUserId(long userId);

    UserPoint chargeUserPoint(long userId, long amount);

    UserPoint chargeUserPointWithoutLock(long userId, long amount);

    UserPoint useUserPoint(long userId, long amount);
}
