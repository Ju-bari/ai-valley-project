package com.rally.ai_valley.domain.board.service;

import com.rally.ai_valley.common.exception.CustomException;
import com.rally.ai_valley.common.exception.ErrorCode;
import com.rally.ai_valley.domain.board.dto.BoardCreateRequest;
import com.rally.ai_valley.domain.board.dto.BoardInfoResponse;
import com.rally.ai_valley.domain.board.dto.BoardSubscriptionRequest;
import com.rally.ai_valley.domain.board.dto.BoardsInCloneResponse;
import com.rally.ai_valley.domain.board.entity.Board;
import com.rally.ai_valley.domain.board.repository.BoardRepository;
import com.rally.ai_valley.domain.clone.entity.Clone;
import com.rally.ai_valley.domain.clone.entity.CloneBoard;
import com.rally.ai_valley.domain.clone.repository.CloneBoardRepository;
import com.rally.ai_valley.domain.clone.repository.CloneRepository;
import com.rally.ai_valley.domain.user.entity.User;
import com.rally.ai_valley.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardService {

    private final BoardRepository boardRepository;
    private final CloneRepository cloneRepository;
    private final CloneBoardRepository cloneBoardRepository;
    private final UserRepository userRepository;


    private Board getBoardById(Long boardId) {
        return boardRepository.findBoardById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createBoard(Long userId, BoardCreateRequest boardCreateRequest) {
        User findUser = userRepository.findUserById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Board createboard = Board.create(findUser,
                boardCreateRequest.getName(),
                boardCreateRequest.getDescription());

        boardRepository.save(createboard);

        return createboard.getId();
    }

    @Transactional(readOnly = true)
    public List<BoardInfoResponse> getAllBoardsInfo() {
        return boardRepository.findAllBoards();
    }

    @Transactional(readOnly = true)
    public BoardInfoResponse getBoardInfo(Long boardId) {
        return boardRepository.findBoardByBoardId(boardId);
    }

    @Transactional(readOnly = true)
    public List<BoardInfoResponse> getMyBoards(Long userId) {
        return boardRepository.findCreatedByMyBoards(userId);
    }

    @Transactional(readOnly = true)
    public List<BoardsInCloneResponse> getBoardsInClone(Long cloneId) {
        return boardRepository.findBoardsInCloneByCloneId(cloneId);
    }

    // 순한 참조 문제
    @Transactional(rollbackFor = Exception.class)
    public Integer addCloneToBoard(Long boardId, BoardSubscriptionRequest boardSubscriptionRequest) {
        Long cloneId = boardSubscriptionRequest.getCloneId();

        Optional<CloneBoard> optionalCloneBoard = cloneBoardRepository.findCloneBoardByCloneIdAndBoardId(boardId, cloneId);
//        log.info("구독 시도 되었습니다 {} -> {}", cloneId, boardId);

        if (optionalCloneBoard.isPresent()) {
            // 데이터가 이미 존재하는 경우
            CloneBoard existingCloneBoard = optionalCloneBoard.get();
            if (existingCloneBoard.getIsActive() == 1) {
                // 이미 활성화 상태라면, 예외 처리
                throw new CustomException(ErrorCode.ALREADY_ACTIVATED);
            } else {
                // 비활성화(soft-deleted) 상태라면, 다시 활성화(reactivate)
                existingCloneBoard.reactivate();
            }
        } else {
            // 데이터가 아예 존재하지 않는 경우
            Clone findClone = cloneRepository.findById(cloneId)
                    .orElseThrow(() -> new CustomException(ErrorCode.CLONE_NOT_FOUND)); // 순한 참조 방지를 위해 repository 직접 호출
            Board findBoard = getBoardById(boardId);

            CloneBoard cloneBoard = CloneBoard.create(findClone, findBoard);
            cloneBoardRepository.save(cloneBoard);
        }

        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer removeCloneFromBoard(Long boardId, BoardSubscriptionRequest boardSubscriptionRequest) {
        CloneBoard findCloneBoard = cloneBoardRepository
                .findCloneBoardByCloneIdAndBoardId(boardId, boardSubscriptionRequest.getCloneId())
                .orElseThrow(() -> new IllegalArgumentException("해당 클론-보드 관계를 찾을 수 없습니다."));

        findCloneBoard.softDelete();

        return 1;
    }

}
