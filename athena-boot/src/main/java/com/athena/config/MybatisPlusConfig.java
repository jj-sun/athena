package com.athena.config;

import com.athena.common.constant.Constant;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mybatis-plus配置
 *
 * @author Mr.sun
 */
@Configuration
@MapperScan(value={"com.athena.modules.**.mapper*"})
public class MybatisPlusConfig {
    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        // 分页上限兜底：即使某个接口漏了校验，也无法通过超大 pageSize 一次拖走整表
        pagination.setMaxLimit(Constant.MAX_PAGE_SIZE);
        interceptor.addInnerInterceptor(pagination);
        // 防止全表更新与删除：拦截没有 where 条件的 update/delete，避免误操作或注入导致整表被清空
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }

}
