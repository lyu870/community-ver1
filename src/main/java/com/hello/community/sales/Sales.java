// Sales.java
package com.hello.community.sales;

import com.hello.community.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
public class Sales {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String itemName;
    private Integer price;
    private Integer count;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name="member_id",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private Member member; // Member Table 참조

    @CreationTimestamp
    private LocalDateTime created;
}
