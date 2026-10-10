package com.scaffold.controller;

import com.scaffold.common.PageResult;
import com.scaffold.common.Result;
import com.scaffold.entity.Type;
import com.scaffold.service.TypeService;
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
 * 商品分类 接口层（前端连到后端的「大门」）
 *
 * ============================ 前后端连接对照表 ============================
 *  前端「分类管理」页面上的操作   →  前端调用的函数         →  打到本类的哪个方法
 *  ------------------------------------------------------------------------
 *  打开页面 / 点「查询」/ 翻页   →  getTypePage(params)   →  page()    GET  /page
 *  （下拉框加载分类列表）         →  getTypeList()         →  list()    GET  /list
 *  （按 id 取单条）               →  （当前页面未用到）      →  getById() GET  /{id}
 *  弹窗里点「确定」（新增模式）   →  createType(payload)  →  create()  POST /
 *  弹窗里点「确定」（编辑模式）   →  updateType(id,data)  →  update()  PUT  /{id}
 *  表格里点「删除」               →  deleteType(row.id)   →  delete()  DELETE /{id}
 * ==========================================================================
 *
 * 【完整链路】
 * 浏览器 localhost:5173  →  Types.vue  →  api/type.js  →  request.js（加 /api 前缀 + token）
 *                        →  Vite 代理转发  →  【本类】  →  TypeService  →  TypeMapper.xml  →  MySQL
 *
 * 【URL 前面有个 /api】
 * application.yml 里配了 context-path: /api。所以本类 @RequestMapping("/type")
 * 加上方法上的 "/page"，最终地址是：http://localhost:8080/api/type/page
 *
 * 【必须带 token】
 * /type/** 不在 WebMvcConfig 的免登录白名单里（白名单只有 /auth/**、/public/**、/health、swagger）。
 * 请求头必须带 Authorization: Bearer xxx，否则返回 {"code":401,"message":"未登录或 token 无效"}。
 * 这件事前端 api/request.js 已经自动做了，所以页面里不用管。
 *
 * 【返回值为什么是 Result】
 * 统一格式 {"code":200,"message":"success","data":...}，前端 request.js 会自动把 data 拆出来，
 * 所以 Types.vue 里拿到的直接就是数据本身（比如 { total, list }）。
 */
@RestController           // = @Controller + @ResponseBody，方法返回值自动转成 JSON
@RequestMapping("/type")  // 类级路径前缀：本类所有接口都以 /type 开头
@RequiredArgsConstructor
public class TypeController {

    private final TypeService typeService; // 注入业务层，真正的活交给它干

    /**
     * 【查-分页】GET /api/type/page?keyword=数码&page=1&size=10
     *
     * 谁在调：Types.vue 的 loadData() → api/type.js 的 getTypePage()
     * 什么时候：页面一打开、点「查询」、按回车、翻页 —— 都会走这里
     * 返回：{ total, list }，total 给分页控件，list 给表格
     */
    @GetMapping("/page")
    public Result<PageResult<Type>> page(@RequestParam(defaultValue = "") String keyword,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return Result.ok(typeService.page(keyword, page, size));
    }

    /**
     * 【查-全部】GET /api/type/list
     *
     * 谁在调：api/type.js 的 getTypeList()（供下拉框等场景；当前分类页面没用到，留着给 goods 用）
     * 返回：全部分类数组（不分页）
     */
    @GetMapping("/list")
    public Result<List<Type>> list() {
        return Result.ok(typeService.listAll());
    }

    /**
     * 【查-单条】GET /api/type/5
     *
     * {id} 是路径参数，靠 @PathVariable 从地址里取出来（不是从问号后面取）
     * 谁在调：api/type.js 的（暂无）；教程里编辑前回查详情会用到
     */
    @GetMapping("/{id}")
    public Result<Type> getById(@PathVariable Long id) {
        return Result.ok(typeService.getById(id));
    }

    /**
     * 【增】POST /api/type
     *
     * 谁在调：Types.vue 的 handleSubmit() → api/type.js 的 createType(payload)
     * 什么时候：点「新增分类」打开弹窗 → 填完名称 → 点「确定」
     * 请求体 JSON：{"name":"图书","sortOrder":5}   ← 靠 @RequestBody 转成 Type 对象
     * 返回：新增后的完整记录（带数据库生成的 id 和创建时间）
     */
    @PostMapping
    public Result<Type> create(@RequestBody Type type) {
        return Result.ok(typeService.create(type));
    }

    /**
     * 【改】PUT /api/type/5
     *
     * 谁在调：Types.vue 的 handleSubmit()（编辑模式）→ api/type.js 的 updateType(id, payload)
     * 什么时候：表格里点「编辑」→ 改完 → 点「确定」
     * id 在地址里（@PathVariable），要改的内容在请求体 JSON 里（@RequestBody）
     */
    @PutMapping("/{id}")
    public Result<Type> update(@PathVariable Long id, @RequestBody Type type) {
        return Result.ok(typeService.update(id, type));
    }

    /**
     * 【删】DELETE /api/type/5
     *
     * 谁在调：Types.vue 的 handleDelete(row) → api/type.js 的 deleteType(row.id)
     * 什么时候：表格里点「删除」→ 确认框点「确定」
     * 返回：Result.ok()（data 为 null，前端只看 code 是否 200）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        typeService.delete(id);
        return Result.ok();
    }
}
