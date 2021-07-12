package com.kkl.kklplus.golden.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class GoldenCallbackLog implements Serializable {

    private Long id;

    private Long settlementId;

    private String withdrawNo;

    private String interfaceName;

    private String infoJson;

    private Integer processFlag;    //0   1  2 3 4  成功并

    private Integer  processTime;

    private String processComment;

    private Long createBy;

    private Long createDate;

    private Long updateBy;

    private Long updateDate;

    private String quarter;

}
