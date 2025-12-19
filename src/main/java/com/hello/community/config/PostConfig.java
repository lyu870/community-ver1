// PostConfig.java
package com.hello.community.config;

import com.hello.community.board.common.BasePostService;
import com.hello.community.board.music.Music;
import com.hello.community.board.music.MusicRepository;
import com.hello.community.board.news.News;
import com.hello.community.board.news.NewsRepository;
import com.hello.community.board.notice.Notice;
import com.hello.community.board.notice.NoticeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PostConfig {

    @Bean
    public BasePostService<Music> musicPostService(MusicRepository repo) {
        return new BasePostService<>(repo);
    }

    @Bean
    public BasePostService<News> newsPostService(NewsRepository repo) {
        return new BasePostService<>(repo);
    }

    @Bean
    public BasePostService<Notice> noticePostService(NoticeRepository repo) {
        return new BasePostService<>(repo);
    }
}
