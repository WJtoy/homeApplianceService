package com.kkl.kklplus.golden.http.request;

import lombok.Data;

@Data
public class BalanceParam extends RequestParam {

    private String settlementCode;     //结算单号

    private String orderRandomCode;   //客户订单号 必填其一

}
