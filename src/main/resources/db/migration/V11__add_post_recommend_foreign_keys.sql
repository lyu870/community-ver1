-- V11__add_post_recommend_foreign_keys.sql
-- 기존 post_recommend(member_id, post_id)에 유니크 제약이 걸려있고
-- 추천/추천취소는 서비스에서 insert/delete 하기 때문에 기능적으로는 문제가 없었음.
-- 다만 DB FK가 없으면 회원/게시글 삭제 시 레코드가 남을 수 있기 때문에 수정.
-- 운영에서 FK 추가 시 고아 데이터가 있으면 실패하므로, FK 추가 전 고아 레코드를 정리.

-- 0) (안전장치) 고아 레코드 정리: member 없음
DELETE pr
FROM post_recommend pr
LEFT JOIN member m ON m.id = pr.member_id
WHERE m.id IS NULL;

-- 0) (안전장치) 고아 레코드 정리: base_post 없음
DELETE pr
FROM post_recommend pr
LEFT JOIN base_post bp ON bp.id = pr.post_id
WHERE bp.id IS NULL;

-- 1) member_id 단독 인덱스 (FK + 조회 성능용)
ALTER TABLE post_recommend
  ADD INDEX idx_post_recommend_member_id (member_id);

-- 2) member FK (회원 삭제 시 추천도 같이 삭제)
ALTER TABLE post_recommend
  ADD CONSTRAINT fk_post_recommend_member
    FOREIGN KEY (member_id)
    REFERENCES member(id)
    ON DELETE CASCADE;

-- 3) post FK (게시글 삭제 시 추천도 같이 삭제)
ALTER TABLE post_recommend
  ADD CONSTRAINT fk_post_recommend_post
    FOREIGN KEY (post_id)
    REFERENCES base_post(id)
    ON DELETE CASCADE;
