package com.athena.common.base.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Mr.sun
 * @date 2022/1/14 16:17
 * @description
 */
@Data
public class PageDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -6531517433119278637L;

    private Long page;

    private Long pageSize;

    /**
     * 排序字段。由客户端传入，必须经 SQLFilter.requireOrderField 白名单校验后才能使用。
     */
    private String sidx;

    /**
     * 排序方式，仅用于选择 ASC / DESC 字面量，不参与 SQL 拼接。
     */
    private String order;

}
