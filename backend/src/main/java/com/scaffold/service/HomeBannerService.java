package com.scaffold.service;

import com.scaffold.common.BusinessException;
import com.scaffold.common.PageResult;
import com.scaffold.entity.HomeBanner;
import com.scaffold.mapper.HomeBannerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeBannerService {

    private final HomeBannerMapper homeBannerMapper;

    public PageResult<HomeBanner> page(String keyword, int page, int size) {
        int offset = (page - 1) * size;
        long total = homeBannerMapper.countPage(keyword);
        List<HomeBanner> list = homeBannerMapper.findPage(keyword, offset, size);
        return PageResult.of(total, list);
    }

    public List<HomeBanner> listEnabled() {
        return homeBannerMapper.findAll(1);
    }

    public HomeBanner getById(Long id) {
        HomeBanner banner = homeBannerMapper.findById(id);
        if (banner == null) {
            throw new BusinessException(404, "轮播图不存在");
        }
        return banner;
    }

    public HomeBanner create(HomeBanner banner) {
        normalize(banner);
        homeBannerMapper.insert(banner);
        return homeBannerMapper.findById(banner.getId());
    }

    public HomeBanner update(Long id, HomeBanner banner) {
        getById(id);
        banner.setId(id);
        normalize(banner);
        homeBannerMapper.update(banner);
        return homeBannerMapper.findById(id);
    }

    public void delete(Long id) {
        getById(id);
        homeBannerMapper.deleteById(id);
    }

    private void normalize(HomeBanner banner) {
        if (banner.getSortOrder() == null) {
            banner.setSortOrder(0);
        }
        if (banner.getStatus() == null) {
            banner.setStatus(1);
        }
        if (!StringUtils.hasText(banner.getCtaText())) {
            banner.setCtaText("了解更多");
        }
        if (!StringUtils.hasText(banner.getLinkUrl())) {
            banner.setLinkUrl("/courses");
        }
    }
}
