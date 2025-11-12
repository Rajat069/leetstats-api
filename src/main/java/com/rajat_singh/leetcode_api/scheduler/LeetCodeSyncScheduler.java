package com.rajat_singh.leetcode_api.scheduler;

import com.rajat_singh.leetcode_api.client.LeetCodeClient;
import com.rajat_singh.leetcode_api.dto.ContestsDTO;
import com.rajat_singh.leetcode_api.dto.DailyCodingChallengeResponse;
import com.rajat_singh.leetcode_api.dto.QuestionListResponse;
import com.rajat_singh.leetcode_api.entity.ContestDataEntity;
import com.rajat_singh.leetcode_api.entity.QuestionEntity;
import com.rajat_singh.leetcode_api.entity.SponsorEntity;
import com.rajat_singh.leetcode_api.entity.TopicTag;
import com.rajat_singh.leetcode_api.mappers.ContestMapper;
import com.rajat_singh.leetcode_api.mappers.QuestionMapper;
import com.rajat_singh.leetcode_api.repository.GlobalLeetCodeContestsRepository;
import com.rajat_singh.leetcode_api.repository.QuestionsRepository;
import com.rajat_singh.leetcode_api.utility.DBUtilities;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tinylog.Logger;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.rajat_singh.leetcode_api.constants.Constants.*;

@Component
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class LeetCodeSyncScheduler {

    @Autowired
    private LeetCodeClient leetCodeApiClient;

    @Autowired
    private QuestionsRepository questionRepository;

    @Autowired
    private  QuestionMapper questionMapper;

    @Autowired
    private ContestMapper contestMapper;

    @Autowired
    private DBUtilities dbUtilities;

    @Autowired
    private GlobalLeetCodeContestsRepository globalLeetCodeContestsRepository;

    // Runs every week for full data sync
    @Async
    @Scheduled(fixedRate = WEEK_IN_MILLISECONDS)
    public void syncQuestionData() {
        Logger.info("Starting LeetCode [Full Data] sync... at {}", DateFormat.getDateInstance().format(System.currentTimeMillis()));
        long startTime = System.currentTimeMillis();
        QuestionListResponse response = leetCodeApiClient.fetchAllQuestions(false);

        for (QuestionListResponse.Question dto : response.getData().getProblemsetQuestionListV2().getQuestions()) {

            QuestionEntity existingQuestion = questionRepository.findByTitleSlug(dto.getTitleSlug());
            if(Objects.isNull(existingQuestion)){
                existingQuestion = new QuestionEntity();
            }

            existingQuestion.setId(dto.getId());
            existingQuestion.setTitle(dto.getTitle());
            existingQuestion.setTitleSlug(dto.getTitleSlug());
            existingQuestion.setDifficulty(dto.getDifficulty());
            existingQuestion.setIsPaidOnly(dto.getPaidOnly());
            existingQuestion.setAcRate(dto.getAcRate());
            existingQuestion.setProblemUrl(PROBLEM_URL_PREFIX + dto.getTitleSlug());
            if (dto.getTopicTags() != null) {
                List<TopicTag> newTags = dto.getTopicTags().stream()
                        .map(questionMapper::apiTagToEntityTag)
                        .toList();
                existingQuestion.getTopicTags().clear();
                existingQuestion.getTopicTags().addAll(newTags);

            } else {
                existingQuestion.getTopicTags().clear();
            }
            
            questionRepository.save(existingQuestion);
        }
        long endTime = System.currentTimeMillis();
        Logger.info("LeetCode [Full Data] sync completed in {} seconds.", (endTime - startTime) / 1000);
    }

    @Scheduled(fixedRate = DAY_IN_MILLISECONDS) // Runs every day for AC Rate sync
    @Async
    public void syncAcRateData() {
        Logger.info("Starting LeetCode [AC Rate] sync... at {}", DateFormat.getDateInstance().format(System.currentTimeMillis()));
        Long startTime = System.currentTimeMillis();
        QuestionListResponse response = leetCodeApiClient.fetchAllQuestions(true);

        for (QuestionListResponse.Question dto : response.getData().getProblemsetQuestionListV2().getQuestions()) {

            QuestionEntity existingQuestion = questionRepository.findByTitleSlug(dto.getTitleSlug());
            if(Objects.nonNull(existingQuestion)){
                existingQuestion.setAcRate(dto.getAcRate());
                questionRepository.save(existingQuestion);
            }
        }
        Long endTime = System.currentTimeMillis();
        Logger.info("LeetCode [AC Rate] sync completed in {} seconds.", (endTime - startTime) / 1000);
    }

    @Scheduled(cron = "0 31 5 * * ?", zone = "Asia/Kolkata") // Runs every day at 5:31 AM (IST) for POTD sync
    @Async
    public void syncPOTD() {
        Logger.info("Starting LeetCode [POTD] sync... at {}", DateFormat.getDateInstance().format(System.currentTimeMillis()));
        Long startTime = System.currentTimeMillis();
        DailyCodingChallengeResponse response = leetCodeApiClient.fetchDailyCodingChallengeQuestions();

        QuestionEntity existingQuestion = questionRepository.findByTitleSlug(response.getData().getActiveDailyCodingChallengeQuestion().getQuestion().getTitleSlug());
        if(Objects.nonNull(existingQuestion)){
                existingQuestion.setIsProblemOfTheDay(true);
                questionRepository.save(existingQuestion);
        }

        Long endTime = System.currentTimeMillis();
        Logger.info("LeetCode [POTD] sync completed in {} seconds.", (endTime - startTime) / 1000);
    }

    @Scheduled(cron = "0 30 5 * * ?", zone = "Asia/Kolkata") // Runs every day at 5:30 AM (IST) for POTD removal)
    @Async
    public void removePOTD() {
        Logger.info("Starting LeetCode [POTD] removal... at {}", DateFormat.getDateInstance().format(System.currentTimeMillis()));
        Long startTime = System.currentTimeMillis();
        QuestionEntity existingQuestion = questionRepository.findByIsProblemOfTheDayTrue();
        if(Objects.nonNull(existingQuestion)){
            existingQuestion.setIsProblemOfTheDay(false);
            questionRepository.save(existingQuestion);
        }
        Long endTime = System.currentTimeMillis();
        Logger.info("LeetCode [POTD] removal completed in {} seconds.", (endTime - startTime) / 1000);
    }

    @Scheduled(fixedRate = MONTH_IN_MILLISECONDS)
    @Async
    public void syncContestData() {
        Logger.info("Starting LeetCode [Contest] sync... at {}", DateFormat.getDateInstance().format(System.currentTimeMillis()));
        Long startTime = System.currentTimeMillis();
        List<ContestsDTO.ContestData> response = leetCodeApiClient.fetchAllPastContest();
        Logger.info("Fetched {} contests from LeetCode", response.size());
        for(ContestsDTO.ContestData contestsDTO:response){
            ContestDataEntity contestDataEntity = globalLeetCodeContestsRepository.findByTitleSlug(contestsDTO.getTitleSlug());
            if(Objects.isNull(contestDataEntity)){
                contestDataEntity = new ContestDataEntity();
            }
            contestDataEntity.setTitle(contestsDTO.getTitle());
            contestDataEntity.setTitleSlug(contestsDTO.getTitleSlug());
            contestDataEntity.setStartTime(contestsDTO.getStartTime());
            contestDataEntity.setOriginStartTime(contestsDTO.getOriginStartTime());
            contestDataEntity.setCardImg(contestsDTO.getCardImg());
            List<SponsorEntity> sponsorEntities = contestsDTO.getSponsors().stream().map(contestMapper::dtoToSponsorEntity).collect(Collectors.toList());
            contestDataEntity.setSponsors(sponsorEntities);

            globalLeetCodeContestsRepository.save(contestDataEntity);
        }
        Long endTime = System.currentTimeMillis();
        Logger.info("LeetCode [Contest] sync completed in {} seconds.", (endTime - startTime) / 1000);
    }

    @PostConstruct
    public void initialSync() {
        syncPOTD();
    }
}