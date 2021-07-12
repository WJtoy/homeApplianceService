package com.kkl.kklplus.golden.http.utils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kkl.kklplus.golden.entity.param.DataParam;
import com.kkl.kklplus.golden.http.request.CommonParam;
import com.kkl.kklplus.golden.http.command.OperationCommand;
import com.kkl.kklplus.golden.http.config.GoldenProperties;
import com.kkl.kklplus.golden.http.request.RequestParam;
import com.kkl.kklplus.golden.http.response.ResponseBody;
import com.kkl.kklplus.golden.utils.AesUtil;
import com.kkl.kklplus.golden.utils.GsonUtils;
import com.kkl.kklplus.golden.utils.SpringContextHolder;
import com.kkl.kklplus.golden.utils.EncodesUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;

import java.util.ArrayList;

@Slf4j
public class OkHttpUtils {

    private static final MediaType CONTENT_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private static OkHttpClient okHttpClient = SpringContextHolder.getBean(OkHttpClient.class);
    private static GoldenProperties  goldenProperties = SpringContextHolder.getBean(GoldenProperties.class);
    private static Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    public static <T> ResponseBody<T> postSyncGenericNew(OperationCommand command, Class<T> dataClass, CommonParam commonParam) {
        ResponseBody<T> responseBody = new ResponseBody<>();
        if (command.getOpCode()!= null && command != null ) {

            String requestParam = toJsonString(command);  //请求体参数
            //加密
            String aes = AesUtil.encrypt(requestParam, goldenProperties.getGoldenDataConfig().getAppSecret());

            String url = goldenProperties.getGoldenDataConfig().getRequestMainUrl().concat(command.getOpCode().apiUrl);
            RequestBody requestBody = RequestBody.create(CONTENT_TYPE_JSON, toDataJson(aes));
            Request.Builder requestBuilder = new Request.Builder().url(url);
            requestBuilder.addHeader("appkey",goldenProperties.getGoldenDataConfig().getAppKey());
            requestBuilder.addHeader("sign_type",goldenProperties.getGoldenDataConfig().getSignType());
            requestBuilder.addHeader("version",String.valueOf(goldenProperties.getGoldenDataConfig().getVersion()));
            requestBuilder.addHeader("request_id",commonParam.getRequestId());
            requestBuilder.addHeader("timestamp",String.valueOf(commonParam.getTime()));
            requestBuilder.addHeader("signature", EncodesUtils.HMACSHA256(
                            goldenProperties.getGoldenDataConfig().getAppKey()
                                    +commonParam.getTime()
                                    +goldenProperties.getGoldenDataConfig().getAppSecret()
                                    ,goldenProperties.getGoldenDataConfig().getAppSecret()));
            if (!commonParam.getCallBackUrl().isEmpty()) {
                requestBuilder.addHeader("callback_url", Base64.encodeBase64String(commonParam.getCallBackUrl().getBytes()));
            }
            Request request = requestBuilder.post(requestBody).build();

            Call call = okHttpClient.newCall(request);
            try {
                Response response = call.execute();
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String responseBodyJson = response.body().string();
                        responseBody.setOriginalJson(responseBodyJson);
                        try {
                            //解析json
                            T data = gson.fromJson(responseBodyJson, dataClass);
                            responseBody.setData(data);
                        } catch (Exception e) {
                            responseBody = new ResponseBody<>(ResponseBody.ErrorCode.JSON_PARSE_FAILURE, e);
                            return responseBody;
                        }
                    } else {
                        responseBody = new ResponseBody<>(ResponseBody.ErrorCode.HTTP_RESPONSE_BODY_ERROR);
                    }
                } else {
                    responseBody = new ResponseBody<>(ResponseBody.ErrorCode.HTTP_STATUS_CODE_ERROR);
                }
            } catch (Exception e) {
                return new ResponseBody<>(ResponseBody.ErrorCode.REQUEST_INVOCATION_FAILURE, e);
            }
        } else {
            responseBody = new ResponseBody<>(ResponseBody.ErrorCode.REQUEST_PARAMETER_FORMAT_ERROR);
        }
        return responseBody;
    }


    public static String toDataJson(String str){
        DataParam dataParam = new DataParam();
        dataParam.setData(str);
        String data =null;
        try{
            data = GsonUtils.getInstance().toGson(dataParam);
        }catch (Exception e){
            log.error("json-->{}"+e.getMessage());
        }
        return data;
    }

    public static String toJsonString(OperationCommand command){
        String requestParam = null;
        if (command.getOpCode().code==1001) {
            RequestParam param = command.getRequestParam();
            ArrayList<RequestParam> listParam = new ArrayList<>();
            listParam.add(param);
            requestParam = gson.toJson(listParam);
        }else{
            RequestParam param = command.getRequestParam();
            requestParam = gson.toJson(param);
        }
        return requestParam;
    }

}
