package com.rajat_singh.leetcode_api.controller;

import com.rajat_singh.leetcode_api.dto.ContestDTO;
import com.rajat_singh.leetcode_api.dto.ContestsDTO;
import com.rajat_singh.leetcode_api.service.LeetCodeContestService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tinylog.Logger;


@RestController
@RequestMapping("/api/v1/globalContestInfo")
@RequiredArgsConstructor
public class GlobalContestInfoController {

    private final LeetCodeContestService leetCodeContestService;

    @GetMapping("/fetchContests")
    public ResponseEntity<Page<ContestDTO>> getContestsInfo(@ParameterObject Pageable pageable) {
        Logger.info("Fetching contests info");
        return ResponseEntity.ok(leetCodeContestService.getContestsInfo(pageable));
    }

}
