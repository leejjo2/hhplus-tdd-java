package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.aggregate.entity.PointHistory;
import io.hhplus.tdd.point.aggregate.vo.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepositoryImpl implements PointHistoryRepository{
    //
    private final PointHistoryTable pointHistoryTable;

    public PointHistory save(long userId, long amount, TransactionType type, long updateMillis) {
        //
        return this.pointHistoryTable.insert(userId, amount, type, updateMillis);
    }

    public List<PointHistory> findById(long userId) {
        //
        return this.pointHistoryTable.selectAllByUserId(userId);
    }
}
