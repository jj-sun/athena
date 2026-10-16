package com.athena.common.utils;

import com.athena.common.base.dto.PageDto;
import com.athena.common.constant.Constant;
import com.athena.common.xss.SQLFilter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 查询参数
 *
 * @author Mr.sun
 */
public class Query<T> {

    private static final long DEFAULT_PAGE_SIZE = 10L;

    /**
     * 只分页，不指定默认排序。
     */
    public IPage<T> getPage(PageDto pageDto) {
        return buildPage(pageDto, null, false);
    }

    /**
     * 分页 + 默认排序。
     *
     * <p>默认排序字段由调用方以服务端常量传入，不再从请求参数读取 —— 原实现把
     * {@code pageDto.defaultOrderField} 直接拼进 ORDER BY 且不做任何校验，构成 SQL 注入入口，
     * 而该字段实际从未被前端使用。
     *
     * @param pageDto           分页参数（page / pageSize / sidx / order 均来自客户端）
     * @param defaultOrderField 默认排序字段，可为 null；必须由服务端代码提供，不得来自请求
     * @param asc               默认排序是否升序
     */
    public IPage<T> getPage(PageDto pageDto, String defaultOrderField, boolean asc) {
        return buildPage(pageDto, defaultOrderField, asc);
    }

    private IPage<T> buildPage(PageDto pageDto, String defaultOrderField, boolean asc) {
        // 分页参数（做上下界钳制，防止页码为负或 pageSize 过大导致整表拖取）
        long curPage = 1L;
        long limit = DEFAULT_PAGE_SIZE;

        if (Objects.nonNull(pageDto) && Objects.nonNull(pageDto.getPage()) && pageDto.getPage() > 0) {
            curPage = pageDto.getPage();
        }
        if (Objects.nonNull(pageDto) && Objects.nonNull(pageDto.getPageSize()) && pageDto.getPageSize() > 0) {
            limit = Math.min(pageDto.getPageSize(), Constant.MAX_PAGE_SIZE);
        }

        Page<T> page = new Page<>(curPage, limit);

        // 前端字段排序：sidx 走白名单校验，order 只用于在 ASC / DESC 两个字面量之间选择
        String orderField = pageDto == null ? null : SQLFilter.requireOrderField(pageDto.getSidx());
        String order = pageDto == null ? null : pageDto.getOrder();

        if (StringUtils.isNotEmpty(orderField) && StringUtils.isNotEmpty(order)) {
            if (Constant.ASC.equalsIgnoreCase(order)) {
                return page.addOrder(OrderItem.asc(orderField));
            }
            return page.addOrder(OrderItem.desc(orderField));
        }

        // 没有指定排序字段时，使用调用方给定的默认排序
        String safeDefault = SQLFilter.requireOrderField(defaultOrderField);
        if (StringUtils.isBlank(safeDefault)) {
            return page;
        }
        if (asc) {
            page.addOrder(OrderItem.asc(safeDefault));
        } else {
            page.addOrder(OrderItem.desc(safeDefault));
        }
        return page;
    }
}
