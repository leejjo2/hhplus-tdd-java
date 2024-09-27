package io.hhplus.tdd.point.aggregate.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class UserPointTest {

    @Test
    @DisplayName("빈 메서드를 통해 사용자 포인트를 생성할 수 있다.")
    void can_create_user_point_through_empty_method() {
        // When
        UserPoint userPoint = UserPoint.empty(1L);

        // Then
        assertEquals(1L, userPoint.id()); // 사용자의 ID가 1L과 일치하는지 확인
        assertEquals(0L, userPoint.point()); // 초기 포인트가 0인지 확인
        assertTrue(userPoint.updateMillis() > 0L); // 업데이트 시각이 현재 시각보다 큰지 확인
    }

    @Test
    @DisplayName("포인트를 성공적으로 충전할 수 있다.")
    void can_charge_points_successfully() {
        // Given
        UserPoint userPoint = new UserPoint(1L, 5000L, System.currentTimeMillis());

        // When
        UserPoint updatedUserPoint = userPoint.charge(10000L);

        // Then
        assertEquals(15000L, updatedUserPoint.point()); // 충전 후 포인트가 15000인지 확인
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1000L})
    @DisplayName("충전 금액이 0보다 작으면 예외를 발생시킨다.")
    void throws_exception_when_charge_amount_is_less_than_zero(long amount) {
        // Given
        UserPoint userPoint = new UserPoint(1L, 5000L, System.currentTimeMillis());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userPoint.charge(amount));

        assertEquals("The points to be charged must be greater than 0.", exception.getMessage()); // 예외 메시지가 올바른지 확인
    }

    @Test
    @DisplayName("최대 잔고를 초과하는 포인트를 충전할 수 없다.")
    void cannot_charge_more_than_maximum_balance() {
        // Given
        UserPoint userPoint = new UserPoint(1L, 980000L, System.currentTimeMillis());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userPoint.charge(21000L));

        assertEquals("Cannot exceed the maximum balance.", exception.getMessage()); // 예외 메시지가 올바른지 확인
    }

    @Test
    @DisplayName("사용자 포인트를 사용할 수 있다.")
    void can_use_user_points() {
        // Given
        UserPoint userPoint = new UserPoint(1L, 5000L, System.currentTimeMillis());

        // When
        UserPoint updatedUserPoint = userPoint.use(5000L);

        // Then
        assertEquals(0L, updatedUserPoint.point()); // 사용 후 포인트가 0인지 확인
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1000L})
    @DisplayName("사용할 포인트가 0보다 작으면 예외를 발생시킨다.")
    void throws_exception_when_using_points_less_than_zero(long amount) {
        // Given
        UserPoint userPoint = new UserPoint(1L, 5000L, System.currentTimeMillis());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userPoint.use(amount));

        assertEquals("The points to be used must be greater than 0.", exception.getMessage()); // 예외 메시지가 올바른지 확인
    }

    @Test
    @DisplayName("잔고가 부족할 경우 포인트를 사용할 수 없다.")
    void cannot_use_points_when_balance_is_insufficient() {
        // Given
        UserPoint userPoint = new UserPoint(1L, 5000L, System.currentTimeMillis());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userPoint.use(6000L));

        assertEquals("Insufficient balance.", exception.getMessage()); // 예외 메시지가 올바른지 확인
    }
}
