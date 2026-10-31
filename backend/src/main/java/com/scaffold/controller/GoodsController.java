package com.scaffold.controller;

import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.dto.NameCount;
import com.scaffold.entity.Goods;
import com.scaffold.service.GoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品 接口层（前端连到后端的「大门」）
 *
 * ============================ 前后端连接对照表 ============================
 *  前端「商品管理」页面上的操作     →  前端调用的函数         →  打到本类的哪个方法
 *  ---------------------------------------------------------------------------
 *  打开页面 / 查询 / 翻页 / 选分类  →  getGoodsPage(params)  →  page()    GET  /page
 *  （下拉框加载商品列表）           →  getGoodsList()        →  list()    GET  /list
 *  （按 id 取单条）                 →  getGoodsById(id)      →  getById() GET  /{id}
 *  弹窗点「确定」（新增模式）       →  createGoods(payload)  →  create()  POST /
 *  弹窗点「确定」（编辑模式）       →  updateGoods(id,data)  →  update()  PUT  /{id}
 *  表格里点「删除」                 →  deleteGoods(row.id)   →  delete()  DELETE /{id}
 * ==========================================================================
 *
 * 【完整链路】
 * 浏览器 localhost:5173 → Goods.vue → api/goods.js → request.js（加 /api 前缀 + token）
 *                       → Vite 代理转发 → 【本类】→ GoodsService → GoodsMapper.xml → MySQL
 *
 * 【URL 前面有个 /api】
 * application.yml 里配了 context-path: /api。所以本类 @RequestMapping("/goods")
 * 加上方法上的 "/page"，最终地址是：http://localhost:8080/api/goods/page
 *
 * 【参数怎么取，看这三个注解】
 *   @RequestParam  → 从问号后面取，如 ?keyword=坚果&page=1
 *   @PathVariable  → 从地址路径里取，如 /goods/5 的 5
 *   @RequestBody   → 从请求体 JSON 里取，如 {"name":"手机","price":5999}
 *
 * 【返回值为什么是 Result】
 * 统一格式 {"code":200,"message":"success","data":...}，前端 request.js 会自动把 data 拆出来，
 * 所以 Goods.vue 里拿到的直接就是数据本身（比如 { total, list }）。
 */
@RestController              // = @Controller + @ResponseBody，方法返回值自动转成 JSON
@RequestMapping("/goods")    // 类级路径前缀：本类所有接口都以 /goods 开头
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService; // 注入业务层，真正的活交给它干

    /**
     * 【查-分页】GET /api/goods/page?keyword=坚果&typeId=1&page=1&size=10
     *
     * 谁在调：Goods.vue 的 loadData() → api/goods.js 的 getGoodsPage()
     * 什么时候：页面一打开、点「查询」、按回车、翻页、切换分类下拉框 —— 都会走这里
     * 返回：{ total, list }，total 给分页控件，list 给表格
     *
     * 注意 typeId 是 Integer（不是 int）—— 因为「不筛选分类」时前端不传这个参数，
     * 不传就是 null，用 int 会报错（int 不能是 null）。
     */
    @GetMapping("/page")
    public Result<PageResult<Goods>> page(@RequestParam(defaultValue = "") String keyword,
                                          @RequestParam(required = false) Integer typeId,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return Result.ok(goodsService.page(keyword, typeId, page, size));
    }

    /**
     * 【查-全部】GET /api/goods/list
     *
     * 谁在调：api/goods.js 的 getGoodsList()（供下拉框等场景）
     * 返回：全部商品数组（不分页）
     */
    @GetMapping("/list")
    public Result<List<Goods>> list() {
        return Result.ok(goodsService.listAll());
    }
    /**
     * 【查-新品上架】GET /api/goods/times
     *
     * 谁在调：教程里的首页 loadTimeGoods()；本项目首页走 /public/home，这个接口先留着
     * 返回：按上架日期倒序的 4 条商品
     */
    @GetMapping("/times")
    public Result<List<Goods>> times() {
        return Result.ok(goodsService.times());
    }

    /**
     * 【查-热销商品】GET /api/goods/sales
     *
     * 返回：按销量倒序的 4 条商品
     */
    @GetMapping("/sales")
    public Result<List<Goods>> sales() {
        return Result.ok(goodsService.sales());
    }

    /**
     * 【统计分析】GET /api/goods/echarts
     *
     * 谁在调：views/Home.vue（后台首页）的饼图 → api/goods.js 的 getGoodsSalesStat()
     * 返回：[{name:'商品名', value:销量}, ...]，按销量倒序前 10 条
     *
     * ⚠️ 这个路径必须写在 getById 的 /{id} 之前吗？不必须 ——
     *    Spring 匹配时「字面量路径」优先于「路径变量」，/goods/echarts 不会被当成 id="echarts"。
     *    但仍然建议放在 /{id} 上面，因为人读代码时更顺。
     */
    @GetMapping("/echarts")
    public Result<List<NameCount>> echarts() {
        return Result.ok(goodsService.echarts());
    }

    /**
     * 【查-单条】GET /api/goods/5
     *
     * {id} 是路径参数，靠 @PathVariable 从地址里取出来（不是从问号后面取）
     * 谁在调：api/goods.js 的 getGoodsById()（编辑前回查详情）
     */
    @GetMapping("/{id}")
    public Result<Goods> getById(@PathVariable Integer id) {
        return Result.ok(goodsService.getById(id));
    }

    /**
     * 【增】POST /api/goods
     *
     * 谁在调：Goods.vue 的 handleSubmit() → api/goods.js 的 createGoods(payload)
     * 什么时候：点「新增商品」打开弹窗 → 填完 → 点「确定」
     * 请求体 JSON：{"name":"手机","price":5999,"store":100,"typeId":10}
     * 返回：新增后的完整记录（带数据库生成的 id、分类名、用户名）
     */
    @PostMapping
    public Result<Goods> create(@RequestBody Goods goods) {
        return Result.ok(goodsService.create(goods));
    }

    /**
     * 【改】PUT /api/goods/5
     *
     * 谁在调：Goods.vue 的 handleSubmit()（编辑模式）→ api/goods.js 的 updateGoods(id, payload)
     * 什么时候：表格里点「编辑」→ 改完 → 点「确定」
     * id 在地址里（@PathVariable），要改的内容在请求体 JSON 里（@RequestBody）
     */
    @PutMapping("/{id}")
    public Result<Goods> update(@PathVariable Integer id, @RequestBody Goods goods) {
        return Result.ok(goodsService.update(id, goods));
    }

    /**
     * 【删】DELETE /api/goods/5
     *
     * 谁在调：Goods.vue 的 handleDelete(row) → api/goods.js 的 deleteGoods(row.id)
     * 什么时候：表格里点「删除」→ 确认框点「确定」
     * 返回：Result.ok()（data 为 null，前端只看 code 是否 200）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        goodsService.delete(id);
        return Result.ok();
    }
}
