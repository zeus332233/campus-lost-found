package com.example.demo.mapper;

import com.example.demo.model.Claim;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClaimMapper {
    int insert(Claim claim);
    
    Claim selectById(Long id);
    
    int updateById(Claim claim);
    
    int updateStatus(Long id, String status);
    
    List<Claim> selectAll();
}