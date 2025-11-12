package com.rajat_singh.leetcode_api.controller;


import com.rajat_singh.leetcode_api.dto.QuestionResponseDTO;
import com.rajat_singh.leetcode_api.dto.QuestionSearchRequest;
import com.rajat_singh.leetcode_api.entity.QuestionEntity;
import com.rajat_singh.leetcode_api.mappers.QuestionMapper;
import com.rajat_singh.leetcode_api.repository.QuestionsRepository;
import com.rajat_singh.leetcode_api.service.LeetCodeQuestionsService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class LeetCodeQuestionsController {

    private final LeetCodeQuestionsService leetCodeQuestionsService;
    /**
    * TODO: Migrate questionsRepo's logic inside leetCodeQuestionsService for clean controller
     */
    private final QuestionsRepository questionsRepository;
    private final QuestionMapper questionMapper;

    /**
     * Standard paginated endpoint to get questions from YOUR database.
     * e.g., /api/v1/questions?page=0&size=20
     */
    @GetMapping()
    public ResponseEntity<Page<QuestionResponseDTO>> getAllQuestions(@ParameterObject Pageable pageable){
        Logger.info("Fetching questions from database with pagination: {}", pageable);

        Page<QuestionEntity> entityPage = questionsRepository.findAll(pageable);

        Page<QuestionResponseDTO> dtoPage = entityPage.map(questionMapper::entityToResponseDTO);

        return ResponseEntity.ok(dtoPage);
    }


    /**
     * A powerful, filterable search endpoint.
     */
    @PostMapping("/search")
    public ResponseEntity<Page<QuestionResponseDTO>>searchQuestions(@RequestBody QuestionSearchRequest request) {
        Logger.info("searchQuestions() method called with request :: {}",request);
        Page<QuestionResponseDTO> dtoPage = leetCodeQuestionsService.findQuestions(request);
        return ResponseEntity.ok(dtoPage);
    }

    /**
    * Retrieves the current Problem of the Day (POTD) from the database
    */
    @GetMapping("/potd")
    public ResponseEntity<QuestionResponseDTO> getPotd() {
        Logger.info("Fetching POTD from database");
        QuestionEntity potd = questionsRepository.findByIsProblemOfTheDayTrue();
        if (potd == null) {
            Logger.warn("No problem of the day found");
            return ResponseEntity.notFound().build();
        }
        QuestionResponseDTO dto = questionMapper.entityToResponseDTO(potd);
        return ResponseEntity.ok(dto);
    }

}
