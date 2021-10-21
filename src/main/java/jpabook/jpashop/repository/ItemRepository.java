package jpabook.jpashop.repository;


import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if (item.getId() == null) {
            //save
            em.persist(item);
        } else {
            //update
            //merge 는 속성 전부 다 변경 된다.
            // setting 을 안해주면 null 로 변경됌
            //실무에서는 잘 안씀
            em.merge(item);
        }
    }
    public Item findOne(Long id) {
        return em.find(Item.class,id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }
}
