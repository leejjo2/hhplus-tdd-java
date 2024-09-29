package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.aggregate.entity.UserPoint;
import io.hhplus.tdd.point.aggregate.vo.TransactionType;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.util.LockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceImplTest {

    @Mock
    private UserPointRepository userPointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;
    @Mock
    private LockService lockService;

    @InjectMocks
    private PointServiceImpl pointService;

    @Test
    @DisplayName("사용자가 100 포인트 충전 요청 시, 충전 후 1000 포인트를 반환하는지 테스트")
    void should_Return_1000Points_When_UserWithId1Charges_100Points() {
        // Given: mock 객체를 사용하여 초기값 설정

        UserPoint userPoint = mock(UserPoint.class);
        given(userPointRepository.findById(anyLong())).willReturn(userPoint);
        given(userPoint.charge(anyLong())).willReturn(new UserPoint(1L, 1000L, System.currentTimeMillis()));


        // LockService 모킹: 실제 작업을 수행하는 부분을 래핑
        given(lockService.executeWithLock(eq(1L), any(Supplier.class))).willAnswer(invocation -> {
            Supplier<?> task = invocation.getArgument(1);
            return task.get();
        });

        // When: 포인트 충전 메서드 호출
        UserPoint chargedPoint = pointService.chargeUserPoint(1L, 100L);

        // Then: 결과 검증
        assertAll(
                () -> assertNotNull(chargedPoint), // 포인트 객체가 null이 아닌지 확인
                () -> assertEquals(1L, chargedPoint.id()), // 사용자 ID가 1인지 확인
                () -> assertEquals(1000L, chargedPoint.point()), // 충전 후 포인트가 30,000인지 확인
                () -> then(userPointRepository).should().findById(1L), // findById 메서드 호출 확인
                () -> then(userPoint).should().charge(100L), // charge 메서드 호출 확인
                () -> then(userPointRepository).should(times(1)).save(chargedPoint), // save 메서드 호출 확인
                () -> then(pointHistoryRepository).should(times(1)).save(1L, 100L, TransactionType.CHARGE, chargedPoint.updateMillis()) // 히스토리 저장 확인
        );
    }

    @Test
    @DisplayName("유효하지 않은 금액(음수)으로 포인트 충전 시 예외가 발생하는지 테스트")
    void should_ThrowException_When_ChargingWithInvalidAmount() {
        // Given: 유효하지 않은 포인트 충전 시도
        UserPoint userPoint = mock(UserPoint.class);
        given(userPointRepository.findById(anyLong())).willReturn(userPoint);
        doThrow(new IllegalArgumentException("The points to be charged must be greater than 0."))
                .when(userPoint).charge(anyLong());

        // LockService 모킹: 실제 작업을 수행하는 부분을 래핑
        given(lockService.executeWithLock(eq(1L), any(Supplier.class))).willAnswer(invocation -> {
            Supplier<?> task = invocation.getArgument(1);
            return task.get();
        });


        // When & Then: 예외가 발생하는지 확인
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> pointService.chargeUserPoint(1L, -100L));

        // Then: 예외 메시지와 메서드 호출 검증
        assertAll(
                () -> assertEquals("The points to be charged must be greater than 0.", exception.getMessage()), // 예외 메시지 확인
                () -> then(userPointRepository).should().findById(1L), // findById 메서드 호출 확인
                () -> then(userPoint).should(times(1)).charge(-100L), // charge 메서드 호출 확인
                () -> then(userPointRepository).should(never()).save(any(UserPoint.class)), // save 메서드가 호출되지 않았는지 확인
                () -> then(pointHistoryRepository).should(never()).save(anyLong(), anyLong(), any(TransactionType.class), anyLong()) // 히스토리 저장 메서드 호출되지 않았는지 확인
        );
    }

    /**
     * 사용자가 100 포인트 사용 요청 시, 사용 후 1000 포인트를 반환하는지 테스트
     */
    @Test
    void should_Return_20000Points_When_UserWithId1Uses_10000Points() {
        // Given: 포인트 사용 설정
        UserPoint userPoint = mock(UserPoint.class);
        given(userPointRepository.findById(anyLong())).willReturn(userPoint);
        given(userPoint.use(anyLong())).willReturn(new UserPoint(1L, 1000L, System.currentTimeMillis()));

        // LockService 모킹: 실제 작업을 수행하는 부분을 래핑
        given(lockService.executeWithLock(eq(1L), any(Supplier.class))).willAnswer(invocation -> {
            Supplier<?> task = invocation.getArgument(1);
            return task.get();
        });

        // When: 포인트 사용 메서드 호출
        UserPoint usedPoint = pointService.useUserPoint(1L, 100L);

        // Then: 결과 검증
        assertAll(
                () -> assertNotNull(usedPoint), // 포인트 객체가 null이 아닌지 확인
                () -> assertEquals(1L, usedPoint.id()), // 사용자 ID가 1인지 확인
                () -> assertEquals(1000L, usedPoint.point()), // 사용 후 포인트가 20,000인지 확인
                () -> then(userPointRepository).should().findById(1L), // findById 메서드 호출 확인
                () -> then(userPoint).should().use(100L), // use 메서드 호출 확인
                () -> then(userPointRepository).should(times(1)).save(usedPoint), // save 메서드 호출 확인
                () -> then(pointHistoryRepository).should(times(1)).save(1L, 100L, TransactionType.USE, usedPoint.updateMillis()) // 히스토리 저장 확인
        );
    }

    @Test
    @DisplayName("유효하지 않은 금액으로 포인트 사용 시 예외가 발생한다.")
    void should_ThrowException_When_UsingWithInvalidAmount() {
        // Given: 유효하지 않은 포인트 사용 시도
        UserPoint userPoint = mock(UserPoint.class);
        given(userPointRepository.findById(anyLong())).willReturn(userPoint);
        doThrow(new IllegalArgumentException("The points to be used must be greater than 0."))
                .when(userPoint).use(anyLong());

        // LockService 모킹: 실제 작업을 수행하는 부분을 래핑
        given(lockService.executeWithLock(eq(1L), any(Supplier.class))).willAnswer(invocation -> {
            Supplier<?> task = invocation.getArgument(1);
            return task.get();
        });

        // When & Then: 예외가 발생하는지 확인
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> pointService.useUserPoint(1L, -1000L));

        // Then: 예외 메시지와 메서드 호출 검증
        assertAll(
                () -> assertEquals("The points to be used must be greater than 0.", exception.getMessage()), // 예외 메시지 확인
                () -> then(userPointRepository).should().findById(1L), // findById 메서드 호출 확인
                () -> then(userPoint).should(times(1)).use(-1000L), // use 메서드 호출 확인
                () -> then(userPointRepository).should(never()).save(any(UserPoint.class)), // save 메서드가 호출되지 않았는지 확인
                () -> then(pointHistoryRepository).should(never()).save(anyLong(), anyLong(), any(TransactionType.class), anyLong()) // 히스토리 저장 메서드 호출되지 않았는지 확인
        );
    }
}
