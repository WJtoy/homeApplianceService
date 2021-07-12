package com.kkl.kklplus.golden.mapper;

import com.github.pagehelper.Page;


import com.kkl.kklplus.entity.golden.entity.GoldenSettlements;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface GoldenSettlementsMapper {

    List<GoldenSettlements> select(Object parameter);

    Integer insert(GoldenSettlements goldenSettlements);

    GoldenSettlements selectOne(@Param("withdrawId") String withdrawId);

    GoldenSettlements selectById(@Param("id")Long id);


    void update(GoldenSettlements goldenSettlements);

    /**
     * 关闭退款
     * @param id
     * @param updateById
     * @param updateDt
     * @return
     */
    Integer closeRefund(@Param("id") Long id,
                        @Param("updateById") Long updateById,
                        @Param("updateDt") long updateDt);

    /**
     * 批量查询付款单对应的结算单信息
     * @param withdrawIds
     * @return
     */
    @MapKey("withdrawId")
    Map<Long, GoldenSettlements> batchSearchSettlementsByWithdrawIds(@Param("withdrawIds") List<Long> withdrawIds);

    /**
     * 更新为执行中
     * @param id
     * @return
     */
    Integer updateStatusToExecute(@Param("id") Long id);

    /**
     * 请求结果更新
     * @param goldenSettlements
     * @return
     */
    Integer updateReqResult(GoldenSettlements goldenSettlements);


    /**
     *
     * @param goldenSettlements
     * @return
     */
    Integer updateByStatus(GoldenSettlements goldenSettlements);


    /**
     * 退款更新
     * @param goldenSettlements
     */
    Integer updateResult(GoldenSettlements goldenSettlements);

    /**
     * 退结算单并更新
     * @param goldenSettlements
     * @return
     */
    Integer updateBySubStatus(GoldenSettlements goldenSettlements);

    /**
     *
     * @param parameter
     * @return
     */
    Page<GoldenSettlements> selectFailListByStatus(Object parameter);



    /**
     * 分页查询处理中的列表
     * @param parameter
     * @return
     */
    Page<GoldenSettlements> selectProcessListByStatus(Object parameter);

    /**
     * 分页查询付款成功的列表
     * @param parameter
     * @return
     */
    Page<GoldenSettlements> selectSuccessListByStatus(Object parameter);

}
