package io.hhplus.tdd.point.aggregate.entity;

import io.hhplus.tdd.point.aggregate.vo.TransactionType;

public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {
}
