package com.scaffold.service;

import com.scaffold.common.BusinessException;
import com.scaffold.common.PageResult;
import com.scaffold.entity.Carousel;
import com.scaffold.mapper.CarouselMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 轮播图 业务层（写业务规则的地方）
 *
 * 【在链路中的位置】
 * 前端页面 → CarouselController【大门】→ 【本类】→ CarouselMapper 接口 → CarouselMapper.xml【SQL】→ MySQL
 *
 * 本类不碰 HTTP，也不写 SQL，只做「业务判断」：
 *  - 参数合不合法
 *  - 数据存不存在（删除前先查一下，查不到就抛异常）
 *  - 分页怎么算（offset 怎么来）
 *  - 返回给 Controller 什么结构
 *
 * 【⚠️ 和教程最大的结构差异：本项目不用「接口 + 实现类」】
 * 教程是两文件：
 *     ICarouselService           （接口：声明 save / update / remove / selectAll / selectById / selectPage）
 *     CarouselServiceImpl        （实现类：@Service + implements ICarouselService）
 * 本项目全项目 13 个 Service 都是「一个 @Service 类写完」，所以这两个文件合并成本类：
 *     教程 save(carousel)              ↔  本类 create(carousel)
 *     教程 update(carousel)            ↔  本类 update(id, carousel)   ← 多收一个路径 id
 *     教程 remove(id)                  ↔  本类 delete(id)
 *     教程 selectAll()                 ↔  本类 listAll()
 *     教程 selectById(id)              ↔  本类 getById(id)
 *     教程 selectPage(pageNum,pageSize,name)  ↔  本类 page(keyword, page, size)
 *
 * 【⚠️ 第二个差异：教程注入了 GoodsMapper，本类不需要】
 * 教程要在 Java 里循环调 goodsMapper.selectById() 去补商品名（N+1 查询），所以必须注入 GoodsMapper。
 * 本项目的商品名是 CarouselMapper.xml 里 LEFT JOIN 出来的，SQL 一次搞定，所以本类只注入 CarouselMapper。
 *
 * 【@RequiredArgsConstructor】Lombok 自动生成构造方法，把 final 字段注入进来，
 * 所以不用手写 @Autowired。
 */
@Service // 交给 Spring 管理，Controller 里能直接注入
@RequiredArgsConstructor
public class CarouselService {

    /** 注入 Mapper：本类访问数据库全靠它（它背后就是 CarouselMapper.xml 里的 SQL） */
    private final CarouselMapper carouselMapper;

    /**
     * 分页查询
     *
     * 【谁在用】CarouselController.page() ← 前端 getCarouselPage()（轮播图2 页面列表）
     * 【返回给前端】PageResult → 序列化成 JSON 的 { total: 3, list: [...] }
     *           前端 Carousels.vue 里：tableData = data.list（填表格）、total = data.total（填分页）
     *
     * 【和教程的差异】教程返回的是 MyBatis-Plus 的 IPage/Page 对象，JSON 长这样：
     *     { "records": [...], "total": 3, "size": 10, "current": 1, "pages": 1 }
     *   所以教程前端要写 res.data.records 才能拿到列表。
     *   本项目返回 { total, list }，前端直接 data.list —— 更简单，但两边写法不能混用。
     *
     * @param keyword 轮播图名称关键字，空则不筛选（对应前端搜索框）
     * @param page    第几页，从 1 开始
     * @param size    每页几条
     */
    public PageResult<Carousel> page(String keyword, int page, int size) {
        // SQL 的 LIMIT 需要的是「跳过多少条」，所以：
        // 第 1 页跳过 0 条 → (1-1)*10 = 0；第 2 页跳过 10 条 → (2-1)*10 = 10
        int offset = (page - 1) * size;

        // 查两次：先要总数（前端分页控件显示「共 N 条」、算总页数），再要当前页的数据
        // ⚠️ 两次的筛选条件必须一致，所以 XML 里共用了 baseWhere 片段
        long total = carouselMapper.countPage(keyword);
        List<Carousel> list = carouselMapper.findPage(keyword, offset, size);

        return PageResult.of(total, list);  // 打包成 { total, list }
    }

    /**
     * 查询全部轮播图（不分页）
     * 【谁在用】CarouselController.list() ← 前端 getCarouselList()
     */
    public List<Carousel> listAll() {
        return carouselMapper.findAll();
    }

    /**
     * 根据 id 查一条，查不到就抛业务异常
     * 【谁来接这个异常】GlobalExceptionHandler 会把它转成
     * {"code":404,"message":"轮播图不存在"} 返回给前端，前端自动弹红字提示
     */
    public Carousel getById(Integer id) {
        Carousel carousel = carouselMapper.findById(id);
        if (carousel == null) {
            throw new BusinessException(404, "轮播图不存在");
        }
        return carousel;
    }

    /**
     * 新增轮播图
     * 【谁在用】CarouselController.create() ← 前端弹窗点「确定」（新增模式）
     *
     * 三步：
     *  1. 整理一下名称（去空格）
     *  2. 入库（XML 里配了 useGeneratedKeys，插完 carousel.getId() 会自动变成数据库生成的 id）
     *  3. 回查一遍再返回 —— 这样前端拿到的是完整记录（含 id、商品名）
     *
     * 【和教程的差异】教程 save() 只做第 2 步，直接 carouselMapper.insert(carousel) 就返回 void，
     * 前端拿不到完整对象。本项目回查一遍，前端拿到的就是「数据库里的真实样子」。
     */
    public Carousel create(Carousel carousel) {
        normalize(carousel);
        carouselMapper.insert(carousel);
        return carouselMapper.findById(carousel.getId());
    }

    /**
     * 修改轮播图
     * 【谁在用】CarouselController.update() ← 前端弹窗点「确定」（编辑模式）
     *
     * 注意第 2 步：以 URL 上的 id 为准（前端传的 body 里就算有 id 也不信任），
     * 避免「改 A 的地址却把 B 改了」这种事。
     *
     * 【和教程的差异】教程 update() 直接 updateById(carousel)，id 从前端 JSON 里读，
     * 而且会把没传的字段更新成 NULL。本类先从路径拿 id，且 XML 里只更新传了值的字段。
     */
    public Carousel update(Integer id, Carousel carousel) {
        getById(id);                  // 先确认存在，不存在直接抛 404
        carousel.setId(id);           // 用路径参数里的 id 覆盖
        normalize(carousel);
        carouselMapper.update(carousel);
        return carouselMapper.findById(id);   // 回查返回最新数据（带商品名、商品封面）
    }

    /**
     * 删除轮播图
     * 【谁在用】CarouselController.delete() ← 前端表格点「删除」
     * 先 getById 确认存在，让「删一条不存在的记录」报 404 而不是假装成功
     */
    public void delete(Integer id) {
        getById(id);
        carouselMapper.deleteById(id);
    }

    /**
     * 业务规则：整理名称
     *
     * 【和商品模块的差异，值得想一想】
     * Goods2Service.normalize() 补了一大堆默认值（状态=上架、销量=0、库存=0、价格=0、
     * 所属用户=1、上架日期=当前时间）—— 因为那些列在业务上「不该为空」。
     *
     * 而 carousel 表的 4 个列全都是 DEFAULT NULL，一个必填的都没有：
     *     id      自增，不用管
     *     name    可以为空（数据库允许，且前端表单会拦）
     *     cover   可以为空（有的轮播图可能先建着，图后补）
     *     goods_id 可以为空（可以不关联商品，比如纯宣传图）
     * 所以这里 **不需要补任何默认值** —— 不填就存 NULL，这是符合业务的。
     *
     * 唯一做的是「去掉名称首尾空格」：用户输入 "  首页推荐  " 时，
     * 存进库里的应该是 "首页推荐"，不然搜索时匹配不上。
     * 如果去掉空格后变成空字符串，就统一存 NULL（空字符串和 NULL 混着存，以后查询很麻烦）。
     */
    private void normalize(Carousel carousel) {
        if (carousel.getName() != null) {
            String name = carousel.getName().trim();
            carousel.setName(StringUtils.hasText(name) ? name : null);
        }
    }
}
