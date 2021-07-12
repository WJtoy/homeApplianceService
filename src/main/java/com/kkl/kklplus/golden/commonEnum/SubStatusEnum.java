package com.kkl.kklplus.golden.commonEnum;

import lombok.Getter;
import lombok.Setter;

public enum SubStatusEnum {

    REQUESTING(15,"请求中"),
    REQ_EXCEPTION(18,"请求异常"),
    PROCESS(20,"处理中"),
    REFUNDED(30,"待退款"),
    REFUNDING(35,"退款中"),
    FAIL_REFUND(38,"退款失败"),
    SUCCESS_REFUND(39,"退款成功");

    @Setter
    @Getter
    private Integer value;

    @Setter
    @Getter
    private String name;

    private SubStatusEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

}
