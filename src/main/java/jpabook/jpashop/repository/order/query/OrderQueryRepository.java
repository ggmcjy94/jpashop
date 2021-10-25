package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    public List<OrderQueryDTO> findOrderQueryDTOS() {
        em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderQueryDTO()" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d" , OrderQueryDTO.class)
                .getResultList();
        )
    }


}
