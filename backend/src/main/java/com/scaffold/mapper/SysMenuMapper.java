package com.scaffold.mapper;

import com.scaffold.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysMenuMapper {

    SysMenu findById(@Param("id") Long id);

    List<SysMenu> findAll();

    List<SysMenu> findPage(@Param("offset") int offset, @Param("limit") int limit);

    long countPage();

    int insert(SysMenu menu);

    int update(SysMenu menu);

    int deleteById(@Param("id") Long id);
}
