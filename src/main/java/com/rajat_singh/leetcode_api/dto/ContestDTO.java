package com.rajat_singh.leetcode_api.dto;

import lombok.Data;
import java.util.List;

@Data
public class ContestDTO {
    private Long id;
    private String title;
    private String titleSlug;
    private long startTime;
    private long originStartTime;
    private String cardImg;
    private List<ContestsDTO.Sponsor> sponsors;
}
