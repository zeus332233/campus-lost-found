package com.example.demo.mapper;

import com.example.demo.model.FoundItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface FoundItemMapper {
    int insert(FoundItem foundItem);
    FoundItem selectById(Long id);
    List<FoundItem> selectByUserId(Long userId);
    List<FoundItem> selectByStatus(String status);
    int updateById(FoundItem foundItem);
    int deleteById(Long id);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    List<FoundItem> selectAll();
    List<FoundItem> selectLatest(@Param("limit") Integer limit);

    /** 分页+条件搜索 */
    List<FoundItem> search(Map<String, Object> params);
    int countSearch(Map<String, Object> params);

    /** 按类别和状态查询（匹配引擎用） */
    List<FoundItem> selectByCategoryAndStatus(@Param("category") String category, @Param("status") String status);

    /** 统计 */
    int countAll();
    int countByStatus(@Param("status") String status);
}
