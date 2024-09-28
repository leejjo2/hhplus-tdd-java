package io.hhplus.tdd.ref;

import java.math.BigDecimal;

public class OrderService {

    public OrderService(UserRepository userRepository, ProductRepository productRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    private UserRepository userRepository;
    private ProductRepository productRepository;
    private OrderRepository orderRepository;


    // 상품의 재고를 차감한다.
    // 유저의 잔액을 주문금액만큼 차감한다.
    // 주문을 생성한다. 주문번호 유저, 상품, 상태, 주문금액 데이터가 있어야 함.
    // 생성된 주문의 번호를 반환한다.
    // 주문번호는 save 결과의 orderId, 유저, 상품Id 는 요청 파라미터와 동일, 상태는 PAID, orderAmount는 상품의 금액이어야 한다.

    // 존재하지 않는 상품, 유저의 경우 IllegalArgumentException
    // 재고차감에 실패했다면 InsufficientStockException
    // 유저의 잔액이 부족하다면 InsufficientBalanceException

    public Order createOrder(Long productId, Long userId) {
        Product product = productRepository.findById(productId);
        User user = userRepository.findById(userId);

        if (product == null || user == null) {
            throw new IllegalArgumentException();
        }
        product.deductStock();
        user.deductBalance(product.price);
        return orderRepository.save(new Order(user.userId, product.productId, product.price));
    }

}

class Order {
    public Order(Long userId, Long productId, BigDecimal orderAmount) {
        this.userId = userId;
        this.productId = productId;
        this.orderAmount = orderAmount;
        this.status = Status.PAID;
    }

    Long orderId;
    Long userId;
    Long productId;
    Status status;
    BigDecimal orderAmount;

    enum Status {
        PAID, SHIPPED, DELIVERED, CANCELED
    }
}

interface UserRepository {

    User findById(Long userId);
}

interface ProductRepository {

    Product findById(Long productId);
}

class Product {
    public Product(Long productId, BigDecimal price, Long stock) {
        this.productId = productId;
        this.price = price;
        this.stock = stock;
    }

    public void deductStock() {
        if (stock == 0) {
            throw new InsufficientStockException();
        }
        stock--;
    }

    Long productId;
    BigDecimal price;
    Long stock;
}

class User {
    public User(Long userId, BigDecimal balance) {
        this.userId = userId;
        this.balance = balance;

        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException();
        }
    }

    public void deductBalance(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }
        balance = balance.subtract(amount);
    }

    Long userId;
    BigDecimal balance;
}


interface OrderRepository {
    Order save(Order order);
}

class InsufficientStockException extends RuntimeException {
}

class InsufficientBalanceException extends RuntimeException {
}
