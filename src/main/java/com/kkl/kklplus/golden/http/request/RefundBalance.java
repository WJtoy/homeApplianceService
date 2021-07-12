package com.kkl.kklplus.golden.http.request;

import lombok.Data;

import java.util.List;

@Data
public class RefundBalance extends RequestParam {

    private List<String> orderRandomCode; //商户订单号

}
