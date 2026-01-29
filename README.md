# community-ver1
## 소개
시사뉴스/노래/음악 관련 이야기를 공유하는 게시판기반의 커뮤니티 사이트입니다.
(Spring Boot 프로젝트)
<br>

## 핵심 구현기능
1. 댓글 지연로딩(lazy loading)을 통한 페이지로딩속도 향상.
2. 이메일 인증번호 발행시스템(redis)
3. 댓글/추천이 DB에 저장된 뒤 Kafka로 알림이벤트를 발행하고, Consumer가 알림을 별도의 트랜잭션으로 저장.
4. kafka Consumer 처리 실패 시 재시도 후 DLT로 분기, DLT메시지를 DB에 저장 (관리자페이지 → 조회/상태 처리)
5. 알림 목록은 REST로 조회, 미확인 개수(unread-count)는 SSE로 실시간 갱신.(실패 시 폴링 폴백)
6. Docker Compose(dev/prod분리) + Actuator healthcheck(컨테이너 정상동작중인지 체크) + Nginx리버스 프록시사용(외부는 80/443만 열고 서비스는 숨김. SSE동작고려.)
  <br><br>


## 서비스 기능
### [ 게시판 ]
- NEWS / MUSIC / NOTICE 게시판 CRUD
- 조회수/추천수 집계, 검색/페이지네이션
- 공지사항(Notice)은 권한(ADMIN) 기반으로 작성/수정/삭제 제한

### [ 댓글/대댓글 ]
- 루트댓글 + 자식댓글(대댓글) 구조
- 자식댓글은 더보기 방식의 지연 로딩 + 페이징
- 대댓글 UX → 닉네임멘션 기능 (@닉네임 댓글내용)

### [ 추천(토글) ]
- 게시글 추천 토글(중복 추천 방지)
- 추천 이벤트 기반 알림(회원별 마이페이지 설정으로 ON/OFF가능)

### [ 알림(Notifications) ]
- 내 게시글에 댓글 / 내 댓글에 답글 / 내 게시글에 추천 시 알림
- 상단알림 드롭다운 → 알림 읽음처리, 알림 삭제 알림 페이지이동
- 마이페이지 : 알림 설정(댓글/답글/추천) ON/OFF
- 알림 unread-count는 SSE로 실시간 업데이트

### [ 관리자(Admin) ]
- 회원 목록/상세 확인가능. (회원별 아이디, 닉네임, 가입날짜, 메일주소 + 작성글/작성댓글)
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

### 주요 사용 라이브러리
- Flyway (DB Migration)
- Lombok
- Spring for Apache Kafka (Spring Kafka)
- Spring Data Redis
- Spring Boot Actuator (Health)
- Spring Boot Mail (SMTP)
- Thymeleaf
- Thymeleaf Layout Dialect (3.2.1)
- Thymeleaf Extras Spring Security 6

### 인프라 / 개발운영(DevOps)
- Docker / Docker Compose
- MySQL 8.4
- Redis 7
- Kafka 3.7 (KRaft)
- Nginx Reverse Proxy (SSE 경로 proxy_buffering off)
