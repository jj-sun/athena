package com.athena.modules.sys.controller;

import com.athena.common.annotation.Dict;
import com.athena.common.utils.PageUtils;
import com.athena.common.utils.Result;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/test/dict")
public class TestDictController {

    @Data
    public static class TestUser {
        private String id;
        private String username;

        @Dict(dicCode = "id", dicText = "dept_name", dictTable = "sys_dept")
        private String deptCode;

        @Dict(dicCode = "sex")
        private Integer sex;

        @Dict(dicCode = "del_flag")
        private Integer delFlag;
    }

    @GetMapping("/user")
    public Result<TestUser> testUser() {
        TestUser user = new TestUser();
        user.setId("1");
        user.setUsername("admin");
        user.setDeptCode("1485429843877969921");
        user.setSex(0);
        user.setDelFlag(0);
        return Result.ok(user);
    }

    @GetMapping("/list")
    public Result<List<TestUser>> testList() {
        List<TestUser> list = new ArrayList<>();
        TestUser user1 = new TestUser();
        user1.setId("1");
        user1.setUsername("admin");
        user1.setDeptCode("1485429843877969921");
        user1.setSex(0);
        user1.setDelFlag(0);
        list.add(user1);

        TestUser user2 = new TestUser();
        user2.setId("2");
        user2.setUsername("test");
        user2.setDeptCode("1485429843877969921");
        user2.setSex(1);
        user2.setDelFlag(0);
        list.add(user2);

        return Result.ok(list);
    }

    @GetMapping("/page")
    public Result<PageUtils<TestUser>> testPage() {
        List<TestUser> list = new ArrayList<>();
        TestUser user1 = new TestUser();
        user1.setId("1");
        user1.setUsername("admin");
        user1.setDeptCode("1485429843877969921");
        user1.setSex(0);
        user1.setDelFlag(0);
        list.add(user1);

        PageUtils<TestUser> page = new PageUtils<>(list, 2, 10, 1);
        return Result.ok(page);
    }
}