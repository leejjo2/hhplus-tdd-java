package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.aggregate.entity.PointHistory;
import io.hhplus.tdd.point.aggregate.entity.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PointServiceImplConcurrencyTest {

    private static final Logger logger = LoggerFactory.getLogger(PointServiceImplConcurrencyTest.class);

    @Autowired
    private PointServiceImpl pointService;

    @Test
    @DisplayName("단일 유저 포인트 충전, 사용 동시성 테스트 - CompletableFuture 사용")
    void single_user_charge_point_with_completable_future() {
        // Given
        long userId = 1L;
        long chargeAmount = 100L;
        long useAmount = 50L;
        int count = 10;
        long balance = pointService.findUserPointByUserId(userId).point();

        // CompletableFuture 배열 생성
        CompletableFuture<Void>[] futures = new CompletableFuture[count];

        // When
        for (int i = 0; i < count; i++) {
            futures[i] = CompletableFuture.runAsync(() -> {
                pointService.chargeUserPoint(userId, chargeAmount);
                pointService.useUserPoint(userId, useAmount);
            });
        }

        // 모든 작업이 완료될 때까지 대기
        CompletableFuture.allOf(futures).join();

        // 포인트 히스토리 조회 및 출력
        print_point_history(userId);

        // 포인트 조회
        UserPoint finalUserPoint = pointService.findUserPointByUserId(userId);
        logger.info("\n>>> UserPoint <<< {}", finalUserPoint.toString());

        // Then
        assertEquals(balance + chargeAmount * count - useAmount * count, finalUserPoint.point());
    }


    @Test
    @DisplayName("여러 유저 포인트 충전, 사용 동시성 테스트 - CompletableFuture 사용")
    void multi_users_charge_point_with_completable_future() {
        // Given
        long userId = 1L;
        long chargeAmount = 100L;

        long user2Id = 2L;
        long chargeAmount2 = 1000L;

        int count = 10;
        long balance = pointService.findUserPointByUserId(userId).point();
        long balance2 = pointService.findUserPointByUserId(user2Id).point();

        // CompletableFuture 배열 생성
        CompletableFuture<Void>[] futures = new CompletableFuture[count];

        // When
        for (int i = 0; i < count; i++) {
            futures[i] = CompletableFuture.runAsync(() -> {
                pointService.chargeUserPoint(userId, chargeAmount);
                pointService.chargeUserPoint(user2Id, chargeAmount2);
            });
        }

        // 모든 작업이 완료될 때까지 대기
        // 포인트 히스토리 조회 및 출력
        CompletableFuture.allOf(futures).join();


        // 포인트 조회
        UserPoint finalUserPoint = pointService.findUserPointByUserId(userId);
        UserPoint finalUser2Point = pointService.findUserPointByUserId(user2Id);

        print_point_history(userId);
        logger.info("\n>>> UserPoint <<< {}", finalUserPoint.toString());
        print_point_history(user2Id);
        logger.info("\n>>> UserPoint <<< {}", finalUser2Point.toString());

        // Then
        assertEquals(balance + chargeAmount * count, finalUserPoint.point());
        assertEquals(balance2 + chargeAmount2 * count, finalUser2Point.point());
    }


    @Test
    @DisplayName("포인트 충전 동시성 테스트 - CountDownLatch 사용")
    void charge_point_with_count_down_latch() throws InterruptedException {
        // Given
        long userId = 1L;
        long chargeAmount = 100L;
        int count = 10;
        long balance = pointService.findUserPointByUserId(userId).point();

        // CountDownLatch 초기화 (스레드 수만큼 카운트)
        CountDownLatch latch = new CountDownLatch(count);

        // When
        ExecutorService executorService = Executors.newFixedThreadPool(count);
        for (int i = 0; i < count; i++) {
            executorService.submit(() -> {
                try {
                    pointService.chargeUserPoint(userId, chargeAmount);
                } finally {
                    // 스레드 작업이 끝날 때마다 latch 카운트 감소
                    latch.countDown();
                }
            });
        }

        // 모든 스레드가 종료될 때까지 대기
        latch.await(20, TimeUnit.SECONDS);
        executorService.shutdown();

        // 포인트 히스토리 조회 및 출력
        print_point_history(userId);

        // 포인트 조회
        UserPoint finalUserPoint = pointService.findUserPointByUserId(userId);
        logger.info(">>> UserPoint <<< {}", finalUserPoint.toString());

        // Then
        assertEquals(balance + chargeAmount * count, finalUserPoint.point());
    }

    @Test
    @DisplayName("포인트 사용 동시성 테스트")
    void use_point_concurrently() {
        // Given
        long userId = 1L;
        long initialAmount = 10000L;
        long useAmount = 100L;
        int count = 10;
        long balance = pointService.findUserPointByUserId(userId).point();

        // 초기 포인트 충전
        pointService.chargeUserPoint(userId, initialAmount);

        // CompletableFuture 배열 생성
        CompletableFuture<Void>[] futures = new CompletableFuture[count];

        // When
        for (int i = 0; i < count; i++) {
            futures[i] = CompletableFuture.runAsync(() -> pointService.useUserPoint(userId, useAmount));
        }

        // 모든 작업이 완료될 때까지 대기
        CompletableFuture.allOf(futures).join();

        // 포인트 히스토리 조회 및 출력
        print_point_history(userId);

        // 포인트 조회
        UserPoint finalUserPoint = pointService.findUserPointByUserId(userId);
        logger.info("\n >>> UserPoint <<< {}", finalUserPoint.toString());

        // Then
        assertEquals(balance + initialAmount - useAmount * count, finalUserPoint.point());
    }

    private void print_point_history(long userId) {
        List<PointHistory> histories = pointService.findPointHistoriesByUserId(userId);
        logger.info("\n>>> PointHistory <<< {}", histories.toString());
    }
}
