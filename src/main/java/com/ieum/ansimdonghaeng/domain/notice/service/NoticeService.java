package com.ieum.ansimdonghaeng.domain.notice.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.common.response.PageResponse;
import com.ieum.ansimdonghaeng.domain.notice.dto.response.NoticeDetailResponse;
import com.ieum.ansimdonghaeng.domain.notice.dto.response.NoticeSummaryResponse;
import com.ieum.ansimdonghaeng.domain.notice.entity.Notice;
import com.ieum.ansimdonghaeng.domain.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public PageResponse<NoticeSummaryResponse> getNotices(Pageable pageable) {
        Page<Notice> page = noticeRepository.findAllByPublishedYnTrue(pageable);
        return PageResponse.from(page.map(notice -> new NoticeSummaryResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getPublishedAt(),
                notice.getCreatedAt()
        )));
    }

    public NoticeDetailResponse getNotice(Long noticeId) {
        Notice notice = noticeRepository.findDetailById(noticeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));
        if (!notice.isPublished()) {
            throw new CustomException(ErrorCode.NOTICE_NOT_FOUND);
        }
        return new NoticeDetailResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getPublishedAt()
        );
    }
}
