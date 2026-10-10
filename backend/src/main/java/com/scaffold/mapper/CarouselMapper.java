package com.scaffold.mapper;

import com.scaffold.entity.Carousel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 轮播图 Mapper 接口（数据访问层）
 *
 * 【在链路中的位置】
 * 前端页面 → CarouselController → CarouselService → 【本接口】→ CarouselMapper.xml（真正的 SQL）→ MySQL
 *
 * 【完整的四层对照】同一个功能，四层各写一句，串起来就是一条链：
 *   前端 api/carousel.js       CarouselService        【本接口】         CarouselMapper.xml
 *   ------------------------------------------------------------------------------------
 *   getCarouselPage()  →  page()      →  countPage()  →  <select id="countPage">
 *                                        findPage()   →  <select id="findPage">
 *   getCarouselList()  →  listAll()   →  findAll()    →  <select id="findAll">
 *   getCarouselById()  →  getById()   →  findById()   →  <select id="findById">
 *   createCarousel()   →  create()    →  insert()     →  <insert id="insert">
 *   updateCarousel()   →  update()    →  update()     →  <update id="update">
 *   deleteCarousel()   →  delete()    →  deleteById() →  <delete id="deleteById">
 *
 * 【和教程的对照】教程写的是：
 *     public interface CarouselMapper extends BaseMapper<Carousel> {}
 * 靠继承 BaseMapper 白拿一堆方法（insert / deleteById / selectList / selectById / selectPage / updateById），
 * 一行 SQL 都不用写 —— 那是 MyBatis-Plus 的方式。
 * 本项目用原生 MyBatis，所以要把「用到的那几个方法」自己声明出来，再到 XML 里写 SQL：
 *     教程 deleteById(id)    ↔  本接口 deleteById(@Param("id") Integer id)
 *     教程 selectList(null)  ↔  本接口 findAll()
 *     教程 selectById(id)    ↔  本接口 findById(@Param("id") Integer id)
 *     教程 selectPage(...)   ↔  本接口 findPage(...) + countPage(...)
 *     教程 insert(...)       ↔  本接口 insert(...)
 *     教程 updateById(...)   ↔  本接口 update(...)
 *
 * 【为什么只有声明、没有实现】
 * 方法对应的 SQL 全写在 resources/mapper/CarouselMapper.xml 里，
 * 两边靠「方法名 == SQL 的 id」配对（如 findPage() ↔ <select id="findPage">）。
 *
 * 【@Param 是什么】给参数起名字，XML 里就能用 #{id}、#{keyword} 取值。
 */
@Mapper // 告诉 MyBatis：这是 Mapper 接口，启动时为它自动生成实现类
public interface CarouselMapper {

    /** 根据 id 查一条轮播图（带商品名、商品封面，靠 XML 里的 JOIN） → 对应 findById */
    Carousel findById(@Param("id") Integer id);

    /** 查全部轮播图 → 对应 findAll */
    List<Carousel> findAll();

    /**
     * 分页查询
     * @param keyword 轮播图名称关键字，空则不筛选
     * @param offset  跳过的条数（Service 算好：(page-1)*size）
     * @param limit   每页几条
     */
    List<Carousel> findPage(@Param("keyword") String keyword,
                            @Param("offset") int offset,
                            @Param("limit") int limit);

    /** 统计满足条件的总条数（筛选条件必须和 findPage 保持一致） */
    long countPage(@Param("keyword") String keyword);

    /** 新增轮播图，插入后自增 id 会回填到参数对象 → 对应 insert */
    int insert(Carousel carousel);

    /** 修改轮播图 → 对应 update */
    int update(Carousel carousel);

    /** 根据 id 删除轮播图 → 对应 deleteById */
    int deleteById(@Param("id") Integer id);
}
