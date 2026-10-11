package com.scaffold.mapper;
import com.scaffold.entity.Collect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CollectMapper {
    /** 查「这个用户有没有收藏这个商品」→ 没有返回 null */
    Collect findByUserAndGoods(@Param("userId") Integer userId,
                               @Param("goodsId") Integer goodsId);

    /** 我的收藏（带商品信息，靠 XML 的 association） */
    List<Collect> findByUser(@Param("userId") Integer userId);

    /** 后台分页数据（带用户名、商品名） */
    List<Collect> findPage(@Param("offset") int offset,
                           @Param("limit") int limit);

    /** 后台总条数 */
    long countPage();

    /** 新增收藏 */
    int insert(Collect collect);

    /** 按 id 删除（前后台共用） */
    int deleteById(@Param("id") Integer id);

    /** 取消收藏（按 用户+商品 删） */
    int deleteByUserAndGoods(@Param("userId") Integer userId,
                             @Param("goodsId") Integer goodsId);
}
