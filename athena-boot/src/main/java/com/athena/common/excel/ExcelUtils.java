package com.athena.common.excel;

import com.athena.common.exception.RRException;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.context.AnalysisContext;
import org.apache.fesod.sheet.read.listener.ReadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Excel 导入导出工具，基于 Apache Fesod（fesod-sheet）。
 *
 * <p>业务 Controller 通过 {@code AbstractController} 的两个模板方法使用，
 * 避免每个模块重复处理响应头、文件名编码、文件校验与行数限制。
 *
 * @author Mr.sun
 */
public final class ExcelUtils {

	private static final Logger log = LoggerFactory.getLogger(ExcelUtils.class);

	/**
	 * 单次导入默认行数上限。
	 */
	public static final int DEFAULT_MAX_IMPORT_ROWS = 5000;

	/**
	 * 导入文件大小上限：10MB。
	 */
	private static final long MAX_IMPORT_FILE_SIZE = 10L * 1024 * 1024;

	private static final String XLSX_CONTENT_TYPE =
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	private ExcelUtils() {
	}

	/**
	 * 导出 Excel 到响应流。
	 *
	 * <p>先在内存中生成完整工作簿，再写入响应。这样生成阶段的异常仍能交给全局异常处理器
	 * 返回统一的 JSON 信封，而不会留下一个已提交响应头、内容损坏的下载文件。
	 *
	 * @param response  响应对象
	 * @param fileName  文件名（不带扩展名时自动补 .xlsx）
	 * @param sheetName sheet 名称
	 * @param head      Excel 表头类型（字段使用 {@code @ExcelProperty} 标注）
	 * @param data      数据集合，允许为 null
	 */
	public static <T> void write(HttpServletResponse response, String fileName, String sheetName,
								 Class<T> head, Collection<T> data) {
		if (response == null) {
			throw new RRException("导出响应对象不能为空");
		}
		if (head == null) {
			throw new RRException("导出数据类型不能为空");
		}

		String safeFileName = StringUtils.isBlank(fileName) ? "export" : fileName.trim();
		if (!safeFileName.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
			safeFileName += ".xlsx";
		}
		String safeSheetName = StringUtils.isBlank(sheetName) ? "Sheet1" : sheetName;
		Collection<T> rows = data == null ? List.of() : data;

		byte[] bytes;
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			FesodSheet.write(buffer, head).sheet(safeSheetName).doWrite(rows);
			bytes = buffer.toByteArray();
		} catch (Exception e) {
			log.error("Excel 生成失败", e);
			throw new RRException("Excel 导出失败");
		}

		String encodedFileName = URLEncoder.encode(safeFileName, StandardCharsets.UTF_8).replace("+", "%20");
		response.setContentType(XLSX_CONTENT_TYPE);
		response.setContentLength(bytes.length);
		response.setHeader("Content-Disposition",
				"attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
		response.setHeader("Cache-Control", "no-store");
		try (OutputStream out = response.getOutputStream()) {
			out.write(bytes);
			out.flush();
		} catch (IOException e) {
			log.error("Excel 响应写出失败", e);
			throw new RRException("Excel 导出失败");
		}
	}

	/**
	 * 读取 Excel，使用默认行数上限。
	 */
	public static <T> List<T> read(MultipartFile file, Class<T> headClass) {
		return read(file, headClass, DEFAULT_MAX_IMPORT_ROWS);
	}

	/**
	 * 读取 Excel 为对象列表。
	 *
	 * <p>关闭了 fesod 的“忽略空行”默认行为，保证返回列表的下标与 Excel 实际行号对应
	 * （表头为第 1 行，数据从第 2 行开始）。空行会以全字段为空的对象返回，
	 * 调用方可用 {@link #isBlankRow(Object)} 跳过。
	 *
	 * @param file     上传文件
	 * @param headClass Excel 行类型（字段使用 {@code @ExcelProperty} 标注）
	 * @param maxRows  最大数据行数
	 */
	public static <T> List<T> read(MultipartFile file, Class<T> headClass, int maxRows) {
		if (file == null || file.isEmpty()) {
			throw new RRException("请上传 Excel 文件");
		}
		if (headClass == null) {
			throw new RRException("导入数据类型不能为空");
		}
		if (file.getSize() > MAX_IMPORT_FILE_SIZE) {
			throw new RRException("Excel 文件不能超过 10MB");
		}
		String originalFilename = file.getOriginalFilename();
		String lowerName = originalFilename == null ? "" : originalFilename.toLowerCase(Locale.ROOT);
		if (!lowerName.endsWith(".xls") && !lowerName.endsWith(".xlsx")) {
			throw new RRException("仅支持 .xls 或 .xlsx 格式的 Excel 文件");
		}

		List<T> rows = new ArrayList<>();
		try (InputStream inputStream = file.getInputStream()) {
			FesodSheet.read(inputStream, headClass, new ReadListener<T>() {
				@Override
				public void invoke(T data, AnalysisContext context) {
					if (rows.size() >= maxRows) {
						throw new RRException("单次导入不能超过 " + maxRows + " 行");
					}
					rows.add(data);
				}

				@Override
				public void doAfterAllAnalysed(AnalysisContext context) {
					// 读取完成，无需额外处理
				}
			}).ignoreEmptyRow(false).sheet().doRead();
		} catch (RRException e) {
			throw e;
		} catch (Exception e) {
			log.error("Excel 解析失败", e);
			throw new RRException("Excel 文件解析失败，请检查文件格式");
		}

		boolean hasData = rows.stream().anyMatch(row -> !isBlankRow(row));
		if (!hasData) {
			throw new RRException("Excel 中没有可导入的数据，请检查表头是否与模板一致");
		}
		return rows;
	}

	/**
	 * 判断一行数据是否全部为空（含空白字符串）。
	 */
	public static boolean isBlankRow(Object row) {
		if (row == null) {
			return true;
		}
		for (Class<?> clazz = row.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
			for (Field field : clazz.getDeclaredFields()) {
				if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				try {
					field.setAccessible(true);
					Object value = field.get(row);
					if (value instanceof CharSequence text) {
						if (!text.toString().isBlank()) {
							return false;
						}
					} else if (value != null) {
						return false;
					}
				} catch (IllegalAccessException e) {
					return false;
				}
			}
		}
		return true;
	}
}
