// BasePostRepository.java
package com.hello.community.board.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BasePostRepository<T extends BasePost> extends JpaRepository<T, Long> {
}
