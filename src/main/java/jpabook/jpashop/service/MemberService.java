package jpabook.jpashop.service;


import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) // 조회 할때 쿼리성능 최적화 , 데이터 변경은 true 하면 안됌
//@AllArgsConstructor // 이거는 생성자를 만들어줌
@RequiredArgsConstructor // final 있는 것만 생성자 만들어줌
public class MemberService {

    private final MemberRepository memberRepository;

////    @Autowired 필없음 생성자 를 생성해서 스프링에서 인젝션 해줌
//    public MemberService(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

    //회원가입
    @Transactional // readonly default false
    public Long join(Member member) {
        validateDuplicateMember(member); // 중복 회원 검증
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        //EXCEPTION
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }
    
    //회원 전체 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }
    
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }
}
