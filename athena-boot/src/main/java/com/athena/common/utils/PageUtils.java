package com.athena.common.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页工具类
 *
 * @author Mr.sun
 */
public class PageUtils<T> implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	/**
	 * 总记录数
	 */
	private int total;
	/**
	 * 每页记录数
	 */
	private int pageSize;
	/**
	 * 总页数
	 */
	private int pages;
	/**
	 * 当前页数
	 */
	private int current;
	/**
	 * 列表数据
	 */
	private List<T> list;
	
	/**
	 * 分页
	 * @param list        列表数据
	 * @param total  总记录数
	 * @param pageSize    每页记录数
	 * @param current    当前页数
	 */
	public PageUtils(List<T> list, int total, int pageSize, int current) {
		this.list = list;
		this.total = total;
		this.pageSize = pageSize;
		this.current = current;
		this.pages = (int)Math.ceil((double) total /pageSize);
	}

	/**
	 * 分页
	 */
	@SuppressWarnings("unchecked")
	public PageUtils(IPage<?> page) {
		this.list = (List<T>) page.getRecords();
		this.total = (int)page.getTotal();
		this.pageSize = (int)page.getSize();
		this.current = (int)page.getCurrent();
		this.pages = (int)page.getPages();
	}

	public int getTotal() {
		return total;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getPages() {
		return pages;
	}

	public int getCurrent() {
		return current;
	}

	public List<T> getList() {
		return list;
	}

}