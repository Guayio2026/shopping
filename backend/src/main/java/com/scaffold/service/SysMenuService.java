package com.scaffold.service;

import com.scaffold.common.BusinessException;
import com.scaffold.common.PageResult;
import com.scaffold.entity.SysMenu;
import com.scaffold.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysMenuService {

    private final SysMenuMapper sysMenuMapper;

    public List<SysMenu> listAll() {
        return sysMenuMapper.findAll();
    }

    public PageResult<SysMenu> page(int page, int size) {
        int offset = (page - 1) * size;
        long total = sysMenuMapper.countPage();
        List<SysMenu> list = sysMenuMapper.findPage(offset, size);
        return PageResult.of(total, list);
    }

    public SysMenu getById(Long id) {
        SysMenu menu = sysMenuMapper.findById(id);
        if (menu == null) {
            throw new BusinessException(404, "菜单不存在");
        }
        return menu;
    }

    public SysMenu create(SysMenu menu) {
        if (menu.getStatus() == null) {
            menu.setStatus(1);
        }
        if (menu.getSortOrder() == null) {
            menu.setSortOrder(0);
        }
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        sysMenuMapper.insert(menu);
        return sysMenuMapper.findById(menu.getId());
    }

    public SysMenu update(Long id, SysMenu menu) {
        getById(id);
        menu.setId(id);
        sysMenuMapper.update(menu);
        return sysMenuMapper.findById(id);
    }

    public void delete(Long id) {
        getById(id);
        sysMenuMapper.deleteById(id);
    }
}
