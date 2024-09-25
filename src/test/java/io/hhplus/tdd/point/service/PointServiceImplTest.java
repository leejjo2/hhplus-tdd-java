package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.aggregate.entity.PointHistory;
import io.hhplus.tdd.point.aggregate.entity.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PointServiceImplTest {

    @Autowired
    private PointService pointService;

    @Test
    @DisplayName("포인트 충전 동시성 테스트")
    void chargePoint() throws InterruptedException {
        // Setup
        long userId = 1L;
        long chargeAmount = 100;

        int count = 10;

        // Action
        ExecutorService executorService = Executors.newFixedThreadPool(count);

        for (int i = 0; i < count; i++) {
            // 동시에 포인트 충전요청
            executorService.submit(() -> pointService.chargeUserPoint(userId, chargeAmount));
        }

        try {
            // 모든 스레드가 종료될 때까지 최대 10초 대기
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                // 만약 10초 내에 종료되지 않으면 강제 종료
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            throw e;
        }

        // 포인트 히스토리 조회 및 출력
        List<PointHistory> histories = pointService.findPointHistoriesByUserId(userId);
        System.out.println(">>> PointHistory <<< " + histories.toString());

        // 포인트 조회
        UserPoint finalUserPoint = pointService.findUserPointByUserId(userId);
        System.out.println(">>> UserPoint <<< " + finalUserPoint.toString());


        // Assert
        assertEquals(chargeAmount * count, finalUserPoint.point());
    }


    @Test
    @DisplayName("포인트 사용 동시성 테스트")
    void usePoint() {
        //
    }
}
