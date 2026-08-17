package com.athena.common.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * @author Mr.sun
 * @date 2022/1/20 15:06
 * @description
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DictModelMany extends DictModel {

    @Serial
    private static final long serialVersionUID = -6327736172656211312L;

    /**
     * 字典code，根据多个字段code查询时才用到，用于区分不同的字典选项
     */
    private String dictCode;

}
