package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.aggregate.entity.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

class UserPointRepositoryImplTest {

    @Mock
    private UserPointTable userPointTable;

    @InjectMocks
    private UserPointRepositoryImpl userPointRepository;

    private long currentTime;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        currentTime = System.currentTimeMillis();
    }

    @Test
    @DisplayName("사용자 포인트를 저장")
    void save_success() {
        // given
        UserPoint userPoint = new UserPoint(1, 1000, currentTime);
        given(userPointTable.insertOrUpdate(1L, 1000L))
                .willReturn(userPoint);

        // when
        UserPoint savedUserPoint = userPointRepository.save(userPoint);

        // then
        assertEquals(userPoint, savedUserPoint);
    }

    @Test
    @DisplayName("사용자 포인트가 null 인 경우 포인트를 저장 불가")
    void fail_save_null() {
        // given
        UserPoint userPoint = null;

        // when & then
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userPointRepository.save(userPoint));

        // then
        assertEquals("userPoint is null", exception.getMessage());
    }

    @Test
    @DisplayName("사용자 ID로 사용자 포인트를 조회")
    void findByUserId_success() {
        // given
        UserPoint userPoint = new UserPoint(1L, 1000L, currentTime);
        given(userPointTable.selectById(1L))  // 정확한 값 사용
                .willReturn(userPoint);

        // when
        UserPoint foundUserPoint = userPointRepository.findById(1L);

        // then
        assertEquals(userPoint, foundUserPoint);
    }

    @Test
    @DisplayName("사용자 ID가 없는 경우 빈 UserPoint 객체를 반환")
    void findByUserId_not_found() {
        // given
        UserPoint emptyUserPoint = UserPoint.empty(1L);
        given(userPointTable.selectById(1L))
                .willReturn(emptyUserPoint);

        // when
        UserPoint foundUserPoint = userPointRepository.findById(1L);

        // then
        assertAll(
                () -> assertNotNull(foundUserPoint),
                () -> assertEquals(1L, foundUserPoint.id()),
                () -> assertEquals(0L, foundUserPoint.point())
        );
    }
}
