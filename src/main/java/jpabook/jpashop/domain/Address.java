package jpabook.jpashop.domain;


import lombok.Getter;

import javax.persistence.Embeddable;
//테이블 컬럼 값에 넣기 위한
@Embeddable
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    protected Address() {
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
