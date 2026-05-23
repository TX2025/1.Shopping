package com.shop.service.impl;

import com.shop.entity.Category;
import com.shop.exception.BusinessException;
import com.shop.repository.CategoryRepository;
import com.shop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<Map<String, Object>> tree() {
        List<Category> all = categoryRepository.findAllByOrderBySortOrder();
        Map<Long, Map<String, Object>> map = new LinkedHashMap<>();
        for (Category c : all) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", c.getId());
            node.put("name", c.getName());
            node.put("parentId", c.getParentId());
            node.put("sortOrder", c.getSortOrder());
            node.put("icon", c.getIcon());
            node.put("coverImage", c.getCoverImage());
            node.put("videos", c.getVideos());
            node.put("children", new ArrayList<>());
            map.put(c.getId(), node);
        }
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Category c : all) {
            if (c.getParentId() == null) {
                roots.add(map.get(c.getId()));
            } else if (map.containsKey(c.getParentId())) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> children = (List<Map<String, Object>>) map.get(c.getParentId()).get("children");
                children.add(map.get(c.getId()));
            }
        }
        return roots;
    }

    @Override
    public List<Category> all() {
        return categoryRepository.findAllByOrderBySortOrder();
    }

    @Override
    public Category create(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category update(Long id, Category category) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "分类不存在"));
        existing.setName(category.getName());
        existing.setParentId(category.getParentId());
        existing.setSortOrder(category.getSortOrder());
        existing.setIcon(category.getIcon());
        existing.setCoverImage(category.getCoverImage());
        existing.setVideos(category.getVideos());
        return categoryRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }
}
