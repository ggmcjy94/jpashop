package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    //merge 랑 같음
    //변경감지를 명확하게 setter 는 왠만하면 쓰지말아라
    //파라미터가 많으면 DTO 만들어서 쓰세여
    @Transactional
    public void updateItem(Long itemId, String name , int price, int stockQuantity) {
        Item findItem = itemRepository.findOne(itemId);
        findItem.change(name,price,stockQuantity);
//        findItem.setPrice(param.getPrice());
//        findItem.setName(param.getName());
//        findItem.setStockQuantity(param.getStockQuantity());
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }




    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}
