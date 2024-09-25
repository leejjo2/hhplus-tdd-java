package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.aggregate.entity.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserPointRepositoryImpl implements UserPointRepository{
    //
    private final UserPointTable userPointTable;

    @Override
    public UserPoint save(long userId, long amount) {
        //
        return this.userPointTable.insertOrUpdate(userId, amount);
    }

    @Override
    public UserPoint findById(long userId) {
        //
        return this.userPointTable.selectById(userId);
    }
}
