// CommunityApplication.java
package com.hello.community;

import com.hello.community.board.music.MusicRepository;
import com.hello.community.board.news.NewsRepository;
import com.hello.community.board.notice.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
@Controller
@RequiredArgsConstructor
public class CommunityApplication {

	private final NoticeRepository noticeRepository;
	private final NewsRepository newsRepository;
	private final MusicRepository musicRepository;

	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
		String start = "서버 정상작동.";
		System.out.println(start);
	}

	@GetMapping("/")
	public String index(Model model) {
		model.addAttribute("recentNotices",
				noticeRepository.findAll(
						PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
				).getContent()
		);

		model.addAttribute("recentNews",
				newsRepository.findAll(
						PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
				).getContent()
		);

		model.addAttribute("recentMusic",
				musicRepository.findAll(
						PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
				).getContent()
		);

		return "index";
	}
}
