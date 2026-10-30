package com.scaffold.service;

import com.scaffold.common.BusinessException;
import com.scaffold.common.PageResult;
import com.scaffold.entity.Notice;
import com.scaffold.mapper.NoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final NoticeMapper noticeMapper;

    public PageResult<Notice> page(String keyword, int page, int size) {
        int offset = (page - 1) * size;
        long total = noticeMapper.countPage(keyword);
        List<Notice> list = noticeMapper.findPage(keyword, offset, size);
        return PageResult.of(total, list);
    }

    public List<Notice> listAll() {
        return noticeMapper.findAll();
    }

    public Notice getById(Integer id) {
        Notice notice = noticeMapper.findById(id);
        if (notice == null) {
            throw new BusinessException(404, "公告不存在");
        }
        return notice;
    }

    public Notice create(Notice notice, Integer userId) {
        normalize(notice);
        notice.setTime(LocalDateTime.now().format(TIME_FORMAT));
        notice.setUserId(userId);
        noticeMapper.insert(notice);
        return noticeMapper.findById(notice.getId());
    }

    public Notice update(Integer id, Notice notice) {
        getById(id);
        notice.setId(id);
        normalize(notice);
        noticeMapper.update(notice);
        return noticeMapper.findById(id);
    }

    public void delete(Integer id) {
        getById(id);
        noticeMapper.deleteById(id);
    }

    private void normalize(Notice notice) {
        if (notice.getName() != null) {
            String name = notice.getName().trim();
            notice.setName(StringUtils.hasText(name) ? name : null);
        }
        if (!StringUtils.hasText(notice.getName())) {
            throw new BusinessException("公告标题不能为空");
        }
        if (notice.getContent() != null) {
            String content = notice.getContent().trim();
            notice.setContent(StringUtils.hasText(content) ? content : null);
        }
    }
}
