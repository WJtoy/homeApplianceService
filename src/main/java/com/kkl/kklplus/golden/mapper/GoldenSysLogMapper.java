package com.kkl.kklplus.golden.mapper;

import com.kkl.kklplus.golden.entity.GoldenSysLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GoldenSysLogMapper {

    List<GoldenSysLog> select();

    Long insert(GoldenSysLog goldenSysLog);

    void update(Object parameter);

}
