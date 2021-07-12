package com.kkl.kklplus.golden.commonEnum;

import lombok.Getter;
import lombok.Setter;

public enum CreateForBatchEnum {


    FINANCIAL_AUDIT(300,"财务审核中"),
    MERCHANT_PAT(600,"商户已支付"),
    CONFIRMED_USER(610,"待用户确认"),
    PAID(1000,"已打款"),
    PAY_FAIL(1004,"打款失败");

    @Getter
    @Setter
    private int value;

    @Setter
    @Getter
    private String name;

    private CreateForBatchEnum(int value ,String name){
        this.value = value;
        this.name = name;
    }
}
