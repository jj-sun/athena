package com.athena.modules.sys.controller;

import com.athena.common.excel.ExcelUtils;
import com.athena.common.utils.SecurityUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller公共组件
 *
 * @author Mr.sun
 */
public abstract class AbstractController {
	protected Logger logger = LoggerFactory.getLogger(getClass());


	protected String getUsername() {
		return SecurityUtil.getLoginUsername();
	}

	/**
	 * 导出 Excel（供业务 Controller 继承使用）。
	 *
	 * @param response  响应对象
	 * @param fileName  文件名，例如“用户列表_20250101120000.xlsx”
	 * @param sheetName sheet 名称
	 * @param head      Excel 行类型，字段使用 {@code @ExcelProperty} 标注
	 * @param data      数据集合
	 */
	protected <T> void exportExcel(HttpServletResponse response, String fileName, String sheetName,
								   Class<T> head, List<T> data) {
		ExcelUtils.write(response, fileName, sheetName, head, data);
	}

	/**
	 * 导入 Excel（供业务 Controller 继承使用）。
	 *
	 * <p>返回列表的下标与 Excel 数据行对应：第 1 行为表头，第 N 条数据对应 Excel 第 N+1 行。
	 * 空行会以全字段为空的对象返回，调用方可使用 {@link ExcelUtils#isBlankRow(Object)} 跳过。
	 *
	 * @param file      上传的 Excel 文件
	 * @param headClass Excel 行类型，字段使用 {@code @ExcelProperty} 标注
	 * @return 解析后的数据列表
	 */
	protected <T> List<T> importExcel(MultipartFile file, Class<T> headClass) {
		return ExcelUtils.read(file, headClass);
	}
}
