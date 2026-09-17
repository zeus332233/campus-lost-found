package com.example.demo.service.impl;

import com.example.demo.dto.FoundItemDTO;
import com.example.demo.mapper.FoundItemMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.FoundItem;
import com.example.demo.model.User;
import com.example.demo.service.FoundItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoundItemServiceImpl implements FoundItemService {

    private final FoundItemMapper foundItemMapper;
    private final UserMapper userMapper;

    @Override
    public FoundItem createFoundItem(FoundItemDTO foundItemDTO, Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (foundItemDTO.getTitle() == null || foundItemDTO.getTitle().trim().isEmpty()) {
            throw new RuntimeException("标题不能为空");
        }
        if (foundItemDTO.getFoundTime() == null) {
            throw new RuntimeException("发现时间不能为空");
        }

        FoundItem foundItem = new FoundItem();
        foundItem.setTitle(foundItemDTO.getTitle());
        foundItem.setDescription(foundItemDTO.getDescription());
        foundItem.setCategory(foundItemDTO.getCategory());
        foundItem.setLocation(foundItemDTO.getLocation());
        foundItem.setFoundTime(foundItemDTO.getFoundTime());
        foundItem.setContactPhone(foundItemDTO.getContactPhone());
        foundItem.setContactEmail(foundItemDTO.getContactEmail());
        foundItem.setStatus("SEARCHING");
        foundItem.setUserId(userId);
        foundItem.setImageUrls(foundItemDTO.getImageUrls());
        long currentTime = System.currentTimeMillis();
        foundItem.setCreatedAt(currentTime);
        foundItem.setUpdatedAt(currentTime);

        foundItemMapper.insert(foundItem);
        log.info("用户 {} 发布了失物招领: {}", userId, foundItem.getTitle());
        return foundItem;
    }

    @Override
    public FoundItem updateFoundItem(Long id, FoundItemDTO dto, Long userId) {
        FoundItem existing = foundItemMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("失物招领信息不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new RuntimeException("只能编辑自己发布的失物招领信息");
        }

        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setCategory(dto.getCategory());
        existing.setLocation(dto.getLocation());
        existing.setFoundTime(dto.getFoundTime());
        existing.setContactPhone(dto.getContactPhone());
        existing.setContactEmail(dto.getContactEmail());
        existing.setImageUrls(dto.getImageUrls());
        existing.setUpdatedAt(System.currentTimeMillis());

        foundItemMapper.updateById(existing);
        log.info("用户 {} 编辑了失物招领 ID: {}", userId, id);
        return existing;
    }

    @Override
    public void deleteFoundItem(Long id, Long userId) {
        FoundItem existing = foundItemMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("失物招领信息不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new RuntimeException("只能删除自己发布的失物招领信息");
        }
        foundItemMapper.deleteById(id);
        log.info("用户 {} 删除了失物招领 ID: {}", userId, id);
    }

    @Override
    public List<FoundItem> getAllFoundItems() {
        List<FoundItem> items = foundItemMapper.selectAll();
        return items != null ? items : new ArrayList<>();
    }

    @Override
    public List<FoundItem> getLatestFoundItems() {
        List<FoundItem> items = foundItemMapper.selectLatest(5);
        return items != null ? items : new ArrayList<>();
    }

    @Override
    public FoundItem getById(Long id) {
        return foundItemMapper.selectById(id);
    }

    @Override
    public List<FoundItem> getFoundItemsByUserId(Long userId) {
        return foundItemMapper.selectByUserId(userId);
    }

    @Override
    public FoundItem updateFoundItemStatus(Long id, String status) {
        foundItemMapper.updateStatus(id, status);
        return getById(id);
    }

    @Override
    public boolean save(FoundItem foundItem) {
        if (foundItem.getId() == null) {
            return foundItemMapper.insert(foundItem) > 0;
        } else {
            return foundItemMapper.updateById(foundItem) > 0;
        }
    }

    @Override
    public Map<String, Object> searchWithPage(String keyword, String category, String location,
                                              String status, Long startDate, Long endDate, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("category", category);
        params.put("location", location);
        params.put("status", status);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("offset", (page - 1) * size);
        params.put("size", size);

        List<FoundItem> items = foundItemMapper.search(params);
        int total = foundItemMapper.countSearch(params);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items != null ? items : new ArrayList<>());
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }

    @Override
    public int countAll() {
        return foundItemMapper.countAll();
    }

    @Override
    public int countByStatus(String status) {
        return foundItemMapper.countByStatus(status);
    }
}
