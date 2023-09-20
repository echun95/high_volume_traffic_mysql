package com.example.fastcampusmysql.domain.member;

import com.example.fastcampusmysql.domain.member.entity.Member;
import com.example.fastcampusmysql.util.MemberFixureFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.LongStream;

public class MemberTest {


    @Test
    @DisplayName("회원 닉네임을 변경할 수 있다.")
    void testChangeName() throws Exception {
        Member member = MemberFixureFactory.create();
        String expected = "chun";
        member.changeNickname(expected);

        Assertions.assertThat(member.getNickname()).isEqualTo(expected);
    }

    @Test
    @DisplayName("회원 닉네임은 10자를 넘을 수 없습니다.")
    void testNicknameMaxLength() throws Exception {
        Member member = MemberFixureFactory.create();
        String overMaxLengthName = "chun1234456789";

        Assertions.assertThatThrownBy(() -> member.changeNickname(overMaxLengthName))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
