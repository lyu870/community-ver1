# community-ver1
## 소개
시사뉴스/노래/음악 관련 이야기를 공유하는 게시판기반의 커뮤니티 사이트입니다.  
(Spring Boot 프로젝트)  
<br>

## 핵심 구현기능
- 화면구성 : Thymeleaf 기반의 서버 렌더링 페이지 + fetch 기반의 API 호출을 혼합
- 댓글/대댓글 : 자식 댓글 페이징 + 지연 로딩(lazy paging), 답글 UI(@닉네임멘션 UI)
- 추천 : 토글방식의 게시글추천 + 추천알림 연동
- 알림 : 트랜잭션 afterCommit → Kafka → Consumer → Handler(REQUIRES_NEW) → DLT로 구성(장애격리 + 재처리까지 고려)
- 실시간 : unread-count는 SSE로 실시간 push, 실패 시 폴백(폴링)
- 운영 : 알림 Kafka consumer 재시도 + DLT 적재 + 관리자 전용페이지를 통해 조회/상태 처리
- 보안 : /api/** 전용 Security 체인(JSON 401/403), CSRF dev/prod 토글, 안전 리다이렉트
- 배포 : Docker Compose(dev/prod) + Actuator healthcheck + Nginx리버스 프록시사용(SSE최적화)
<br><br>


## 서비스 기능
### [ 게시판 ]
- NEWS / MUSIC / NOTICE 게시판 CRUD
- 조회수/추천수 집계, 검색/페이지네이션
- 공지사항(Notice)은 권한(ADMIN) 기반으로 작성/수정/삭제 제한

### [ 댓글/대댓글 ]
- 루트댓글 + 자식댓글(대댓글) 구조
- 자식댓글은 더보기 방식의 지연 로딩 + 페이징
- 대댓글 UX → 닉네임멘션 기능 **(@닉네임 댓글내용)**

### [ 추천(토글) ]
- 게시글 추천 토글(중복 추천 방지)
- 추천 이벤트 기반 알림(회원별 마이페이지 설정으로 ON/OFF가능)

### [ 알림(Notifications) ]
- 내 게시글에 댓글 / 내 댓글에 답글 / 내 게시글에 추천 시 알림
- 상단알림 드롭다운 → 알림 읽음처리, 알림 삭제 알림 페이지이동
- 마이페이지 : 알림 설정(댓글/답글/추천) ON/OFF
- 알림 unread-count는 SSE로 실시간 업데이트

### [ 관리자(Admin) ]
- 회원 목록/상세(작성글/작성댓글)
- 관리자 전용페이지에서 Kafka DLT 적재목록/상세 조회 + 상태 처리(미확인/확인/해결)
<br><br>

## 개발 환경 / 기술 스택
### 백엔드
- Java 17
- Spring Boot 3.5.6
- Spring MVC, Spring Security
- Spring Data JPA(Hibernate)
- Flyway
- Spring Data Redis
- Spring Kafka
- Spring Boot Actuator(health 중심)

### 프론트엔드
- Thymeleaf + Layout Dialect
- HTML, CSS, JS(fetch/AJAX)

### 인프라 / 개발운영(DevOps)
- Docker / Docker Compose
- MySQL 8.4
- Redis 7
- Kafka 3.7 (KRaft)
- Nginx Reverse Proxy (SSE 경로 proxy_buffering off)
