package com.example.demo.service;

import com.example.demo.dto.FoundItemDTO;
import com.example.demo.model.FoundItem;
import java.util.List;
import java.util.Map;

public interface FoundItemService {
    FoundItem getById(Long id);
    boolean save(FoundItem foundItem);
    FoundItem createFoundItem(FoundItemDTO foundItemDTO, Long userId);
    FoundItem updateFoundItem(Long id, FoundItemDTO dto, Long userId);
    void deleteFoundItem(Long id, Long userId);
    List<FoundItem> getAllFoundItems();
    FoundItem updateFoundItemStatus(Long id, String status);
    List<FoundItem> getFoundItemsByUserId(Long userId);
    List<FoundItem> getLatestFoundItems();

    /** 分页搜索 */
    Map<String, Object> searchWithPage(String keyword, String category, String location,
                                       String status, Long startDate, Long endDate, int page, int size);

    /** 统计 */
    int countAll();
    int countByStatus(String status);
}
