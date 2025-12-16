// RecommendResponseDto.java
package com.hello.community.board.recommend;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecommendResponseDto {

    private boolean recommended;
    private int recommendCount;
}
