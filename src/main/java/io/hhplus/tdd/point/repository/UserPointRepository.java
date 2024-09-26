package io.hhplus.tdd.point.repository;


import io.hhplus.tdd.point.aggregate.entity.UserPoint;

public interface UserPointRepository {

    UserPoint save(UserPoint chargedPoint);

    UserPoint findById(long userId);
}
