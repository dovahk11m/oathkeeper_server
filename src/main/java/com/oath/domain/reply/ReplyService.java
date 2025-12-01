package com.oath.domain.reply;

import com.oath.common.exception.Exception403;
import com.oath.common.exception.Exception404;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.review.Review;
import com.oath.domain.review.ReviewJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyJpaRepository replyJpaRepository;
    private final ReviewJpaRepository reviewJpaRepository;
    private final MemberRepository memberRepository;

    // 댓글 조회
    @Transactional(readOnly = true)
    public Reply getReplyById(Long replyId) {
        return replyJpaRepository.findByIdWithDetails(replyId)
                .orElseThrow(() -> new Exception404("해당 댓글을 찾을 수 없습니다."));
    }

    // 댓글 생성
    @Transactional
    public Reply createReply(Long memberId, Long reviewId, String content) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new Exception404("해당 멤버를 찾을 수 없습니다."));

        Review review = reviewJpaRepository.findById(reviewId)
                .orElseThrow(() -> new Exception404("해당 후기를 찾을 수 없습니다."));

        Reply reply = Reply.builder()
                .author(member)
                .review(review)
                .content(content)
                .build();

        return replyJpaRepository.save(reply);
    }

    // 댓글 수정
    @Transactional
    public Reply updateReply(Long replyId, Long requesterId, String content) {
        Reply reply = getReplyById(replyId);

        // 작성자 본인만 수정 가능
        validateAuthor(reply, requesterId);

        reply.update(content);
        return reply; // dirty checking으로 자동 업데이트
    }

    // 댓글 삭제
    @Transactional
    public void deleteReply(Long replyId, Long requesterId) {
        Reply reply = getReplyById(replyId);

        // 작성자 본인만 삭제 가능
        validateAuthor(reply, requesterId);

        replyJpaRepository.delete(reply);
    }

    // 작성자 권한 검증
    private void validateAuthor(Reply reply, Long memberId) {
        if (!reply.getAuthor().getId().equals(memberId)) {
            throw new Exception403("작성자만 수정/삭제할 수 있습니다.");
        }
    }
}

