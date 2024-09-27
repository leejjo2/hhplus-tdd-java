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


    public UserPoint charge(long amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException("The points to be charged must be greater than 0.");
        }

        if (this.point + amount > 1_000_000) {
            throw new IllegalArgumentException("Cannot exceed the maximum balance.");
        }

        return new UserPoint(this.id, this.point + amount, System.currentTimeMillis());
    }

    public UserPoint use(final long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("The points to be used must be greater than 0.");
        }

        if (this.point < amount) {
            throw new IllegalArgumentException("Insufficient balance.");
        }

        return new UserPoint(this.id, this.point - amount, System.currentTimeMillis());
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
