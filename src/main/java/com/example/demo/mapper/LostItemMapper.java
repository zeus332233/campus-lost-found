package com.example.demo.mapper;

import com.example.demo.model.LostItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface LostItemMapper {
    int insert(LostItem lostItem);
    List<LostItem> selectAll();
    List<LostItem> selectByUserId(Long userId);
    LostItem selectById(Long id);
    int updateById(LostItem lostItem);
    int deleteById(Long id);
    List<LostItem> selectByStatus(String status);
    List<LostItem> selectLatest(@Param("limit") int limit);
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /** 分页+条件搜索 */
    List<LostItem> search(Map<String, Object> params);
    int countSearch(Map<String, Object> params);

    /** 按类别和状态查询（匹配引擎用） */
    List<LostItem> selectByCategoryAndStatus(@Param("category") String category, @Param("status") String status);

    /** 统计 */
    int countAll();
    int countByStatus(@Param("status") String status);
}
