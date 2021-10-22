package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.OrderSimpleQueryDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


/**
 * xToOne (many to one, one to one)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all= orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress();// Lazy 강제 초기화
        }
        return all;
    }
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDTO> ordersV2() { // 참고로 List 로 감싸면 안되고 result 로 해야된다
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDTO> result = orders.stream()
                .map(o -> new SimpleOrderDTO(o))
                .collect(Collectors  .toList());
        return result;
    }


    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDTO> ordersV3() { //fetch join 지연 로딩 x 재사용성 가능
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDTO> result = orders.stream().map(o -> new SimpleOrderDTO(o)).collect(Collectors.toList());
        return result;
    }


    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDTO> ordersV4() {  // 재사용성 불가 하지만 성능은 v3 보다 좋음
        return orderRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDTO {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDTO(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();

        }
    }

}
