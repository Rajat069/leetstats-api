package com.rajat_singh.leetcode_api.dto;

import lombok.Data;
import java.util.List;

@Data
public class ContestsDTO {

    private ResponseData data;

    @Data
    public static class ResponseData {
        private PastContests pastContests;
    }

    @Data
    public static class PastContests {
        private int pageNum;
        private int currentPage;
        private int totalNum;
        private int numPerPage;
        private List<ContestData> data;
    }

    @Data
    public static class ContestData {
        private String title;
        private String titleSlug;
        private long startTime;
        private long originStartTime;
        private String cardImg;
        private List<Sponsor> sponsors;
    }

    @Data
    public static class Sponsor {
        private String name;
        private String lightLogo;
        private String darkLogo;
    }
}
