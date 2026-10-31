package com.scaffold.mapper;

import com.scaffold.dto.NameCount;
import com.scaffold.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品 Mapper 接口（数据访问层）
 *
 * 【在链路中的位置】
 * 前端页面 → GoodsController → GoodsService → 【本接口】→ GoodsMapper.xml（真正的 SQL）→ MySQL
 *
 * 【完整的四层对照】同一个功能，四层各写一句，串起来就是一条链：
 *   前端 api/goods.js        GoodsService          【本接口】         GoodsMapper.xml
 *   -------------------------------------------------------------------------------
 *   getGoodsPage()   →  page()      →  countPage()  →  <select id="countPage">
 *                                      findPage()   →  <select id="findPage">
 *   getGoodsList()   →  listAll()   →  findAll()    →  <select id="findAll">
 *   getGoodsById()   →  getById()   →  findById()   →  <select id="findById">
 *   createGoods()    →  create()    →  insert()     →  <insert id="insert">
 *   updateGoods()    →  update()    →  update()     →  <update id="update">
 *   deleteGoods()    →  delete()    →  deleteById() →  <delete id="deleteById">
 *
 * 【和 Type2Mapper 的唯一区别】
 * 查询方法多了 typeId 参数 —— 因为商品列表要支持「按分类筛选」。
 *
 * 【为什么只有声明、没有实现】
 * 方法对应的 SQL 全写在 resources/mapper/GoodsMapper.xml 里，
 * 两边靠「方法名 == SQL 的 id」配对（如 findPage() ↔ <select id="findPage">）。
 * 这是原生 MyBatis 的写法；MyBatis-Plus 是继承 BaseMapper 免写 SQL，本项目不用那种方式。
 *
 * 【@Param 是什么】给参数起名字，XML 里就能用 #{id}、#{keyword} 取值。
 */
@Mapper // 告诉 MyBatis：这是 Mapper 接口，启动时为它自动生成实现类
public interface GoodsMapper {

    /** 根据 id 查一条商品（带分类名、用户名，靠 XML 里的 JOIN） → 对应 findById */
    Goods findById(@Param("id") Integer id);

    /** 查全部商品 → 对应 findAll */
    List<Goods> findAll();

    /** 新品上架：按上架日期倒序取前 N 条（首页「新品上架」用） → 对应 findLatest */
    List<Goods> findLatest(@Param("limit") int limit);

    /** 热销商品：按销量倒序取前 N 条（首页「热销商品」用） → 对应 findHot */
    List<Goods> findHot(@Param("limit") int limit);

    /**
     * 分页查询
     * @param keyword 商品名称关键字，空则不筛选
     * @param typeId  分类 id，null 则不筛选（下拉框选了分类才传）
     * @param offset  跳过的条数（Service 算好：(page-1)*size）
     * @param limit   每页几条
     */
    List<Goods> findPage(@Param("keyword") String keyword,
                         @Param("typeId") Integer typeId,
                         @Param("offset") int offset,
                         @Param("limit") int limit);

    /** 统计满足条件的总条数（筛选条件必须和 findPage 保持一致） */
    long countPage(@Param("keyword") String keyword,
                   @Param("typeId") Integer typeId);
    /**
     * 【前台分页数据】只查「上架」的商品
     * @param keyword 商品名关键字，空字符串表示不筛
     * @param typeId  分类 id，null 表示不筛
     * @param offset  跳过多少条（Service 算好：(page-1)*size）
     * @param limit   每页几条
     */
    List<Goods> findPageOnSale(@Param("keyword") String keyword,
                               @Param("typeId") Integer typeId,
                               @Param("offset") int offset,
                               @Param("limit") int limit);
    /**
     * 【前台分页总数】筛选条件必须和 findPageOnSale 完全一致
     */
    long countPageOnSale(@Param("keyword") String keyword,
                         @Param("typeId") Integer typeId);
    /** 新增商品，插入后自增 id 会回填到参数对象 → 对应 insert */
    int insert(Goods goods);

    /** 修改商品 → 对应 update */
    int update(Goods goods);

    /** 根据 id 删除商品 → 对应 deleteById */
    int deleteById(@Param("id") Integer id);
    int deductStoreAndAddSales(@Param("id") Integer id, @Param("nums") Integer nums);

    /**
     * 商品销量统计（ECharts 饼图用）
     *
     * 【为什么返回 NameCount 而不是 Goods】
     * ECharts 饼图只认 [{name:'商品名', value:1200}] 这种两字段结构，
     * 查 20 列商品对象再丢掉 18 列是浪费。SQL 里直接 AS name / AS value，
     * NameCount 正好是这两个字段，Service 拿到就能直接返给前端。
     *
     * @param limit 取前几条（饼图扇区别超过 10 个，否则标签会叠在一起）
     */
    List<NameCount> statSalesTop(@Param("limit") int limit);
}
