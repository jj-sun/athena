package com.athena.config;

import com.athena.common.constant.Constant;
import com.athena.common.utils.DateUtils;
import com.athena.common.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author Mr.sun
 * @date 2021/12/20 15:06
 * @description
 */
@Slf4j
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {

        String username = getUsername();

        String ctimeStr = "ctime";
        if (Objects.isNull(this.getFieldValByName(ctimeStr, metaObject))) {
            this.setFieldValByName(ctimeStr, DateUtils.now(), metaObject);
        }
        String mtimeStr = "mtime";
        if (Objects.isNull(this.getFieldValByName(mtimeStr, metaObject))) {
            this.setFieldValByName(mtimeStr, DateUtils.now(), metaObject);
        }
        String creator = "creator";
        if (Objects.isNull(this.getFieldValByName(creator, metaObject)) && StringUtils.isNoneBlank(username)) {
            this.setFieldValByName(creator, username, metaObject);
        }
        String editor = "editor";
        if (Objects.isNull(this.getFieldValByName(editor, metaObject)) && StringUtils.isNoneBlank(username)) {
            this.setFieldValByName(editor, username, metaObject);
        }
        String delFlag = "delFlag";
        if (Objects.isNull(this.getFieldValByName(delFlag, metaObject))) {
            this.setFieldValByName(delFlag, Constant.DelFlag.DEL_FLAG_0.getValue(), metaObject);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        String mtimeStr = "mtime";
        String editor = "editor";
        String username = getUsername();
        this.setFieldValByName(mtimeStr, DateUtils.now(), metaObject);
        if (Objects.isNull(this.getFieldValByName(editor, metaObject)) && StringUtils.isNoneBlank(username)) {
            this.setFieldValByName(editor, username, metaObject);
        }
    }

    private String getUsername() {
        String username;
        try {
            username = SecurityUtil.getLoginUsername();
        }catch (Exception e) {
            username = "";
            log.error("人员自动填充异常，无法获取登录用户名！{}", e.getMessage());
        }
        return username;
    }

}
