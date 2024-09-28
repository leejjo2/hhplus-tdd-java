package io.hhplus.tdd.ref;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class OrderServiceTest {

    // 주문번호는 save 결과의 orderId, 유저, 상품Id 는 요청 파라미터와 동일, 상태는 PAID, orderAmount는 상품의 금액이어야 한다.

    @Test
    void successOrder() {
        OrderService sut = new OrderService(
                new UserRepository() {
                    @Override
                    public User findById(Long userId) {
                        return new User(userId, new BigDecimal("10000"));
                    }
                },
                new ProductRepository() {
                    @Override
                    public Product findById(Long productId) {
                        return new Product(productId, new BigDecimal("3000"), 100L);
                    }
                },
                new OrderRepository() {
                    @Override
                    public Order save(Order order) {
                        order.orderId = 1L;
                        return order;
                    }
                }
        );

        Long userId = 1L;
        Long productId = 100L;
        Order order = sut.createOrder(productId, userId);

        assert order != null;
        assert order.orderId == 1L;
        assert order.productId.equals(productId);
        assert order.userId.equals(userId);
        assert order.status.equals(Order.Status.PAID);
    }

    @Test
    void failedOrder_stock() {
        OrderService sut = new OrderService(
                new UserRepository() {
                    @Override
                    public User findById(Long userId) {
                        return new User(userId, new BigDecimal("10000"));
                    }
                },
                new ProductRepository() {
                    @Override
                    public Product findById(Long productId) {
                        return new Product(productId, new BigDecimal("3000"), 0L);
                    }
                },
                new OrderRepository() {
                    @Override
                    public Order save(Order order) {
                        order.orderId = 1L;
                        return order;
                    }
                }
        );

        Long userId = 1L;
        Long productId = 100L;


        Exception e = null;

        try {
            sut.createOrder(productId, userId);
        } catch (Exception exception) {
            e = exception;
        }

        assert e instanceof InsufficientStockException;
    }

    @Test
    void failedOrder_balance() {
        OrderService sut = new OrderService(
                new UserRepository() {
                    @Override
                    public User findById(Long userId) {
                        return new User(userId, new BigDecimal("2000"));
                    }
                },
                new ProductRepository() {
                    @Override
                    public Product findById(Long productId) {
                        return new Product(productId, new BigDecimal("3000"), 999L);
                    }
                },
                new OrderRepository() {
                    @Override
                    public Order save(Order order) {
                        order.orderId = 1L;
                        return order;
                    }
                }
        );

        Long userId = 1L;
        Long productId = 100L;


        Exception e = null;

        try {
            sut.createOrder(productId, userId);
        } catch (Exception exception) {
            e = exception;
        }

        assert e instanceof InsufficientBalanceException;
    }
}