package com.scaffold.service;

import com.scaffold.common.BusinessException;
import com.scaffold.common.PageResult;
import com.scaffold.dto.NameCount;
import com.scaffold.entity.Goods;
import com.scaffold.mapper.GoodsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 商品 业务层（写业务规则的地方）
 *
 * 【在链路中的位置】
 * 前端页面 → GoodsController【大门】→ 【本类】→ GoodsMapper 接口 → GoodsMapper.xml【SQL】→ MySQL
 *
 * 本类不碰 HTTP，也不写 SQL，只做「业务判断」：
 *  - 参数合不合法（比如状态没填就给「上架」）
 *  - 数据存不存在（删除前先查一下，查不到就抛异常）
 *  - 分页怎么算（offset 怎么来）
 *  - 返回给 Controller 什么结构
 *
 * 【和 Type2Service 的区别】
 * ① 分页方法多一个 typeId 参数（支持按分类筛选）
 * ② create() 里要补更多默认值（状态、销量、上架日期）
 *
 * 【注意】本项目不用「接口 + impl 实现类」那种写法，直接一个 @Service 类写完
 * （全项目 13 个 Service 都是这个风格）。教程里的 IGoodsService + impl/GoodsServiceImpl，
 * 对应到本项目就是本类，逻辑写在同一处。
 *
 * 【@RequiredArgsConstructor】Lombok 自动生成构造方法，把 final 字段注入进来，
 * 所以不用手写 @Autowired。
 */
@Service // 交给 Spring 管理，Controller 里能直接注入
@RequiredArgsConstructor
public class GoodsService {

    /** 注入 Mapper：本类访问数据库全靠它（它背后就是 GoodsMapper.xml 里的 SQL） */
    private final GoodsMapper goodsMapper;

    /** 日期格式：和数据库 varchar 里存的样子保持一致（2025-11-28 10:06:55） */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /** 首页「新品上架」「热销商品」各显示几条 */
    private static final int HOME_LIMIT = 4;

    /**
     * 分页查询（支持「按分类筛选」）
     *
     * 【谁在用】GoodsController.page() ← 前端 getGoodsPage()（商品管理页面列表）
     * 【返回给前端】PageResult → 序列化成 JSON 的 { total: 8, list: [...] }
     *           前端 Goods.vue 里：tableData = data.list（填表格）、total = data.total（填分页）
     *
     * @param keyword 商品名关键字，空则不筛选（对应前端搜索框）
     * @param typeId  分类 id，null 则不筛选（对应前端分类下拉框）
     * @param page    第几页，从 1 开始
     * @param size    每页几条
     */
    public PageResult<Goods> page(String keyword, Integer typeId, int page, int size) {
        // SQL 的 LIMIT 需要的是「跳过多少条」，所以：
        // 第 1 页跳过 0 条 → (1-1)*10 = 0；第 2 页跳过 10 条 → (2-1)*10 = 10
        int offset = (page - 1) * size;

        // 查两次：先要总数（前端分页控件显示「共 N 条」、算总页数），再要当前页的数据
        // ⚠️ 两次的筛选条件必须一致，所以 XML 里共用了 baseWhere 片段
        long total = goodsMapper.countPage(keyword, typeId);
        List<Goods> list = goodsMapper.findPage(keyword, typeId, offset, size);

        return PageResult.of(total, list);  // 打包成 { total, list }
    }

    /**
     * 前台商品分页（只查「上架」商品）
     *
     * 【和上面 page() 的区别】代码几乎一样，只差「查哪条 SQL」：
     *   page()        → countPage / findPage             → 上架 + 下架都能看到（后台管理用）
     *   pageOnSale()  → countPageOnSale / findPageOnSale → 只有上架（前台商城的顾客看）
     *
     * 【谁在用】PublicController.goods() ← 前端 views/user/GoodsList.vue
     */
    public PageResult<Goods> pageOnSale(String keyword, Integer typeId, int page, int size) {
        int offset = (page - 1) * size;

        // 和 page() 一样「先要总数、再要数据」，两次的筛选条件共用 XML 里的 onSaleWhere
        long total = goodsMapper.countPageOnSale(keyword, typeId);
        List<Goods> list = goodsMapper.findPageOnSale(keyword, typeId, offset, size);

        return PageResult.of(total, list);
    }

    /**
     * 查询全部商品（不分页）
     * 【谁在用】GoodsController.list() ← 前端 getGoodsList()，给下拉框用
     */
    public List<Goods> listAll() {
        return goodsMapper.findAll();
    }
    /**
     * 新品上架：按上架日期倒序取前 N 条
     * 【谁在用】GoodsController.times() 和 PublicController.home()
     */
    public List<Goods> times() {
        return goodsMapper.findLatest(HOME_LIMIT);
    }

    /**
     * 热销商品：按销量倒序取前 N 条
     * 【谁在用】GoodsController.sales() 和 PublicController.home()
     */
    public List<Goods> sales() {
        return goodsMapper.findHot(HOME_LIMIT);
    }

    /**
     * 商品销量统计（第十二章：后台首页 ECharts 饼图）
     *
     * 【谁在用】GoodsController.echarts() ← 前端 views/Home.vue 的饼图
     *
     * 【返回给前端】List<NameCount> → JSON 长这样：
     *     [ {"name":"美味壹客全麦坚果欧包","value":128}, {"name":"小米耳机","value":96} ]
     *   ECharts 饼图的 series.data 就是吃这个格式，前端拿到直接塞进去，不用再加工。
     *
     * 【和教程的差异】教程是「查出全部商品 → 遍历 → 一个个 set 进 JSONObject」，
     * 本项目把"筛选 + 排序 + 只要两列"全放进 SQL（statSalesTop），
     * Java 这边一行都不用循环 —— 这叫「让数据库干数据库擅长的事」。
     *
     * 【为什么只取 10 条】饼图扇区超过 10 个，标签就会互相压在一起看不清。
     * 想看全部的话，把 10 改大或改成参数即可。
     */
    public List<NameCount> echarts() {
        return goodsMapper.statSalesTop(10);
    }

    /**
     * 根据 id 查一条，查不到就抛业务异常
     * 【谁来接这个异常】GlobalExceptionHandler 会把它转成
     * {"code":404,"message":"商品不存在"} 返回给前端，前端自动弹红字提示
     */
    public Goods getById(Integer id) {
        Goods goods = goodsMapper.findById(id);
        if (goods == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return goods;
    }
    public Goods getOnSale(Integer id) {
        Goods goods = getById(id);
        if (!"上架".equals(goods.getState())) {
            throw new BusinessException(404, "商品不存在");
        }
        return goods;
    }

    /**
     * 新增商品
     * 【谁在用】GoodsController.create() ← 前端弹窗点「确定」（新增模式）
     *
     * 三步：
     *  1. 补默认值（状态、销量、上架日期、所属用户）
     *  2. 入库（XML 里配了 useGeneratedKeys，插完 goods.getId() 会自动变成数据库生成的 id）
     *  3. 回查一遍再返回 —— 这样前端拿到的是完整记录（含 id、分类名、用户名）
     */
    public Goods create(Goods goods) {
        normalize(goods);
        goodsMapper.insert(goods);
        return goodsMapper.findById(goods.getId());
    }

    /**
     * 修改商品
     * 【谁在用】GoodsController.update() ← 前端弹窗点「确定」（编辑模式）
     *
     * ⚠️ 注意这里和 Type2Service.update() 有一处不同（很重要）：
     * 前端编辑弹窗里「没有」上架日期、发布者、销量这三个字段，所以提交的 JSON 里也不含它们。
     * 如果直接走 normalize()，它们会被补成「当前时间 / 1 号用户 / 0」——
     * 结果就是「改一下商品名，上架日期和销量却被重置了」这种脏数据。
     *
     * 所以第 2 步先从数据库取出原记录，把这三个字段「继承」过来，再补默认值。
     */
    public Goods update(Integer id, Goods goods) {
        Goods original = getById(id);   // 先确认存在，不存在直接抛 404

        goods.setId(id);                // 用路径参数里的 id 覆盖（不信任前端传的 id）

        // 前端表单没覆盖到的字段 → 保留原来的值
        if (!StringUtils.hasText(goods.getDate())) {
            goods.setDate(original.getDate());
        }
        if (goods.getUserId() == null) {
            goods.setUserId(original.getUserId());
        }
        // ⚠️ 销量同样不在编辑弹窗里（Goods.vue 的 payload 不含 sales）。
        // 必须在这里先把它继承过来：否则紧随其后的 normalize() 会把 null 补成 0，
        // 变成「编辑一次商品，销量就被清零」。
        if (goods.getSales() == null) {
            goods.setSales(original.getSales());
        }

        normalize(goods);
        goodsMapper.update(goods);
        return goodsMapper.findById(id);   // 回查返回最新数据（带分类名、用户名）
    }

    /**
     * 删除商品
     * 【谁在用】GoodsController.delete() ← 前端表格点「删除」
     * 先 getById 确认存在，让「删一条不存在的记录」报 404 而不是假装成功
     */
    public void delete(Integer id) {
        getById(id);
        goodsMapper.deleteById(id);
    }

    /**
     * 业务规则：补默认值，避免必填字段为空导致显示异常
     *
     * 【为什么需要】前端新增商品时，用户可能只填了名称和价格，
     * 其他字段（状态、销量、日期）不应该让用户操心 —— 由后端补上合理的默认值。
     */
    private void normalize(Goods goods) {
        if (!StringUtils.hasText(goods.getState())) {
            goods.setState("上架");                     // 默认上架
        }
        if (goods.getSales() == null) {
            goods.setSales(0);                          // 新商品销量为 0
        }
        if (goods.getStore() == null) {
            goods.setStore(0);                          // 库存默认 0
        }
        if (goods.getPrice() == null) {
            goods.setPrice(0.0);                        // 价格默认 0
        }
        if (goods.getUserId() == null) {
            goods.setUserId(1);                         // 默认属于 1 号用户（演示用户）
        }
        if (!StringUtils.hasText(goods.getDate())) {
            goods.setDate(LocalDateTime.now().format(DATE_FORMAT));   // 没填就记当前时间
        }
    }
}
