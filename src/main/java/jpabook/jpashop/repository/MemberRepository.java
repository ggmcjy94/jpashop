package jpabook.jpashop.repository;


import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {


    // spring
    // jpa
    // spring data jpa
    // querydsl

    //select m from Member m where m.name = ?;
    List<Member> findByName(String name);
}
