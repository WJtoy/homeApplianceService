package com.kkl.kklplus.golden.http.request;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CreateForBatchParam extends RequestParam {

    private String orderRandomCode;   //客户订单号，每个结算单唯一
    private String name; //姓名

    private Integer certificateType; //  证件类型  1 3 4 5
    private String certificateNum;  //证件号
    private String phoneNum; //手机号

    private Double settleAmount; //结算金额
    private String bankName; //银行名称
    private String bankcardNum;    //银行卡号
    private Integer paymentWay; //收款方式

    private String paymentAccount; //支付宝收款账号
    private String alipayAccountid;
    private String WxOpenId;
    private String WxAppid;

}
