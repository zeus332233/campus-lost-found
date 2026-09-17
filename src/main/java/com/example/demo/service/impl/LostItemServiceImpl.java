package com.example.demo.service.impl;

import com.example.demo.dto.LostItemDTO;
import com.example.demo.mapper.LostItemMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.LostItem;
import com.example.demo.model.User;
import com.example.demo.service.LostItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LostItemServiceImpl implements LostItemService {

    private final LostItemMapper lostItemMapper;
    private final UserMapper userMapper;

    @Override
    public LostItem createLostItem(LostItemDTO lostItemDTO, Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        LostItem lostItem = new LostItem();
        lostItem.setTitle(lostItemDTO.getTitle());
        lostItem.setDescription(lostItemDTO.getDescription());
        lostItem.setCategory(lostItemDTO.getCategory());
        lostItem.setLocation(lostItemDTO.getLocation());
        lostItem.setLostTime(lostItemDTO.getLostTime());
        lostItem.setContactPhone(lostItemDTO.getContactPhone());
        lostItem.setContactEmail(lostItemDTO.getContactEmail());
        lostItem.setStatus("SEARCHING");
        lostItem.setUserId(userId);
        lostItem.setImageUrls(lostItemDTO.getImageUrls());
        long currentTime = System.currentTimeMillis();
        lostItem.setCreatedAt(currentTime);
        lostItem.setUpdatedAt(currentTime);
        lostItemMapper.insert(lostItem);
        log.info("用户 {} 发布了寻物启事: {}", userId, lostItem.getTitle());
        return lostItem;
    }

    @Override
    public LostItem updateLostItem(Long id, LostItemDTO dto, Long userId) {
        LostItem existing = lostItemMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("寻物启事信息不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new RuntimeException("只能编辑自己发布的寻物启事信息");
        }

        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setCategory(dto.getCategory());
        existing.setLocation(dto.getLocation());
        existing.setLostTime(dto.getLostTime());
        existing.setContactPhone(dto.getContactPhone());
        existing.setContactEmail(dto.getContactEmail());
        existing.setImageUrls(dto.getImageUrls());
        existing.setUpdatedAt(System.currentTimeMillis());

        lostItemMapper.updateById(existing);
        log.info("用户 {} 编辑了寻物启事 ID: {}", userId, id);
        return existing;
    }

    @Override
    public void deleteLostItem(Long id, Long userId) {
        LostItem existing = lostItemMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("寻物启事信息不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new RuntimeException("只能删除自己发布的寻物启事信息");
        }
        lostItemMapper.deleteById(id);
        log.info("用户 {} 删除了寻物启事 ID: {}", userId, id);
    }

    @Override
    public List<LostItem> getAllLostItems() {
        List<LostItem> items = lostItemMapper.selectAll();
        return items != null ? items : new ArrayList<>();
    }

    @Override
    public LostItem getLostItemById(Long id) {
        return lostItemMapper.selectById(id);
    }

    @Override
    public List<LostItem> getLostItemsByUserId(Long userId) {
        return lostItemMapper.selectByUserId(userId);
    }

    @Override
    public boolean updateStatus(Long id, String status) {
        return lostItemMapper.updateStatus(id, status) > 0;
    }

    @Override
    public LostItem getById(Long id) {
        return lostItemMapper.selectById(id);
    }

    @Override
    public boolean save(LostItem lostItem) {
        if (lostItem.getId() == null) {
            return lostItemMapper.insert(lostItem) > 0;
        } else {
            return lostItemMapper.updateById(lostItem) > 0;
        }
    }

    @Override
    public List<LostItem> getLatestLostItems() {
        List<LostItem> items = lostItemMapper.selectLatest(5);
        return items != null ? items : new ArrayList<>();
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

        List<LostItem> items = lostItemMapper.search(params);
        int total = lostItemMapper.countSearch(params);

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
        return lostItemMapper.countAll();
    }

    @Override
    public int countByStatus(String status) {
        return lostItemMapper.countByStatus(status);
    }
}
