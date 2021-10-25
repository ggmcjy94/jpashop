package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDTO;
import jpabook.jpashop.repository.order.query.OrderQueryDTO;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import jpabook.jpashop.service.query.OrderDTO;
import jpabook.jpashop.service.query.OrderQueryService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    // 둘을 나눠서 개발 osiv
    // orderservice: 핵심 비즈니스 로직
    // orderQueryService : 화면이나 api에 맞춘 서비스 (주로 읽기 전용 트랜잭션 사용)
    // 참고 고객은 서비스 기반의 트래픽이 많은 실시간 api 는 osiv 를 끄고 (끄는 이유 트래픽이 많으면 데이터베이스 커넥션이 말라버림)
    // ADMIN처럼 커넥션을 많이 사용하지 않는 곳에서는 osiv를 켠다.

    //권장 순서
    // 1. 엔티티 조회 방식으로 우선 접근
    // 1-1 페치조인으로 쿼리수를 최적화
    // 1-2 컬렉션 최적화
    // 1-2-1 페이징 필요 .yaml fetch_size v3.1
    // 1-2-2 페이징 필요  x 페치 조인
    // 2.엔티티 조회 방식으로 해결이 안되면 DTO 조회 방식 사용
    // 3. DTO 조회 방식으로 해결이 안되면 NativeSQL or 스프링 jdbcTemplate 사용


    @GetMapping("/api/v1/orders") //엔티티를 조회해서 그대로 반환
    public List<Order> ordersV1() { //엔티티 노출 안좋다.
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) { //hibernate5 강제 초기화
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;

    }

    @GetMapping("/api/v2/orders")// 엔티티 조회후 dto 로 변환후 반환
    public List<OrderDTO> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDTO> result = orders.stream().map(o -> new OrderDTO(o)).collect(toList());
        return result;
    }

    @GetMapping("/api/v3/orders")// 성능이 안나올때 페치 조인으로 쿼리수 최적화 해서 반환
    public List<OrderDTO> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem(); //1 대 다 를 페치조인 하는 순간 페이징 이 불가능하다.
        List<OrderDTO> result = orders.stream().map(o -> new OrderDTO(o)).collect(toList());
        return result;
    }

    //open-session-in-view 이건 공부해야 될듯..
    private final OrderQueryService orderQueryService;
    @GetMapping("/api/v3/orders/osiv")// open-session-in-view를 끄면 이런식으로 하세여 왜냐 service repository 해서 처리하고 와야한다. 이둘에서만 지연로딩이 살아있기때문에
    public List<jpabook.jpashop.service.query.OrderDTO> ordersV3Osiv() {
        return orderQueryService.ordersV3Osiv();

    }

    @GetMapping("/api/v3.1/orders")// 페치 조인으로 페이징불가능을 가능 하게 반환
    public List<OrderDTO> ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                        @RequestParam(value = "limit", defaultValue= "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit); // to one 은 페치조인 해서 가져온다.

        List<OrderDTO> result = orders.stream().map(o -> new OrderDTO(o)).collect(toList());
        return result;
    }

    @GetMapping("/api/v4/orders")// jpa 에서 DTO 를 직접 조회
    public List<OrderQueryDTO> ordersV4(){
        return orderQueryRepository.findOrderQueryDTOS();
    }

    @GetMapping("/api/v5/orders") //query 2번 //컬렉션 조회 최적화 - 일대다 관계인 컬렉션은 in 절을 활용해서 메모리에 미리 조회해서 최적화후 반환
    public List<OrderQueryDTO> ordersV5(){
        return orderQueryRepository.findAllByDto_optimization();
    }

    //query 1번 중복 //데이터가 추가되므로 상황에 따라 v5 보다 더 느릴 수도 있다. //애플리케이션에서 추가 작업이 크다 //order 를 기준으로 페이징 불가능 //데이터가 무수히 많을때 이용
    @GetMapping("/api/v6/orders") // 플랫 데이터 최적화 - join 결과를 그대로 조회 후 애플리케이션에서 원한는 모양으로 직접변환 후 반환
    public List<OrderQueryDTO> ordersV6(){
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDTO(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDTO(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDTO(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(toList()); //람다 코드로 직접 구조 맞춘거 flats 를 가지고
    }

    @Getter
    static class OrderDTO { //dto 반환
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDTO> orderItems;
        public OrderDTO(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); //여기서 open-in-view could not initialize proxy 에러남
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();//여기서 open-in-view could not initialize proxy 에러남
            orderItems = order.getOrderItems().stream().map(orderItem -> new OrderItemDTO(orderItem)).collect(toList());
        }
    }
    @Getter
    static class OrderItemDTO{
        private String itemName; //상품명
        private int orderPrice; //주문 가격
        private int count; //주문 수량
        public OrderItemDTO(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

}
