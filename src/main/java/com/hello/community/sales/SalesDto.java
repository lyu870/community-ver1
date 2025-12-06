// SalesDto.java
package com.hello.community.sales;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class SalesDto {
    private String title;
    private Integer price;
    private Integer count;
}


