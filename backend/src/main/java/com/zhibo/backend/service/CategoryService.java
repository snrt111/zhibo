package com.zhibo.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhibo.backend.entity.Category;
import com.zhibo.backend.mapper.CategoryMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService extends ServiceImpl<CategoryMapper, Category> {

    /**
     * 获取所有分类列表
     */
    public List<Category> getCategoryList() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1); // 只获取状态正常的分类
        wrapper.orderByAsc(Category::getSort); // 按排序字段升序排列
        return list(wrapper);
    }

    /**
     * 根据ID获取分类
     */
    public Category getCategoryById(Long id) {
        return getById(id);
    }

    /**
     * 添加分类
     */
    public Category addCategory(Category category) {
        // 设置默认值
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        save(category);
        return category;
    }

    /**
     * 更新分类
     */
    public boolean updateCategory(Category category) {
        return updateById(category);
    }

    /**
     * 删除分类（软删除，设置状态为禁用）
     */
    public boolean deleteCategory(Long id) {
        Category category = getById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        category.setStatus(0); // 设置为禁用状态
        return updateById(category);
    }
}