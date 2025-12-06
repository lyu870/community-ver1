// MemberDto.java
package com.hello.community.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDto {
    String username;
    String displayName;
    Long id;

    MemberDto(String a, String b){
        this.username = a;
        this.displayName = b;
    }
    MemberDto(String a, String b, Long id){
        this.username = a;
        this.displayName = b;
        this.id = id;
    }
}