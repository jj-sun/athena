package com.athena.common.utils;

import com.athena.common.base.tree.BaseTree;
import com.athena.common.constant.Constant;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mr.sun
 * @date 2021/12/20 15:56
 * @description 构建树结构
 */
public class TreeUtils {

    public static<T> List<? extends BaseTree<T>> buildTree(List<? extends BaseTree<T>> all) {
        if(CollectionUtils.isEmpty(all)) {
            return null;
        }
        List<BaseTree<T>> root = all.stream().filter(tree -> tree.getParentKey().equals(Constant.TREE_ROOT)).collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(root)) {
            buildTree(root, all);
        }
        return root;
    }

    public static<T> void buildTree(List<? extends BaseTree<T>> roots, List<? extends BaseTree<T>> all) {
        if(CollectionUtils.isEmpty(roots)) {
            return;
        }
        findChildren(roots, all);
    }

    private static<T> void findChildren(List<? extends BaseTree<T>> roots, List<? extends BaseTree<T>> all) {
        if(CollectionUtils.isEmpty(roots)) {
            return;
        }
        roots.forEach(root -> {
            List<BaseTree<T>> childrenList = all.stream().filter(tree -> tree.getParentKey().equals(root.getKey())).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(childrenList)) {
                root.setChildren(childrenList);
                findChildren(childrenList, all);
            }
        });
    }
}
