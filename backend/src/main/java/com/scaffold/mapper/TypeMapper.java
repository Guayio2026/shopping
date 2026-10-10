package com.scaffold.mapper;

import com.scaffold.entity.Type;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品分类 Mapper 接口（数据访问层）
 *
 * 【在链路中的位置】
 * 前端页面 → TypeController → TypeService → 【本接口】→ TypeMapper.xml（真正的 SQL）→ MySQL
 *
 * 【完整的四层对照】同一个功能，四层各写一句，串起来就是一条链：
 *   前端 api/type.js        TypeService          【本接口】        TypeMapper.xml
 *   ---------------------------------------------------------------------------
 *   getTypePage()   →  page()  →  countPage()  →  <select id="countPage">
 *                                 findPage()   →  <select id="findPage">
 *   getTypeList()   →  listAll()  →  findAll()  →  <select id="findAll">
 *   createType()    →  create()   →  insert()   →  <insert id="insert">
 *   updateType()    →  update()   →  update()   →  <update id="update">
 *   deleteType()    →  delete()   →  deleteById() → <delete id="deleteById">
 *
 * 【为什么只声明、不实现】本接口一个方法体都没写，SQL 全在 XML 里。
 * 两边靠"方法名 == SQL 的 id"对应上，写错名字启动时就会报错。
 * 这是原生 MyBatis 的写法；MyBatis-Plus 是继承 BaseMapper 免写 SQL，本项目不用那种方式。
 *
 * 【@Param 是什么】给参数起名字，XML 里就能用 #{id}、#{keyword} 取值。
 * 不加 @Param 的话，XML 里只能按 arg0/arg1 或 param1/param2 这种名字取，很难读。
 */
@Mapper // 告诉 MyBatis：这是 Mapper 接口，启动时为它自动生成实现类
public interface TypeMapper {

    /** 查一条 —— 前端：编辑前回查 / 增改后的回查。SQL 见 findById */
    Type findById(@Param("id") Long id);

    /** 查全部（按 sort_order 升序）—— 前端：getTypeList()，下拉框用。SQL 见 findAll */
    List<Type> findAll();

    /** 分页数据 —— 前端：getTypePage()（列表第一屏、翻页）。SQL 见 findPage */
    List<Type> findPage(@Param("keyword") String keyword,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

    /** 分页总数 —— 前端：分页控件显示"共 N 条"。SQL 见 countPage */
    long countPage(@Param("keyword") String keyword);

    /** 新增 —— 前端：弹窗点「确定」（新增）。SQL 见 insert，插入后自增 id 回填到参数对象 */
    int insert(Type type);

    /** 修改 —— 前端：弹窗点「确定」（编辑）。SQL 见 update */
    int update(Type type);

    /** 删除 —— 前端：表格点「删除」。SQL 见 deleteById */
    int deleteById(@Param("id") Long id);
}
