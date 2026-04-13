package com.zhibo.backend.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.zhibo.backend.entity.Category;
import com.zhibo.backend.entity.Live;
import com.zhibo.backend.entity.LiveDocument;
import com.zhibo.backend.entity.User;
import com.zhibo.backend.repository.LiveSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

@Service
public class LiveSearchService {

    private static final Logger logger = LoggerFactory.getLogger(LiveSearchService.class);

    @Autowired
    private LiveSearchRepository liveSearchRepository;

    @Autowired
    private LiveService liveService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @PostConstruct
    public void init() {
        try {
            syncAllLivesToElasticsearch();
        } catch (Exception e) {
            logger.error("初始化同步直播数据到Elasticsearch失败", e);
        }
    }

    public void syncAllLivesToElasticsearch() {
        try {
            logger.info("开始同步所有直播数据到Elasticsearch");
            List<Live> lives = liveService.getLiveList(null);
            int syncCount = 0;
            for (Live live : lives) {
                try {
                    LiveDocument doc = convertToDocument(live);
                    liveSearchRepository.save(doc);
                    syncCount++;
                } catch (Exception e) {
                    logger.warn("同步直播失败: {}", live.getId(), e);
                }
            }
            logger.info("同步完成，共同步 {} 条直播数据", syncCount);
        } catch (Exception e) {
            logger.error("同步所有直播数据到Elasticsearch失败", e);
        }
    }

    public void syncLiveToElasticsearch(Long liveId) {
        try {
            Live live = liveService.getById(liveId);
            if (live == null) {
                logger.warn("直播不存在，无法同步到Elasticsearch: {}", liveId);
                return;
            }
            LiveDocument doc = convertToDocument(live);
            liveSearchRepository.save(doc);
            logger.info("同步直播到Elasticsearch成功: {}", liveId);
        } catch (Exception e) {
            logger.error("同步直播到Elasticsearch失败: {}", liveId, e);
        }
    }

    public void deleteLiveFromElasticsearch(Long liveId) {
        try {
            liveSearchRepository.deleteById(liveId);
            logger.info("从Elasticsearch删除直播成功: {}", liveId);
        } catch (Exception e) {
            logger.error("从Elasticsearch删除直播失败: {}", liveId, e);
        }
    }

    public Map<String, Object> searchLives(String keyword, Integer status, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "viewCount"));
            Page<LiveDocument> result;
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                if (status != null) {
                    result = liveSearchRepository.findByStatusAndTitleContaining(status, keyword.trim(), pageable);
                } else {
                    result = liveSearchRepository.findByTitleContaining(keyword.trim(), pageable);
                }
            } else {
                if (status != null) {
                    result = liveSearchRepository.findByStatus(status, pageable);
                } else {
                    result = liveSearchRepository.findAll(pageable);
                }
            }
            
            List<Map<String, Object>> list = new ArrayList<>();
            for (LiveDocument doc : result.getContent()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", doc.getId());
                item.put("userId", doc.getUserId());
                item.put("title", doc.getTitle());
                item.put("description", doc.getDescription());
                item.put("cover", doc.getCover());
                item.put("categoryId", doc.getCategoryId());
                item.put("categoryName", doc.getCategoryName());
                item.put("userNickname", doc.getUserNickname());
                item.put("status", doc.getStatus());
                item.put("viewCount", doc.getViewCount());
                item.put("startTime", doc.getStartTime());
                list.add(item);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("list", list);
            response.put("total", result.getTotalElements());
            response.put("totalPages", result.getTotalPages());
            response.put("currentPage", result.getNumber());
            response.put("size", result.getSize());
            
            return response;
        } catch (Exception e) {
            logger.error("搜索直播失败, keyword={}, status={}", keyword, status, e);
            return fallbackSearchFromDatabase(keyword, status, page, size);
        }
    }

    private Map<String, Object> fallbackSearchFromDatabase(String keyword, Integer status, int page, int size) {
        logger.info("使用数据库回退搜索");
        List<Live> lives = liveService.getLiveList(status);
        List<Map<String, Object>> list = new ArrayList<>();
        
        for (Live live : lives) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                if (live.getTitle() == null || !live.getTitle().contains(keyword.trim())) {
                    continue;
                }
            }
            Map<String, Object> item = new HashMap<>();
            item.put("id", live.getId());
            item.put("userId", live.getUserId());
            item.put("title", live.getTitle());
            item.put("description", live.getDescription());
            item.put("cover", live.getCover());
            item.put("categoryId", live.getCategoryId());
            item.put("status", live.getStatus());
            item.put("viewCount", live.getViewCount());
            list.add(item);
        }
        
        int start = Math.min(page * size, list.size());
        int end = Math.min(start + size, list.size());
        
        Map<String, Object> response = new HashMap<>();
        response.put("list", list.subList(start, end));
        response.put("total", list.size());
        response.put("totalPages", (list.size() + size - 1) / size);
        response.put("currentPage", page);
        response.put("size", size);
        
        return response;
    }

    private LiveDocument convertToDocument(Live live) {
        LiveDocument doc = new LiveDocument();
        doc.setId(live.getId());
        doc.setTitle(live.getTitle());
        doc.setDescription(live.getDescription());
        doc.setCover(live.getCover());
        doc.setUserId(live.getUserId());
        doc.setCategoryId(live.getCategoryId());
        doc.setStatus(live.getStatus());
        doc.setViewCount(live.getViewCount());
        
        User user = userService.getById(live.getUserId());
        if (user != null) {
            doc.setUserNickname(user.getNickname());
        }
        
        if (live.getCategoryId() != null) {
            Category category = categoryService.getById(live.getCategoryId());
            if (category != null) {
                doc.setCategoryName(category.getName());
            }
        }
        
        if (live.getStartTime() != null) {
            doc.setStartTime(live.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        if (live.getCreatedAt() != null) {
            doc.setCreatedAt(live.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        return doc;
    }
}
