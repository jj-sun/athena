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

    private String sidx;

    private String order;

    private String defaultOrderField;

    private boolean isAsc;

}
