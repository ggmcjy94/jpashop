package jpabook.jpashop.repository.order.query;

import lombok.Data;

@Data
public class OrderItemQueryDTO {

    private String itemName;
    private int orderPrice;
    private int count;

}
