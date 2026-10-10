package com.scaffold.mapper;

import com.scaffold.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminMapper {

    Admin findById(@Param("id") Long id);

    Admin findByUsername(@Param("username") String username);

    List<Admin> findPage(@Param("keyword") String keyword,
                         @Param("offset") int offset,
                         @Param("limit") int limit);

    long countPage(@Param("keyword") String keyword);

    int insert(Admin admin);

    int update(Admin admin);

    int updatePassword(@Param("id") Long id, @Param("password") String password);

    int deleteById(@Param("id") Long id);

    long countAll();
}
