package com.athena.modules.sys.controller;

import com.athena.common.excel.ExcelUtils;
import com.athena.common.exception.RRException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证 AbstractController 的两个 Excel 公共方法可以独立完成导出 → 导入往返。
 */
class AbstractControllerExcelTest {

	private final TestController controller = new TestController();

	@Test
	void shouldRoundTripExcel() {
		MockHttpServletResponse response = new MockHttpServletResponse();
		List<ExcelRow> rows = List.of(new ExcelRow("alice", 18), new ExcelRow("bob", 20));

		controller.export(response, rows);

		byte[] bytes = response.getContentAsByteArray();
		assertTrue(bytes.length > 0, "导出的 Excel 不应为空");
		assertEquals(
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
				response.getContentType());

		MockMultipartFile file = new MockMultipartFile(
				"file", "users.xlsx",
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes);

		List<ExcelRow> actual = controller.read(file);
		assertEquals(2, actual.size());
		assertEquals("alice", actual.get(0).getName());
		assertEquals(18, actual.get(0).getAge());
		assertEquals("bob", actual.get(1).getName());
		assertEquals(20, actual.get(1).getAge());
	}

	@Test
	void shouldRejectNonExcelFile() {
		MockMultipartFile file = new MockMultipartFile(
				"file", "users.txt", "text/plain", "not excel".getBytes(StandardCharsets.UTF_8));
		assertThrows(RRException.class, () -> controller.read(file));
	}

	@Test
	void shouldRejectTooManyRows() {
		MockHttpServletResponse response = new MockHttpServletResponse();
		controller.export(response, List.of(
				new ExcelRow("alice", 18), new ExcelRow("bob", 20), new ExcelRow("carol", 22)));

		MockMultipartFile file = new MockMultipartFile(
				"file", "users.xlsx",
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
				response.getContentAsByteArray());

		RRException exception = assertThrows(RRException.class,
				() -> ExcelUtils.read(file, ExcelRow.class, 2));
		assertTrue(exception.getMsg().contains("不能超过 2 行"), exception.getMsg());
	}

	private static class TestController extends AbstractController {
		void export(HttpServletResponse response, List<ExcelRow> rows) {
			exportExcel(response, "users.xlsx", "用户", ExcelRow.class, rows);
		}

		List<ExcelRow> read(MultipartFile file) {
			return importExcel(file, ExcelRow.class);
		}
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ExcelRow {
		@ExcelProperty("姓名")
		private String name;

		@ExcelProperty("年龄")
		private Integer age;
	}
}
