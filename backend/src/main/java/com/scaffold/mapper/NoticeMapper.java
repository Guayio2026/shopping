package com.scaffold.mapper;

import com.scaffold.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeMapper {

    Notice findById(@Param("id") Integer id);

    List<Notice> findAll();

    List<Notice> findPage(@Param("keyword") String keyword,
                          @Param("offset") int offset,
                          @Param("limit") int limit);

    long countPage(@Param("keyword") String keyword);

    int insert(Notice notice);

    int update(Notice notice);

    int deleteById(@Param("id") Integer id);
}
