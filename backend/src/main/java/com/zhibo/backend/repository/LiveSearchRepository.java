package com.zhibo.backend.repository;

import com.zhibo.backend.entity.LiveDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LiveSearchRepository extends ElasticsearchRepository<LiveDocument, Long> {
    Page<LiveDocument> findByTitleOrDescription(String title, String description, Pageable pageable);
    Page<LiveDocument> findByTitleContaining(String title, Pageable pageable);
    Page<LiveDocument> findByStatus(Integer status, Pageable pageable);
    Page<LiveDocument> findByStatusAndTitleContaining(Integer status, String title, Pageable pageable);
}
