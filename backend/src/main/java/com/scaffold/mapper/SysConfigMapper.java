package com.scaffold.mapper;

import com.scaffold.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysConfigMapper {

    SysConfig findByKey(@Param("configKey") String configKey);

    List<SysConfig> findAll();

    int insert(@Param("configKey") String configKey,
               @Param("configValue") String configValue,
               @Param("remark") String remark);

    int updateValue(@Param("configKey") String configKey,
                    @Param("configValue") String configValue,
                    @Param("remark") String remark);
}
