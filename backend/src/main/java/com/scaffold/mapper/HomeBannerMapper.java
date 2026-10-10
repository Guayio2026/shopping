package com.scaffold.mapper;

import com.scaffold.entity.HomeBanner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HomeBannerMapper {

    HomeBanner findById(@Param("id") Long id);

    List<HomeBanner> findAll(@Param("status") Integer status);

    List<HomeBanner> findPage(@Param("keyword") String keyword,
                              @Param("offset") int offset,
                              @Param("limit") int limit);

    long countPage(@Param("keyword") String keyword);

    int insert(HomeBanner banner);

    int update(HomeBanner banner);

    int deleteById(@Param("id") Long id);
}
