package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.point.aggregate.entity.PointHistory;
import io.hhplus.tdd.point.aggregate.entity.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/point")
public class PointController {

    private final PointService pointService;
    private static final Logger log = LoggerFactory.getLogger(PointController.class);


    /**
     * 특정 사용자 ID에 대한 포인트 정보를 반환합니다.
     *
     * @param id 사용자 ID
     * @return UserPoint 사용자 포인트 정보
     */
    @GetMapping("{id}")
    public UserPoint getUserPoint(@PathVariable long id) {
        log.info("User point requested for user ID: {}", id);
        return this.pointService.findUserPointByUserId(id);
    }

    /**
     * 특정 사용자 ID에 대한 포인트 충전 및 사용 이력을 반환합니다.
     *
     * @param id 사용자 ID
     * @return List<PointHistory> 포인트 이력 목록
     */
    @GetMapping("{id}/histories")
    public List<PointHistory> getPointHistory(@PathVariable long id) {
        log.info("Point history requested for user ID: {}", id);
        return this.pointService.findPointHistoriesByUserId(id);
    }

    /**
     * 특정 사용자 ID에 대한 포인트 충전을 수행합니다.
     *
     * @param id     사용자 ID
     * @param amount 충전할 포인트 양
     * @return UserPoint 충전 후의 사용자 포인트 정보
     */
    @PatchMapping("{id}/charge")
    public UserPoint chargeUserPoint(@PathVariable long id, @RequestBody long amount) {
        log.info("Charging {} points for user ID: {}", amount, id);
        return this.pointService.chargeUserPoint(id, amount);
    }

    /**
     * 특정 사용자 ID에 대한 포인트 사용을 수행합니다.
     *
     * @param id     사용자 ID
     * @param amount 사용할 포인트 양
     * @return UserPoint 사용 후의 사용자 포인트 정보
     */
    @PatchMapping("{id}/use")
    public UserPoint useUserPoint(@PathVariable long id, @RequestBody long amount) {
        log.info("Using {} points for user ID: {}", amount, id);
        return this.pointService.useUserPoint(id, amount);
    }
}
