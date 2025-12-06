// SalesOrderRequest.java
package com.hello.community.sales;

import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

@Getter @Setter
public class SalesOrderRequest {
    @NotNull
    private Long itemId;

    @NotNull
    private Integer count;
}
