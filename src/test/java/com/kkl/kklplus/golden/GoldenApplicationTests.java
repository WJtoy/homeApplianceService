package com.kkl.kklplus.golden;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kkl.kklplus.entity.golden.mq.MQGoldenSettlementsMessage;
import com.kkl.kklplus.entity.golden.mq.MQGoldenSettlementsStatusUpdateMessage;
import com.kkl.kklplus.golden.http.request.CommonParam;
import com.kkl.kklplus.golden.http.request.CreateForBatchParam;
import com.kkl.kklplus.golden.http.response.ResponseBody;

import com.kkl.kklplus.golden.mq.sender.GoldenSettlementMessageSender;
import com.kkl.kklplus.golden.mq.sender.GoldenSettlementsCoreMessageSender;
import com.kkl.kklplus.golden.utils.QuarterUtils;
import com.kkl.kklplus.golden.http.command.OperationCommand;
import com.kkl.kklplus.golden.http.utils.OkHttpUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
public class GoldenApplicationTests {

    @Autowired
    GoldenSettlementsCoreMessageSender goldenSettlementsCoreMessageSender;

    @Autowired
    GoldenSettlementMessageSender goldenSettlementMessageSender;


    @Test
    public void test4(){
        String date = "{\"name\": \"普安\", \"bank_name\": \"工行\", \"phone_num\": \"13540312140\", \"payment_way\": 1, \"bankcard_num\": \"4678877856554\", \"settle_amount\": 100.7, \"certificate_num\": \"445281200101144612\", \"certificate_type\": 1, \"order_random_code\": \"T2020091000007\"}";
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        DecimalFormat df1 = new DecimalFormat("#.00");
        System.out.println(new BigDecimal(100.745).setScale(2,BigDecimal.ROUND_HALF_UP));
    }


    @Test
    public void test5(){
        CreateForBatchParam createForBatchParam = new CreateForBatchParam();

        CommonParam commonParam = new CommonParam();
        commonParam.setRequestId("123");
        commonParam.setTime(System.currentTimeMillis()/1000);

        OperationCommand command = OperationCommand.newInstance(OperationCommand.OperationCode.CREATEFORBATCH,createForBatchParam);

        ResponseBody<ResponseBody> resBody = OkHttpUtils.postSyncGenericNew(command, ResponseBody.class,commonParam);

        System.out.println(resBody);
    }

    @Test
    public void test6(){
        MQGoldenSettlementsMessage.GoldenSettlementsMessage goldenSettlementsMessage = MQGoldenSettlementsMessage.GoldenSettlementsMessage
                .newBuilder()
                .setBank(2)
                .setBankName("中国银行")
                .setBankOwner("王炬")
                .setBankNo("6217857000084608767")
                .setBankOwnerPhone("13751302397")
                .setBankOwnerIdno("430422199705289854")
                .setCertificateType(1)
                .setPayAmount(1.00)
                .setQuarter(QuarterUtils.getQuarter(new Date()))
                .setPaymentType(10)
                .setPaymentWay(1)
                .setWithdrawId(1097400639060840467L)
                .setWithdrawNo("T2019021800017")
                .setDiscountFlag(1)
                .setInvoiceFlag(1)
                .setServicePointId(1622038)
                .build();
        goldenSettlementsCoreMessageSender.send(goldenSettlementsMessage);
    }


    @Test
    public void test7(){
        MQGoldenSettlementsStatusUpdateMessage.GoldenSettlementsStatusUpdateMessage goldenSettlementsStatusUpdateMessage = MQGoldenSettlementsStatusUpdateMessage.GoldenSettlementsStatusUpdateMessage.newBuilder()
                .setWithdrawId(1315906933307346944L)
                .setStatus(40)
                .setCreateBy(2)
                .setRemarks("测试")
                .setQuarter("20203")
                .setPayTime(System.currentTimeMillis())
                .setPayAmount(9.5)
                .build();
        goldenSettlementMessageSender.send(goldenSettlementsStatusUpdateMessage);
    }

}
