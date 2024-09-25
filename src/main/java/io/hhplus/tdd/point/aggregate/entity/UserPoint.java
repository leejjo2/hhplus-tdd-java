package io.hhplus.tdd.point.aggregate.entity;

// get 메서드 자동생성
public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return String.format(
                "\n UserPointDomain { id = %d, point = %d }",
                id,
                point
        );
    }

}
