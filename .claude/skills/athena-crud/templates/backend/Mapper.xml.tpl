<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<!-- 仅在需要自定义 SQL（联表、动态选项等）时生成此文件。 -->
<mapper namespace="com.athena.modules.{{module}}.mapper.{{Entity}}Mapper">

    <!-- 示例：下拉选项
    <select id="selectOptions" resultType="com.athena.common.base.select.BaseSelect">
        SELECT name AS label, id AS value
        FROM {{tableName}}
        <where>
            del_flag = 0
            <if test="keyword != null and keyword != ''">
                AND name LIKE CONCAT('%', #{keyword}, '%')
            </if>
        </where>
        ORDER BY name
        LIMIT 100
    </select>
    -->

</mapper>
