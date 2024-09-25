package io.hhplus.tdd.point.repository;


import io.hhplus.tdd.point.aggregate.entity.UserPoint;

public interface UserPointRepository {
    UserPoint save(long userId, long amount);
    UserPoint findById(long userId);
}
