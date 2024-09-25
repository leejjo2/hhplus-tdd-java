package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.aggregate.entity.PointHistory;
import io.hhplus.tdd.point.aggregate.entity.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PointServiceImplTest {

    @Autowired
    private PointService pointService;

    @Test
    @DisplayName("포인트 충전 동시성 테스트 - CompletableFuture 사용")
    void chargePointWithCompletableFuture() {
        // Setup
        long userId = 1L;
        long chargeAmount = 100;
        int count = 10;

        // CompletableFuture 배열 생성
        CompletableFuture<Void>[] futures = new CompletableFuture[count];

        // Action
        for (int i = 0; i < count; i++) {
            futures[i] = CompletableFuture.runAsync(() -> {
                pointService.chargeUserPoint(userId, chargeAmount);
//                pointService.chargeUserPointWithoutLock(userId, chargeAmount);
            });
        }

        // 모든 작업이 완료될 때까지 대기
        CompletableFuture.allOf(futures).join();

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
    @DisplayName("포인트 충전 동시성 테스트 - CountDownLatch 사용")
    void chargePointWithCountDownLatch() throws InterruptedException {
        // Setup
        long userId = 1L;
        long chargeAmount = 100;
        int count = 10;

        // CountDownLatch 초기화 (스레드 수만큼 카운트)
        CountDownLatch latch = new CountDownLatch(count);

        // Action
        ExecutorService executorService = Executors.newFixedThreadPool(count);

        for (int i = 0; i < count; i++) {
            executorService.submit(() -> {
                try {
                    pointService.chargeUserPoint(userId, chargeAmount);
//                    pointService.chargeUserPointWithoutLock(userId, chargeAmount);
                } finally {
                    // 스레드 작업이 끝날 때마다 latch 카운트 감소
                    latch.countDown();
                }
            });
        }

        // 모든 스레드가 종료될 때까지 대기
        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

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
