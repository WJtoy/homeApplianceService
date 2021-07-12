package com.kkl.kklplus.golden.mapper;

import com.kkl.kklplus.golden.entity.GoldenProcessLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GoldenProcesslogMapper {


    List<GoldenProcessLog> select();

    Integer insert(GoldenProcessLog goldenProcessLog);

    /**
     * 更新请求处理结果
     * @param processLog
     * @return
     */
    Integer updateProcessFlag(GoldenProcessLog processLog);
}
