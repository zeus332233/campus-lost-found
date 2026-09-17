package com.example.demo.service;

import com.example.demo.dto.LostItemDTO;
import com.example.demo.model.LostItem;
import java.util.List;
import java.util.Map;

public interface LostItemService {
    LostItem createLostItem(LostItemDTO lostItemDTO, Long userId);
    LostItem updateLostItem(Long id, LostItemDTO dto, Long userId);
    void deleteLostItem(Long id, Long userId);
    List<LostItem> getAllLostItems();
    LostItem getLostItemById(Long id);
    List<LostItem> getLatestLostItems();
    List<LostItem> getLostItemsByUserId(Long userId);
    boolean updateStatus(Long id, String status);
    LostItem getById(Long id);
    boolean save(LostItem lostItem);

    /** 分页搜索 */
    Map<String, Object> searchWithPage(String keyword, String category, String location,
                                       String status, Long startDate, Long endDate, int page, int size);

    /** 统计 */
    int countAll();
    int countByStatus(String status);
}
