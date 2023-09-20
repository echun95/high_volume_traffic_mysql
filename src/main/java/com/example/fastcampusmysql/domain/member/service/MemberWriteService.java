package com.example.fastcampusmysql.domain.member.service;

import com.example.fastcampusmysql.domain.member.dto.RegisterMemberCommand;
import com.example.fastcampusmysql.domain.member.entity.Member;
import com.example.fastcampusmysql.domain.member.entity.MemberNicknameHistory;
import com.example.fastcampusmysql.domain.member.repository.MemberNicknameRepository;
import com.example.fastcampusmysql.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberWriteService {
    private final MemberRepository memberRepository;
    private final MemberNicknameRepository memberNicknameRepository;

    public Member create(RegisterMemberCommand command){
        Member member = Member.builder()
                .nickname(command.nickname())
                .email(command.email())
                .birthday(command.birthday())
                .build();
        Member savedMember = memberRepository.save(member);
        saveMemberNicknameHistroy(savedMember);
        return savedMember;
    }


    public void changeNickname(Long memberId, String nickName){
        Member findMember = memberRepository.findById(memberId).orElseThrow();
        findMember.changeNickname(nickName);
        memberRepository.save(findMember);

        saveMemberNicknameHistroy(findMember);
    }

    private void saveMemberNicknameHistroy(Member member) {
        MemberNicknameHistory history = MemberNicknameHistory.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .build();
        memberNicknameRepository.save(history);
    }
}
