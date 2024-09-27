package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.aggregate.entity.PointHistory;
import io.hhplus.tdd.point.aggregate.vo.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;

class PointHistoryRepositoryImplTest {

    @Mock
    private PointHistoryTable pointHistoryTable;

    @InjectMocks
    private PointHistoryRepositoryImpl pointHistoryRepository;

    private long currentTime;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        currentTime = currentTimeMillis();
    }

    @Test
    @DisplayName("PointHistoryTable 에 PointHistory 를 저장")
    void save() {

        // given
        final PointHistory pointHistory = new PointHistory(1L, 1L, 100L, TransactionType.CHARGE, currentTime);
        given(pointHistoryTable.insert(1L, 100L, TransactionType.CHARGE, currentTime))
                .willReturn(pointHistory);

        // when
        PointHistory savedPointHistory = pointHistoryRepository.save(1L, 100L, TransactionType.CHARGE, currentTime);

        // then
        assertNotNull(savedPointHistory);
        assertEquals(pointHistory, savedPointHistory);
    }

    @Test
    @DisplayName("사용자 ID로 PointHistory 조회")
    void findByUserId() {
        // given
        final PointHistory pointHistory = new PointHistory(1L, 1L, 100L, TransactionType.CHARGE, currentTime);
        final List<PointHistory> pointHistories = List.of(pointHistory);
        given(pointHistoryTable.selectAllByUserId(1L))
                .willReturn(pointHistories);

        // when
        List<PointHistory> resultPointHistories = pointHistoryRepository.findById(1L);

        // then
        assertEquals(pointHistories, resultPointHistories);
    }
}
