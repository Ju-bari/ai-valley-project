package com.rally.ai_valley.domain.board.repository;

import com.rally.ai_valley.domain.board.dto.BoardInfoResponse;
import com.rally.ai_valley.domain.board.dto.BoardsInCloneResponse;
import com.rally.ai_valley.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// IsDeleted
@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("""
            SELECT b
            FROM Board b
            WHERE b.id = :boardId
                AND b.isDeleted = 0
        """)
    Optional<Board> findBoardById(@Param("boardId") Long boardId);

    @Query("""
            SELECT new com.rally.ai_valley.domain.board.dto.BoardInfoResponse(b.id, b.name, u.nickname, b.description,
                        (SELECT COUNT(DISTINCT cb.clone.id) FROM CloneBoard cb WHERE b.id = cb.board.id AND cb.isActive = 1),
                        (SELECT COUNT(p) FROM Post p WHERE p.board.id = b.id AND p.isDeleted = 0),
                        (SELECT COUNT(r) FROM Reply r INNER JOIN r.post p WHERE p.board.id = b.id AND p.isDeleted = 0 AND r.isDeleted = 0),
                        b.createdAt, b.updatedAt)
            FROM Board b
            JOIN b.createdBy u
            WHERE b.id = :boardId
                AND b.isDeleted = 0
            """)
    BoardInfoResponse findBoardByBoardId(@Param("boardId") Long boardId);

    @Query("""
            SELECT new com.rally.ai_valley.domain.board.dto.BoardInfoResponse(b.id, b.name, u.nickname, b.description,
                        (SELECT COUNT(DISTINCT cb.clone.id) FROM CloneBoard cb WHERE b.id = cb.board.id AND cb.isActive = 1),
                        (SELECT COUNT(p) FROM Post p WHERE p.board.id = b.id AND p.isDeleted = 0),
                        (SELECT COUNT(r) FROM Reply r INNER JOIN r.post p WHERE p.board.id = b.id AND p.isDeleted = 0 AND r.isDeleted = 0),
                        b.createdAt, b.updatedAt)
            FROM Board b
            JOIN b.createdBy u
            WHERE b.isDeleted = 0
            """)
    List<BoardInfoResponse> findAllBoards();

    // 나의 클론들이 속한 게시판들의 모음
    @Query("""
            SELECT new com.rally.ai_valley.domain.board.dto.BoardInfoResponse(b.id, b.name, u.nickname, b.description,
                        (SELECT COUNT(DISTINCT cb.clone.id) FROM CloneBoard cb WHERE b.id = cb.board.id AND cb.isActive = 1),
                        (SELECT COUNT(p) FROM Post p WHERE p.board.id = b.id AND p.isDeleted = 0),
                        (SELECT COUNT(r) FROM Reply r INNER JOIN r.post p WHERE p.board.id = b.id AND p.isDeleted = 0 AND r.isDeleted = 0),
                        b.createdAt, b.updatedAt)
            FROM Board b
            JOIN b.createdBy u
            JOIN b.cloneBoards cb
            WHERE cb.clone.user.id = :userId
                AND cb.isActive = 1
                AND b.isDeleted = 0
            """)
    List<BoardInfoResponse> findCreatedByMyBoards(@Param("userId") Long userId);

    @Query("""
            SELECT new com.rally.ai_valley.domain.board.dto.BoardsInCloneResponse(cb.board.id, cb.clone.id, b.name, b.description, u.nickname, b.createdAt, b.updatedAt)
            FROM Board b
            JOIN CloneBoard cb on cb.board.id = b.id
            JOIN b.createdBy u
            WHERE cb.clone.id = :cloneId
                AND b.isDeleted = 0
                AND cb.isActive = 1
            """)
    List<BoardsInCloneResponse> findBoardsInCloneByCloneId(@Param("cloneId") Long cloneId);

}
