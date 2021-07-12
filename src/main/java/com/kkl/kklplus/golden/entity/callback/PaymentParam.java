package com.kkl.kklplus.golden.entity.callback;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentParam {

    private String accountNum;

    private String orderRandomCode;

    private String settlementCode;

    private Integer status;

    private BigDecimal serviceAmount;

    private BigDecimal settleAmount;

    private BigDecimal totalMoney;

    private String failReason;

    private Boolean hangupFlag;

    private String verificationErrorCode;

    private BigDecimal surplusMoney;

    private String hangupMsg;

    private String hangupBeginTime;

    private Boolean isVerification;

    private String projectCode;

    private String payTime;

    private String handleTime;

}
