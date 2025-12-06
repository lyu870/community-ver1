// Item.java
package com.hello.community.board.item;

import com.hello.community.board.common.BasePost;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "item")
public class Item extends BasePost {
    private Integer price;
}