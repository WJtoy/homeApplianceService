package com.kkl.kklplus.golden.commonEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Administrator
 */
public enum GoldenProcessFlag {

    PROCESS_FLAG_ACCEPT(0, "受理", "接受成功，但是还未处理"),
    PROCESS_FLAG_PROCESSING(1, "执行", "接收成功，正在执行业务数据处理"),
    PROCESS_FLAG_REJECT(2, "拒绝", "接收成功，但是执行处理生成业务数据失败，业务数据不满足要求"),
    PROCESS_FLAG_FAILURE(3, "失败", "接收成功，但是执行处理生成业务数据报错"),
    PROCESS_FLAG_SUCCESS(4, "成功", "接收成功，并且执行处理生成业务数据成功");

    public int value;
    public String label;
    public String description;
    private static final Map<Integer, GoldenProcessFlag> MAP = new HashMap();

    private GoldenProcessFlag(int value, String label, String description) {
        this.value = value;
        this.label = label;
        this.description = description;
    }

    public static GoldenProcessFlag get(int value) {
        return (GoldenProcessFlag)MAP.get(value);
    }

    public static List<GoldenProcessFlag> getAllProcessFlags() {
        return (List)MAP.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
    }

    static {
        MAP.put(PROCESS_FLAG_ACCEPT.value, PROCESS_FLAG_ACCEPT);
        MAP.put(PROCESS_FLAG_PROCESSING.value, PROCESS_FLAG_PROCESSING);
        MAP.put(PROCESS_FLAG_REJECT.value, PROCESS_FLAG_REJECT);
        MAP.put(PROCESS_FLAG_FAILURE.value, PROCESS_FLAG_FAILURE);
        MAP.put(PROCESS_FLAG_SUCCESS.value, PROCESS_FLAG_SUCCESS);
    }
}
