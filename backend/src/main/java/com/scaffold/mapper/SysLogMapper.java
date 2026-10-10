package com.scaffold.mapper;

import com.scaffold.dto.NameCount;
import com.scaffold.entity.SysLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysLogMapper {

    List<SysLog> findPage(@Param("keyword") String keyword,
                          @Param("type") String type,
                          @Param("offset") int offset,
                          @Param("limit") int limit);

    long countPage(@Param("keyword") String keyword, @Param("type") String type);

    long countAll();

    List<NameCount> countDailyCreated(@Param("days") int days);

    int insert(SysLog log);

    int deleteById(@Param("id") Long id);
}
