package com.scaffold.controller;

import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.entity.Carousel;
import com.scaffold.service.CarouselService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 轮播图 接口层（前端连到后端的「大门」）
 *
 * ============================ 前后端连接对照表 ============================
 *  前端「轮播图2」页面上的操作      →  前端调用的函数            →  打到本类的哪个方法
 *  ---------------------------------------------------------------------------
 *  打开页面 / 查询 / 翻页          →  getCarouselPage(params)  →  page()     GET    /page
 *  （下拉框加载轮播图列表）         →  getCarouselList()        →  list()     GET    /list
 *  （按 id 取单条）                →  getCarouselById(id)      →  getById()  GET    /{id}
 *  弹窗点「确定」（新增模式）       →  createCarousel(payload)  →  create()   POST   /
 *  弹窗点「确定」（编辑模式）       →  updateCarousel(id,data)  →  update()   PUT    /{id}
 *  表格里点「删除」                →  deleteCarousel(row.id)   →  delete()   DELETE /{id}
 * ==========================================================================
 *
 * 【完整链路】
 * 浏览器 localhost:5173 → Carousels.vue → api/carousel.js → request.js（加 /api 前缀 + token）
 *                       → Vite 代理转发 → 【本类】→ CarouselService → CarouselMapper.xml → MySQL
 *
 * 【URL 前面有个 /api】
 * application.yml 里配了 context-path: /api。所以本类 @RequestMapping("/carousel")
 * 加上方法上的 "/page"，最终地址是：http://localhost:8080/api/carousel/page
 *
 * ★★★ 【和教程的接口路径对照】★★★
 * 教程用的是「动词式」路径，本项目用 RESTful「名词式」路径，功能完全等价：
 *   教程                                  本项目                                     干什么
 *   ----------------------------------------------------------------------------------------
 *   POST   /carousel/add                  POST   /carousel                           新增
 *   PUT    /carousel/update               PUT    /carousel/{id}                      修改
 *   DELETE /carousel/delete?id=1          DELETE /carousel/1                        删除
 *   GET    /carousel/selectAll            GET    /carousel/list                      查全部
 *   GET    /carousel/selectById?id=1      GET    /carousel/1                         查单条
 *   GET    /carousel/selectPage           GET    /carousel/page                      分页
 *
 * 为什么要改？因为本项目其他模块（type / type2 / goods / goods2）全是 RESTful 风格：
 *   - 「要什么资源」用路径表示（/carousel/1 就是「1 号轮播图」）
 *   - 「做哪个动作」用 HTTP 方法表示（GET 查、POST 增、PUT 改、DELETE 删）
 * 一个项目里风格统一，后面维护才不烧脑。
 *
 * 另外两处差别：
 *   ① 教程的 delete 用 @RequestParam（/delete?id=1），本项目用 @PathVariable（/carousel/1）
 *   ② 教程返回 Result.success()，本项目的统一返回类是 Result.ok()
 *
 * 【参数怎么取，看这三个注解】
 *   @RequestParam  → 从问号后面取，如 ?keyword=首页&page=1
 *   @PathVariable  → 从地址路径里取，如 /carousel/5 的 5
 *   @RequestBody   → 从请求体 JSON 里取，如 {"name":"首页推荐","goodsId":4}
 *
 * 【返回值为什么是 Result】
 * 统一格式 {"code":200,"message":"success","data":...}，前端 request.js 会自动把 data 拆出来，
 * 所以 Carousels.vue 里拿到的直接就是数据本身（比如 { total, list }）。
 */
@RestController            // = @Controller + @ResponseBody，方法返回值自动转成 JSON
@RequestMapping("/carousel") // 类级路径前缀：本类所有接口都以 /carousel 开头
@RequiredArgsConstructor
public class CarouselController {

    private final CarouselService carouselService; // 注入业务层，真正的活交给它干

    /**
     * 【查-分页】GET /api/carousel/page?keyword=首页&page=1&size=10
     *
     * 谁在调：Carousels.vue 的 loadData() → api/carousel.js 的 getCarouselPage()
     * 什么时候：页面一打开、点「查询」、按回车、翻页 —— 都会走这里
     * 返回：{ total, list }，total 给分页控件，list 给表格
     *
     * 对应教程的：GET /carousel/selectPage?pageNum=1&pageSize=10&name=xxx
     * ⚠️ 注意参数名不一样：教程叫 pageNum / pageSize / name，本项目叫 page / size / keyword。
     *    这不是随便改的 —— 要跟本项目其他模块（/type2/page、/goods/page）保持一致。
     */
    @GetMapping("/page")
    public Result<PageResult<Carousel>> page(@RequestParam(defaultValue = "") String keyword,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(carouselService.page(keyword, page, size));
    }

    /**
     * 【查-全部】GET /api/carousel/list
     *
     * 谁在调：api/carousel.js 的 getCarouselList()
     * 返回：全部轮播图数组（不分页）
     * 对应教程的：GET /carousel/selectAll
     */
    @GetMapping("/list")
    public Result<List<Carousel>> list() {
        return Result.ok(carouselService.listAll());
    }

    /**
     * 【查-单条】GET /api/carousel/5
     *
     * {id} 是路径参数，靠 @PathVariable 从地址里取出来（不是从问号后面取）
     * 谁在调：api/carousel.js 的 getCarouselById()
     * 对应教程的：GET /carousel/selectById?id=5
     */
    @GetMapping("/{id}")
    public Result<Carousel> getById(@PathVariable Integer id) {
        return Result.ok(carouselService.getById(id));
    }

    /**
     * 【增】POST /api/carousel
     *
     * 谁在调：Carousels.vue 的 handleSubmit() → api/carousel.js 的 createCarousel(payload)
     * 什么时候：点「新增轮播图」打开弹窗 → 填完 → 点「确定」
     * 请求体 JSON：{"name":"首页首屏推荐","cover":"/uploads/xxx.png","goodsId":4}
     * 返回：新增后的完整记录（带数据库生成的 id、商品名）
     * 对应教程的：POST /carousel/add
     */
    @PostMapping
    public Result<Carousel> create(@RequestBody Carousel carousel) {
        return Result.ok(carouselService.create(carousel));
    }

    /**
     * 【改】PUT /api/carousel/5
     *
     * 谁在调：Carousels.vue 的 handleSubmit()（编辑模式）→ api/carousel.js 的 updateCarousel(id, payload)
     * 什么时候：表格里点「编辑」→ 改完 → 点「确定」
     * id 在地址里（@PathVariable），要改的内容在请求体 JSON 里（@RequestBody）
     * 对应教程的：PUT /carousel/update （id 藏在 body 里）
     */
    @PutMapping("/{id}")
    public Result<Carousel> update(@PathVariable Integer id, @RequestBody Carousel carousel) {
        return Result.ok(carouselService.update(id, carousel));
    }

    /**
     * 【删】DELETE /api/carousel/5
     *
     * 谁在调：Carousels.vue 的 handleDelete(row) → api/carousel.js 的 deleteCarousel(row.id)
     * 什么时候：表格里点「删除」→ 确认框点「确定」
     * 返回：Result.ok()（data 为 null，前端只看 code 是否 200）
     * 对应教程的：DELETE /carousel/delete?id=5
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        carouselService.delete(id);
        return Result.ok();
    }
}
