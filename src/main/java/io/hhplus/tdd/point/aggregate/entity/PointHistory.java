package io.hhplus.tdd.point.aggregate.entity;

import io.hhplus.tdd.point.aggregate.vo.TransactionType;

public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {
    @Override
    public String toString() {
        return String.format(
                "\n UserHistoryDomain { id = %d, userId = %d, amount = %d, type = %s, updateMillis = %d }",
                id,
                userId,
                amount,
                type.toString(),
                updateMillis
        );
    }
}
