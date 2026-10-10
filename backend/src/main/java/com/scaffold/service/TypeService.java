package com.scaffold.service;

import com.scaffold.common.BusinessException;
import com.scaffold.common.PageResult;
import com.scaffold.entity.Type;
import com.scaffold.mapper.TypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品分类 业务层（写业务规则的地方）
 *
 * 【在链路中的位置】
 * 前端页面 → TypeController【大门】→ 【本类】→ TypeMapper 接口 → TypeMapper.xml【SQL】→ MySQL
 *
 * 本类不碰 HTTP，也不写 SQL，只做"业务判断"：
 *  - 参数合不合法（比如排序没填就给默认值）
 *  - 数据存不存在（删除前先查一下，查不到就抛异常）
 *  - 分页怎么算（offset 怎么来）
 *  - 返回给 Controller 什么结构
 *
 * 【注意】本项目不用"接口 + impl 实现类"那种写法，直接一个 @Service 类写完
 * （全项目 13 个 Service 都是这个风格）。教程里的 ITypeService + impl/TypeServiceImpl，
 * 对应到本项目就是本类，逻辑写在同一处。
 *
 * 【@RequiredArgsConstructor】Lombok 自动生成构造方法，把 final 字段注入进来，
 * 所以不用手写 @Autowired。
 */
@Service // 交给 Spring 管理，Controller 里能直接注入
@RequiredArgsConstructor
public class TypeService {

    /** 注入 Mapper：本类访问数据库全靠它（它背后就是 TypeMapper.xml 里的 SQL） */
    private final TypeMapper typeMapper;

    /**
     * 分页查询
     *
     * 【谁在用】TypeController.page() ← 前端 getTypePage()（分类管理页面列表）
     * 【返回给前端】PageResult → 序列化成 JSON 的 { total: 4, list: [...] }
     *           前端 Types.vue 里：tableData = data.list（填表格）、total = data.total（填分页）
     *
     * @param keyword 分类名关键字，空则不筛选（对应前端搜索框）
     * @param page    第几页，从 1 开始（对应前端分页控件的当前页）
     * @param size    每页几条（对应前端分页控件的每页条数）
     */
    public PageResult<Type> page(String keyword, int page, int size) {
        // SQL 的 LIMIT 需要的是"跳过多少条"，所以：
        // 第 1 页跳过 0 条 → (1-1)*10 = 0；第 2 页跳过 10 条 → (2-1)*10 = 10
        int offset = (page - 1) * size;

        // 查两次：先要总数（前端分页控件显示"共 N 条"、算总页数），再要当前页的数据
        long total = typeMapper.countPage(keyword);                    // → XML countPage：SELECT COUNT(*)
        List<Type> list = typeMapper.findPage(keyword, offset, size);   // → XML findPage：LIMIT/OFFSET

        return PageResult.of(total, list);  // 打包成 { total, list }，Controller 外层再套 Result
    }

    /**
     * 查询全部分类（不分页）
     * 【谁在用】TypeController.list() ← 前端 getTypeList()，给下拉选择框用
     */
    public List<Type> listAll() {
        return typeMapper.findAll();
    }

    /**
     * 根据 id 查一条，查不到就抛业务异常
     * 【谁来接这个异常】GlobalExceptionHandler 会把它转成
     * {"code":404,"message":"分类不存在"} 返回给前端，前端自动弹红字提示
     */
    public Type getById(Long id) {
        Type type = typeMapper.findById(id);
        if (type == null) {
            throw new BusinessException(404, "分类不存在");
        }
        return type;
    }

    /**
     * 新增分类
     * 【谁在用】TypeController.create() ← 前端弹窗点「确定」（新增模式）
     *
     * 三步：
     *  1. 补默认值（排序没填给 0）
     *  2. 入库（XML 里配了 useGeneratedKeys，插完 type.getId() 会自动变成数据库生成的 id）
     *  3. 回查一遍再返回 —— 这样前端拿到的是完整记录（含 id、created_at），
     *     而不是自己提交的那几个字段
     */
    public Type create(Type type) {
        normalize(type);
        typeMapper.insert(type);
        return typeMapper.findById(type.getId());
    }

    /**
     * 修改分类
     * 【谁在用】TypeController.update() ← 前端弹窗点「确定」（编辑模式）
     *
     * 注意第 2 步：以 URL 上的 id 为准（前端传的 body 里就算有 id 也不信任），
     * 避免"改 A 的地址却把 B 改了"这种事。
     */
    public Type update(Long id, Type type) {
        getById(id);          // 先确认存在，不存在直接抛 404
        type.setId(id);       // 用路径参数里的 id 覆盖
        normalize(type);
        typeMapper.update(type);
        return typeMapper.findById(id);   // 回查返回最新数据
    }

    /**
     * 删除分类
     * 【谁在用】TypeController.delete() ← 前端表格点「删除」
     * 先 getById 确认存在，让"删一条不存在的记录"报 404 而不是假装成功
     */
    public void delete(Long id) {
        getById(id);
        typeMapper.deleteById(id);
    }

    /** 业务规则：排序没填就给 0（避免数据库 sort_order 为 null 时排序混乱） */
    private void normalize(Type type) {
        if (type.getSortOrder() == null) {
            type.setSortOrder(0);
        }
    }
}
