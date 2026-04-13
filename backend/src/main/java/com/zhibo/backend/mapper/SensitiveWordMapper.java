package com.zhibo.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhibo.backend.entity.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWord> {

    @Select("SELECT word FROM sensitive_word WHERE enabled = 1")
    List<String> selectAllEnabledWords();
}