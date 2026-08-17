package com.athena.common.cache;

import com.athena.common.utils.SpringContextUtils;
import com.athena.modules.sys.entity.SysDict;
import com.athena.modules.sys.entity.SysDictItem;
import com.athena.modules.sys.service.SysDictItemService;
import com.athena.modules.sys.service.SysDictService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DictCache implements ApplicationRunner {

    private static final String DICT_CACHE_PREFIX = "sys:cache:dict:";
    
    private static final Map<String, Map<String, String>> dictMapCache = new HashMap<>();
    private static final Map<String, String> dictIdToCodeMap = new HashMap<>();
    private static final Map<String, String> dictCodeToIdMap = new HashMap<>();

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始初始化字典缓存...");
        loadDictMapping();
        loadDictItems();
        log.info("字典缓存初始化完成，共加载 {} 个字典类型", dictMapCache.size());
    }

    private static void loadDictMapping() {
        SysDictService dictService = SpringContextUtils.getBean("sysDictServiceImpl", SysDictService.class);
        List<SysDict> dicts = dictService.list();
        for (SysDict dict : dicts) {
            dictIdToCodeMap.put(dict.getId(), dict.getDictCode());
            dictCodeToIdMap.put(dict.getDictCode(), dict.getId());
        }
    }

    private static void loadDictItems() {
        SysDictItemService dictItemService = SpringContextUtils.getBean("sysDictItemServiceImpl", SysDictItemService.class);
        RedisTemplate<String, Object> redisTemplate = SpringContextUtils.getBean("redisTemplate", RedisTemplate.class);
        
        List<SysDictItem> allDictItems = dictItemService.list();
        for (SysDictItem item : allDictItems) {
            String dictCode = dictIdToCodeMap.get(item.getDictId());
            if (dictCode != null) {
                String itemValue = item.getItemValue();
                String itemText = item.getItemText();
                
                String redisKey = DICT_CACHE_PREFIX + dictCode + ":" + itemValue;
                redisTemplate.opsForValue().set(redisKey, itemText);
                
                dictMapCache.computeIfAbsent(dictCode, k -> new HashMap<>())
                            .put(itemValue, itemText);
            }
        }
    }

    public static String getDictText(String dictCode, String value) {
        Map<String, String> dictValues = dictMapCache.get(dictCode);
        return dictValues != null ? dictValues.get(value) : null;
    }

    public static String getDictCodeById(String dictId) {
        return dictIdToCodeMap.get(dictId);
    }

    public static void refreshDict(String dictCode) {
        log.info("刷新字典缓存: {}", dictCode);
        SysDictItemService dictItemService = SpringContextUtils.getBean("sysDictItemServiceImpl", SysDictItemService.class);
        RedisTemplate<String, Object> redisTemplate = SpringContextUtils.getBean("redisTemplate", RedisTemplate.class);
        
        dictMapCache.remove(dictCode);
        
        String dictId = dictCodeToIdMap.get(dictCode);
        if (dictId != null) {
            List<SysDictItem> dictItems = dictItemService.list(new LambdaQueryWrapper<SysDictItem>()
                    .eq(SysDictItem::getDictId, dictId));
            
            Map<String, String> newDictValues = new HashMap<>();
            for (SysDictItem item : dictItems) {
                String redisKey = DICT_CACHE_PREFIX + dictCode + ":" + item.getItemValue();
                redisTemplate.opsForValue().set(redisKey, item.getItemText());
                newDictValues.put(item.getItemValue(), item.getItemText());
            }
            dictMapCache.put(dictCode, newDictValues);
        }
    }

    public static void refreshAll() {
        log.info("刷新所有字典缓存");
        dictMapCache.clear();
        dictIdToCodeMap.clear();
        dictCodeToIdMap.clear();
        
        loadDictMapping();
        loadDictItems();
        log.info("所有字典缓存刷新完成");
    }
}