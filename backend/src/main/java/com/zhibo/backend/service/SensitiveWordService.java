package com.zhibo.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhibo.backend.entity.SensitiveWord;
import com.zhibo.backend.mapper.SensitiveWordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SensitiveWordService extends ServiceImpl<SensitiveWordMapper, SensitiveWord> {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveWordService.class);

    private Set<String> sensitiveWords = new HashSet<>();

    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;

    @PostConstruct
    public void init() {
        reloadSensitiveWords();
    }

    public void reloadSensitiveWords() {
        List<String> words = sensitiveWordMapper.selectAllEnabledWords();
        sensitiveWords.clear();
        sensitiveWords.addAll(words);
        logger.info("敏感词库已加载，共 {} 个敏感词", sensitiveWords.size());
    }

    public boolean containsSensitiveWord(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }

        String lowerContent = content.toLowerCase();
        for (String word : sensitiveWords) {
            if (lowerContent.contains(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public String filterSensitiveWords(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        String result = content;
        for (String word : sensitiveWords) {
            String replacement = "*".repeat(word.length());
            result = result.replaceAll("(?i)" + word, replacement);
        }
        return result;
    }

    public Set<String> findSensitiveWords(String content) {
        Set<String> foundWords = new HashSet<>();
        if (content == null || content.isEmpty()) {
            return foundWords;
        }

        String lowerContent = content.toLowerCase();
        for (String word : sensitiveWords) {
            if (lowerContent.contains(word.toLowerCase())) {
                foundWords.add(word);
            }
        }
        return foundWords;
    }

    public int getMaxRiskLevel(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }

        int maxLevel = 0;
        String lowerContent = content.toLowerCase();
        
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensitiveWord::getEnabled, 1);
        List<SensitiveWord> words = list(wrapper);
        
        for (SensitiveWord word : words) {
            if (lowerContent.contains(word.getWord().toLowerCase())) {
                if (word.getLevel() != null && word.getLevel() > maxLevel) {
                    maxLevel = word.getLevel();
                }
            }
        }
        return maxLevel;
    }

    public int getCategoryByMaxRisk(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }

        int maxLevel = 0;
        int category = 0;
        String lowerContent = content.toLowerCase();
        
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensitiveWord::getEnabled, 1);
        List<SensitiveWord> words = list(wrapper);
        
        for (SensitiveWord word : words) {
            if (lowerContent.contains(word.getWord().toLowerCase())) {
                if (word.getLevel() != null && word.getLevel() > maxLevel) {
                    maxLevel = word.getLevel();
                    category = word.getCategory() != null ? word.getCategory() : 0;
                }
            }
        }
        return category;
    }

    public boolean addSensitiveWord(String word, Integer category, Integer level) {
        if (word == null || word.isEmpty()) {
            throw new RuntimeException("敏感词不能为空");
        }

        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensitiveWord::getWord, word);
        if (count(wrapper) > 0) {
            throw new RuntimeException("敏感词已存在");
        }

        SensitiveWord sensitiveWord = new SensitiveWord();
        sensitiveWord.setWord(word);
        sensitiveWord.setCategory(category != null ? category : 0);
        sensitiveWord.setLevel(level != null ? level : 1);
        sensitiveWord.setEnabled(1);
        sensitiveWord.setCreatedAt(LocalDateTime.now());

        boolean result = save(sensitiveWord);
        if (result) {
            sensitiveWords.add(word);
            logger.info("添加敏感词：{}", word);
        }
        return result;
    }

    public boolean removeSensitiveWord(Long id) {
        SensitiveWord word = getById(id);
        if (word != null) {
            sensitiveWords.remove(word.getWord());
        }
        return removeById(id);
    }

    public boolean toggleSensitiveWord(Long id, Integer enabled) {
        SensitiveWord word = getById(id);
        if (word == null) {
            throw new RuntimeException("敏感词不存在");
        }

        word.setEnabled(enabled);
        boolean result = updateById(word);
        if (result) {
            if (enabled == 1) {
                sensitiveWords.add(word.getWord());
            } else {
                sensitiveWords.remove(word.getWord());
            }
        }
        return result;
    }

    public boolean updateSensitiveWord(Long id, String word, Integer category, Integer level) {
        SensitiveWord existingWord = getById(id);
        if (existingWord == null) {
            throw new RuntimeException("敏感词不存在");
        }

        String oldWord = existingWord.getWord();

        if (word != null && !word.equals(oldWord)) {
            LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SensitiveWord::getWord, word);
            if (count(wrapper) > 0) {
                throw new RuntimeException("敏感词已存在");
            }
            existingWord.setWord(word);
        }

        if (category != null) {
            existingWord.setCategory(category);
        }
        if (level != null) {
            existingWord.setLevel(level);
        }

        boolean result = updateById(existingWord);
        if (result && existingWord.getEnabled() == 1) {
            sensitiveWords.remove(oldWord);
            if (word != null && !word.equals(oldWord)) {
                sensitiveWords.add(word);
            } else {
                sensitiveWords.add(oldWord);
            }
            logger.info("更新敏感词：{} -> {}", oldWord, word != null ? word : oldWord);
        }
        return result;
    }

    public Page<SensitiveWord> getSensitiveWordList(int page, int size, Integer category, Integer enabled) {
        Page<SensitiveWord> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        if (category != null) {
            wrapper.eq(SensitiveWord::getCategory, category);
        }
        if (enabled != null) {
            wrapper.eq(SensitiveWord::getEnabled, enabled);
        }
        wrapper.orderByDesc(SensitiveWord::getCreatedAt);
        return page(pageObj, wrapper);
    }
}