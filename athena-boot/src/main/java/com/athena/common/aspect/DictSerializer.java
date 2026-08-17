package com.athena.common.aspect;

import com.athena.common.annotation.Dict;
import com.athena.common.cache.DictCache;
import com.athena.common.utils.ConvertUtils;
import com.athena.common.utils.SpringContextUtils;
import com.athena.common.vo.DictModel;
import com.athena.modules.sys.service.SysDictService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

@Slf4j
public class DictSerializer extends ValueSerializer<Object> {

    private final Dict dict;
    private final String fieldName;
    private ValueSerializer<Object> defaultSerializer;
    private final JavaType valueType;

    public DictSerializer(Dict dict, String fieldName, ValueSerializer<Object> defaultSerializer, JavaType valueType) {
        this.dict = dict;
        this.fieldName = fieldName;
        this.defaultSerializer = defaultSerializer;
        this.valueType = valueType;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializationContext provider) {
        ValueSerializer<Object> ser = defaultSerializer;
        if (ser == null) {
            try {
                if (value != null) {
                    ser = provider.findValueSerializer(value.getClass());
                } else if (valueType != null) {
                    ser = provider.findValueSerializer(valueType);
                }
                if (ser != null) {
                    defaultSerializer = ser;
                }
            } catch (Exception e) {
                log.warn("获取默认序列化器失败，字段：{}", fieldName, e);
            }
        }

        if (ser != null) {
            ser.serialize(value, gen, provider);
        } else if (value == null) {
            gen.writeNull();
        } else if (value instanceof String) {
            gen.writeString((String) value);
        } else if (value instanceof Integer) {
            gen.writeNumber((Integer) value);
        } else if (value instanceof Long) {
            gen.writeNumber((Long) value);
        } else if (value instanceof Boolean) {
            gen.writeBoolean((Boolean) value);
        } else {
            gen.writeString(value.toString());
        }

        if (value != null) {
            try {
                String dictText = getDictText(dict, value.toString());
                if (dictText != null) {
                    gen.writeName(fieldName + "_dictText");
                    gen.writeString(dictText);
                }
            } catch (Exception e) {
                log.warn("获取字典文本失败，字段：{}，值：{}", fieldName, value, e);
            }
        }
    }

    @Override
    public Class<Object> handledType() {
        return Object.class;
    }

    private String getDictText(Dict dict, String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        if (StringUtils.isNotBlank(dict.dictTable())) {
            return getDictTextFromTable(dict.dictTable(), dict.dicText(), dict.dicCode(), value);
        } else {
            return DictCache.getDictText(dict.dicCode(), value);
        }
    }

    private String getDictTextFromTable(String dictTable, String dicText, String dicCode, String value) {
        String dictCode = String.format("%s,%s,%s", dictTable, dicText, dicCode);
        String keyString = String.format("sys:cache:dictTable::SimpleKey [%s,%s]", dictCode, value);

        try {
            RedisTemplate<String, Object> redisTemplate = SpringContextUtils.getBean("redisTemplate", RedisTemplate.class);
            if (Boolean.TRUE.equals(redisTemplate.hasKey(keyString))) {
                return ConvertUtils.getString(redisTemplate.opsForValue().get(keyString));
            }

            SysDictService dictService = SpringContextUtils.getBean("sysDictServiceImpl", SysDictService.class);
            DictModel dictModel = dictService.queryTableDictTextByKey(dictTable, dicText, dicCode, value);

            if (dictModel != null) {
                try {
                    String redisKey = String.format("sys:cache:dictTable::SimpleKey [%s,%s]", dictCode, dictModel.getValue());
                    redisTemplate.opsForValue().set(redisKey, dictModel.getText());
                } catch (Exception e) {
                    log.warn("缓存字典失败", e);
                }
                return dictModel.getText();
            }
        } catch (Exception e) {
            log.warn("获取字典文本失败", e);
        }
        return null;
    }
}