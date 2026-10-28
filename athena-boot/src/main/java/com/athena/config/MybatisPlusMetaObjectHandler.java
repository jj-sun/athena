package com.athena.config;

import com.athena.common.constant.Constant;
import com.athena.common.utils.DateUtils;
import com.athena.common.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

/**
 * @author Mr.sun
 * @date 2021/12/20 15:06
 * @description
 */
@Slf4j
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    /**
     * 新增时填充。
     *
     * <p>审计字段与逻辑删除标记<b>一律以服务端为准</b>，不再"仅为空时填充" ——
     * 否则客户端可以在请求体里塞入 creator/ctime/delFlag 伪造审计信息或创建已删除记录。
     * 主键同样清空，交由 ASSIGN_ID 生成，避免客户端指定主键。
     */
    @Override
    public void insertFill(MetaObject metaObject) {

        String username = getUsername();

        // 主键一律由服务端生成：insertFill 在 MyBatis-Plus 主键填充之后执行
        forceSet(metaObject, "ctime", DateUtils.now());
        forceSet(metaObject, "mtime", DateUtils.now());
        forceSet(metaObject, "creator", StringUtils.isNoneBlank(username) ? username : null);
        forceSet(metaObject, "editor", StringUtils.isNoneBlank(username) ? username : null);
        forceSet(metaObject, "delFlag", Constant.DelFlag.DEL_FLAG_0.getValue());
    }

    /**
     * 更新时填充。
     *
     * <p>{@code editor}/{@code mtime} 是 INSERT_UPDATE 填充字段，SQL 无条件包含它们，
     * 这里用服务端值覆盖客户端传入的值。
     *
     * <p>{@code creator}/{@code ctime}/{@code delFlag} 不在这里处理：MyBatis-Plus 是在
     * {@code updateFill} 之前、根据实体当时的字段值构建 SET 子句的，在填充阶段把它们置 null
     * 只会把绑定值改成 NULL 并真的写库。正确做法是在 {@code BasePo} 上声明
     * {@code updateStrategy = FieldStrategy.NEVER}，让它们根本不进入 UPDATE 语句。
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        String username = getUsername();

        forceSet(metaObject, "mtime", DateUtils.now());
        forceSet(metaObject, "editor", StringUtils.isNoneBlank(username) ? username : null);
    }

    /**
     * 强制写入字段值，包括 null。
     *
     * <p>不能用 {@code setFieldValByName}：它在 MyBatis-Plus 3.5 中对 null 值是空操作。
     */
    private void forceSet(MetaObject metaObject, String field, Object value) {
        if (metaObject.hasSetter(field)) {
            metaObject.setValue(field, value);
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
