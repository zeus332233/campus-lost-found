package com.example.demo.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class ClaimDTO {
    private Long itemId;
    private String itemType; // FOUND或LOST
    private String claimDescription;
}