package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.aggregate.entity.PointHistory;
import io.hhplus.tdd.point.aggregate.vo.TransactionType;

import java.util.List;

public interface PointHistoryRepository {
    PointHistory save(long userId, long amount, TransactionType type, long updateMillis);
    List<PointHistory> findById(long userId);
}
