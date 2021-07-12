package com.kkl.kklplus.golden.http.command;

import com.kkl.kklplus.golden.http.request.RequestParam;
import com.kkl.kklplus.golden.http.request.BalanceParam;
import com.kkl.kklplus.golden.http.request.CreateForBatchParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class OperationCommand {

    public enum OperationCode {

        CREATEFORBATCH(1001, "批量创建结算单", "CreateForBatch", CreateForBatchParam.class),

        REFUNDBALANCE(1002,"结算单退款","refundBalance", BalanceParam.class),

        GETBALANCE(1003,"查询结算单","getbalance",BalanceParam.class);

        public int code;
        public String name;
        public String apiUrl;
        public Class reqBodyClass;

        private OperationCode(int code, String name, String apiUrl, Class reqBodyClass) {
            this.code = code;
            this.name = name;
            this.apiUrl = apiUrl;
            this.reqBodyClass = reqBodyClass;
        }
    }

    @Getter
    @Setter
    private OperationCode opCode;

    @Setter
    @Getter
    private RequestParam requestParam;


    public static OperationCommand newInstance(OperationCode opCode,RequestParam requestParam) {
        OperationCommand command = new OperationCommand();
        command.opCode = opCode;
        command.requestParam = requestParam;
        return command;
    }
}
