package jpabook.jpashop.service.query;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;
@Getter
public class OrderDTO {
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

