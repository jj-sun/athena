package com.athena.common.base.tree;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author Mr.sun
 * @date 2021/12/20 16:03
 * @description
 */
@Data
public abstract class BaseTree<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2329491366699058271L;

    private T info;

    private String key;

    private String parentKey;

    private String title;

    private boolean disabled;

    private List<BaseTree<T>> children;

    /**
     * 信息填充
     */
    public abstract void toTree();

}
