package com.scaffold.service;

import com.scaffold.common.BusinessException;
import com.scaffold.common.PageResult;
import com.scaffold.entity.Collect;
import com.scaffold.mapper.CollectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 收藏 业务层
 *
 * 【注意】本项目不用「接口 + impl」写法，直接一个 @Service 类。
 * 【注意】本类不碰 HTTP —— 当前用户 id 由 Controller 从 request 里取好再传进来。
 */
@Service
@RequiredArgsConstructor
public class CollectService {

    private final CollectMapper collectMapper;

    /** 时间格式：和 goods.date 保持一致（2026-10-11 19:30:05） */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 收藏 / 取消收藏（一个按钮来回切）
     *
     * @return true = 操作后「已收藏」；false = 操作后「已取消」
     */
    public boolean toggle(Long userId, Integer goodsId) {
        if (goodsId == null) {
            throw new BusinessException(400, "商品 id 不能为空");
        }
        if (userId == null) {
            throw new BusinessException(401, "未登录或 token 无效");
        }
        Integer uid = userId.intValue();

        // 1. 先查这个用户收藏过这个商品没有
        Collect exist = collectMapper.findByUserAndGoods(uid, goodsId);

        // 2. 收藏过 → 删掉，返回「未收藏」
        if (exist != null) {
            collectMapper.deleteByUserAndGoods(uid, goodsId);
            return false;
        }

        // 3. 没收藏 → 插一条，返回「已收藏」
        Collect collect = new Collect();
        collect.setUserId(uid);
        collect.setGoodsId(goodsId);
        collect.setTime(LocalDateTime.now().format(DATE_FORMAT));
        collectMapper.insert(collect);
        return true;
    }

    /** 当前用户是否收藏了该商品 */
    public boolean isCollected(Long userId, Integer goodsId) {
        if (userId == null) {
            throw new BusinessException(401, "未登录或 token 无效");
        }
        return collectMapper.findByUserAndGoods(userId.intValue(), goodsId) != null;
    }

    /** 我的收藏（带商品信息） */
    public List<Collect> listMine(Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "未登录或 token 无效");
        }
        return collectMapper.findByUser(userId.intValue());
    }

    /**
     * 后台分页（别人的收藏也要能看，所以不收 userId）
     * 【谁在用】CollectController.page() ← 后台「收藏管理」页
     */
    public PageResult<Collect> page(int page, int size) {
        int offset = (page - 1) * size;
        long total = collectMapper.countPage();
        List<Collect> list = collectMapper.findPage(offset, size);
        return PageResult.of(total, list);
    }

    /** 删除（后台删记录 / 前台「取消收藏」共用） */
    public void delete(Integer id) {
        collectMapper.deleteById(id);
    }
}
