package com.athena.common.base.select;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Mr.sun
 * @date 2022/1/25 15:14
 * @description
 */
@Data
public class BaseSelect implements Serializable {
    @Serial
    private static final long serialVersionUID = 2735605173653989152L;
    private String label;
    private String value;
    private boolean disabled;

    public BaseSelect() {}

    public BaseSelect(String label,String value) {
        this.label = label;
        this.value = value;
    }

}
