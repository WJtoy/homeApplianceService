package com.kkl.kklplus.golden.mapper;

import com.kkl.kklplus.golden.entity.GoldenCallbackLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoldenCallbackLogMapper {

    List<GoldenCallbackLog> select();

    Integer insert(GoldenCallbackLog goldenCallbackLog);

    Long update(GoldenCallbackLog goldenCallbackLog);

    GoldenCallbackLog selectOne(@Param("settlementId") Long settlementId);


}
