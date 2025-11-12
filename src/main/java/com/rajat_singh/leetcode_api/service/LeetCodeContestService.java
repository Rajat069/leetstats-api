package com.rajat_singh.leetcode_api.service;


import com.rajat_singh.leetcode_api.client.LeetCodeClient;
import com.rajat_singh.leetcode_api.dto.*;
import com.rajat_singh.leetcode_api.entity.ContestDataEntity;
import com.rajat_singh.leetcode_api.entity.UserContestHistoryEntity;
import com.rajat_singh.leetcode_api.enums.ContestFilterType;
import com.rajat_singh.leetcode_api.enums.UserContestType;
import com.rajat_singh.leetcode_api.exceptions.InvalidContestTime;
import com.rajat_singh.leetcode_api.exceptions.InvalidTrendDirection;
import com.rajat_singh.leetcode_api.exceptions.NoMatchingContest;
import com.rajat_singh.leetcode_api.exceptions.UserNotFoundException;
import com.rajat_singh.leetcode_api.mappers.ContestMapper;
import com.rajat_singh.leetcode_api.repository.ContestHistoryRepository;
import com.rajat_singh.leetcode_api.repository.GlobalLeetCodeContestsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.tinylog.Logger;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


@RequiredArgsConstructor
@Service
public class LeetCodeContestService {

    private final LeetCodeClient leetCodeClient;
    private final ContestHistoryRepository userContestHistoryRepository;
    private final GlobalLeetCodeContestsRepository globalLeetCodeContestsRepository;
    private final ContestMapper contestMapper;

    @Cacheable(value = "contestRankingCache", key = "#username")
    public Optional<UserContestRanking> getUserContestRanking(String username){
        Logger.info("Fetching contest ranking for user: {}", username);

        var response = leetCodeClient.fetchUserContestRanking(username, UserContestType.EXCLUDE_CONTEST_HISTORY);

        if(Objects.nonNull(response) && Objects.nonNull(response.getData()) && Objects.nonNull(response.getData().getUserContestRanking())){
            Logger.info("Contest ranking found for user: {}", username);
            Logger.debug("Contest ranking details: {}", response.getData().getUserContestRanking());
            return Optional.of(response.getData().getUserContestRanking());
        }

        Logger.warn("No contest ranking found for user: {}", username);
        throw new UserNotFoundException(username);
    }

    @Cacheable(value = "contestRankingWithHistoryCache", key = "#username")
    public Optional<UserContestResponse.DataNode> getUserContestRankingWithHistory(String username){
        Logger.info("Fetching contest ranking with history for user: {}", username);

        var response = leetCodeClient.fetchUserContestRanking(username,UserContestType.INCLUDE_CONTEST_HISTORY);

        if(Objects.nonNull(response) && Objects.nonNull(response.getData())){
            Logger.info("Contest ranking with history found for user: {}", username);
            Logger.debug("Contest ranking with history details: {}", response.getData());
            return Optional.of(response.getData());
        }

        Logger.warn("No contest ranking with history found for user: {}", username);
        throw new UserNotFoundException(username);
    }

    @Cacheable(value = "contestHistoryCache", key = "#username")
    public Optional<List<UserContestRankingHistory>> getUserContestRankingHistory(String username) {
        Logger.info("Fetching contest ranking history for user: {}", username);

        // Try from DB
        List<UserContestHistoryEntity> dbData = userContestHistoryRepository.findByUsername(username);
        if (!dbData.isEmpty()) {
            Logger.info("Loaded contest history for {} from DB", username);
            return Optional.of(convertToDto(dbData));
        }

        // Fetch from API
        Optional<List<UserContestRankingHistory>> res =  fetchContestHistoryFromLeetCode(username);
        if(res.isPresent()){
            Logger.info("Loaded contest history for {} from LeetCode API", username);
            return res;
        }else{
            Logger.warn("No contest history found for user: {}", username);
        }
        throw new UserNotFoundException(username);
    }

    private List<UserContestRankingHistory> convertToDto(List<UserContestHistoryEntity> entities) {
        return entities.stream().map(e -> {
            UserContestRankingHistory dto = new UserContestRankingHistory();
            dto.setAttended(e.isAttended());
            dto.setTrendDirection(e.getTrendDirection());
            dto.setProblemsSolved(e.getProblemsSolved());
            dto.setTotalProblems(e.getTotalProblems());
            dto.setFinishTimeInSeconds((int)e.getFinishTimeInSeconds());
            dto.setRating(e.getRating());
            dto.setRanking(e.getRanking());

            var contest = new UserContestRankingHistory.Contest();
            contest.setTitle(e.getTitle());
            contest.setStartTime(e.getStartTime());
            dto.setContest(contest);

            return dto;
        }).toList();
    }

    public Optional<List<UserContestRankingHistory>> getUsersContestInfo(String username, String value, ContestFilterType filterType) {
        Logger.info("Fetching contest info for user: {} with filter: {} and value: {}", username, filterType, value);
        List<UserContestHistoryEntity> entity = null;
        if(!userContestHistoryRepository.existsByUsername(username)){
            Logger.warn("No contest history found for user: {}", username);
            fetchContestHistoryFromLeetCode(username);
        }

        switch (filterType){
            case IS_ATTENDED:
                entity = userContestHistoryRepository.findByUsernameAndAttended(username,Boolean.parseBoolean(value));
                break;
            case TREND_DIRECTION:
                if(!value.equals("UP") && !value.equals("DOWN") && !value.equals("NONE")){
                    Logger.warn("Invalid trend direction value: {}", value);
                    throw new InvalidTrendDirection("Invalid Trend Direction: " + value + ". Allowed values are UP, DOWN, NONE.");
                }
                entity = userContestHistoryRepository.findByUsernameAndTrendDirection(username,value);
                break;
            case PROBLEMS_SOLVED_GTE:
                entity = userContestHistoryRepository.findByUsernameAndProblemsSolvedGreaterThanEqual(username,Integer.parseInt(value));
                if(entity.isEmpty()){
                    Logger.info("No contests found for user: {} with problems solved >= {}", username, value);
                    throw  new NoMatchingContest("No contests found with problems solved >= " + value + " for user: " + username);
                }
                break;
            case PROBLEMS_SOLVED_LTE: //TODO: PROBLEMS_SOLVED_EQ(1/2/3/4)
                entity = userContestHistoryRepository.findByUsernameAndProblemsSolvedLessThanEqual(username,Integer.parseInt(value));
                break;
            case FINISH_TIME:
                entity = userContestHistoryRepository.findByUsernameAndFinishTimeInSecondsLessThanEqualAndAttendedTrue(username,convertToSeconds(value));
                break;
            case FILTER_BY_RATING:
                entity = userContestHistoryRepository.findByUsernameAndRatingEquals(username,Integer.parseInt(value));
                break;
            case MATCH_BY_TITLE:
                UserContestHistoryEntity res = userContestHistoryRepository.findByUsernameAndTitleContainingIgnoreCase(username,value);
                if(Objects.nonNull(res)){
                    entity = List.of(res);
                } else entity = List.of();
                break;
            default:
                Logger.warn("Invalid filter type: {}", filterType);
        }
        if (entity != null) {
            List<UserContestRankingHistory> dtoList = entity.stream().map(e -> {
                UserContestRankingHistory dto = new UserContestRankingHistory();
                dto.setAttended(e.isAttended());
                dto.setTrendDirection(e.getTrendDirection());
                dto.setProblemsSolved(e.getProblemsSolved());
                dto.setTotalProblems(e.getTotalProblems());
                dto.setFinishTimeInSeconds((int)e.getFinishTimeInSeconds());
                dto.setRating(e.getRating());
                dto.setRanking(e.getRanking());

                UserContestRankingHistory.Contest contest = new UserContestRankingHistory.Contest();
                contest.setTitle(e.getTitle());
                contest.setStartTime(e.getStartTime());
                dto.setContest(contest);
                return dto;
            }).toList();
            return Optional.of(dtoList);
        }
        return Optional.empty();
    }

    private Long convertToSeconds(String timeStr) {
        timeStr = timeStr.toLowerCase().trim();
        long totalSeconds = 0;

        String[] parts = timeStr.split(" ");
        Logger.info("Converting time string: {}", timeStr);
        for (String part : parts) {
            if (part.endsWith("hrs")) {
                totalSeconds += Long.parseLong(part.replace("hrs", "")) * 3600;
            } else if (part.endsWith("mins")) {
                totalSeconds += Long.parseLong(part.replace("mins", "")) * 60;
            } else if (part.endsWith("s")) {
                totalSeconds += Long.parseLong(part.replace("s", ""));
            } else {
                throw new InvalidContestTime("Invalid time format: " + timeStr + ". Please use formats like '30s', '5mins', or '1hrs'.");
            }
        }
        Logger.info("Converted time string: {} to seconds: {}", timeStr, totalSeconds);
        return totalSeconds;
    }

    public Optional<UserContestBiggestRatingJump> getBiggestRatingJump(String username) {
        if(!userContestHistoryRepository.existsByUsername(username)){
            Logger.warn("No contest history found for user: {}", username);
            fetchContestHistoryFromLeetCode(username);
        }
        Map<String, Object> result = userContestHistoryRepository.findBiggestRatingJump(username);
        if (result == null) return Optional.empty();

        UserContestRankingHistory rankingHistory = new UserContestRankingHistory();
        UserContestRankingHistory.Contest contest = new UserContestRankingHistory.Contest();

        // Map contest details
        contest.setTitle((String) result.get("title"));
        contest.setStartTime(((Long) result.get("start_time")));
        rankingHistory.setContest(contest);

        // Map history details
        rankingHistory.setAttended((Boolean) result.get("attended"));
        rankingHistory.setTrendDirection((String) result.get("trend_direction"));
        rankingHistory.setProblemsSolved(((Number) result.get("problems_solved")).intValue());
        rankingHistory.setTotalProblems(((Number) result.get("total_problems")).intValue());
        rankingHistory.setFinishTimeInSeconds(((Number) result.get("finish_time_in_seconds")).intValue());
        rankingHistory.setRating(((Number) result.get("rating")).intValue());
        rankingHistory.setRanking(((Number) result.get("ranking")).intValue());

        // Create final DTO
        UserContestBiggestRatingJump jump = new UserContestBiggestRatingJump();
        jump.setRatingJump(((Number) result.get("rating_jump")).doubleValue());
        jump.setNewRating((float) result.get("new_rating"));
        jump.setPreviousRating((float) result.get("previous_rating"));
        jump.setUserContestRankingHistory(rankingHistory);

        return Optional.of(jump);
    }

    public Optional<List<UserContestRankingHistory>> fetchContestHistoryFromLeetCode(String username){

        Logger.info("Fetching contest history from LeetCode API for user: {}", username);
        var response = leetCodeClient.fetchUserContestRanking(username, UserContestType.ONLY_CONTEST_HISTORY);
        if (Objects.nonNull(response) && Objects.nonNull(response.getData().getUserContestRankingHistory())) {
            Logger.info("Contest history found from LeetCode API for user: {}", username);
            List<UserContestRankingHistory> apiData = response.getData().getUserContestRankingHistory();
            Logger.info("Saving contest history for {} to DB", username);

            List<UserContestHistoryEntity> entities = apiData.stream().map(h -> {
                UserContestHistoryEntity e = new UserContestHistoryEntity();
                e.setUsername(username);
                e.setTitle(h.getContest().getTitle());
                e.setAttended(h.isAttended());
                e.setTrendDirection(h.getTrendDirection());
                e.setProblemsSolved(h.getProblemsSolved());
                e.setTotalProblems(h.getTotalProblems());
                e.setFinishTimeInSeconds(h.getFinishTimeInSeconds());
                e.setRating(h.getRating());
                e.setRanking(h.getRanking());
                e.setStartTime(h.getContest().getStartTime());
                e.setLastUpdated(LocalDateTime.now());
                return e;
            }).toList();

            Logger.info("Saving {} contest history records for user: {} to DB", entities.size(), username);
            userContestHistoryRepository.saveAll(entities);

            return Optional.of(apiData);
        }
        return Optional.empty();
    }

    public void evictUserContestData(String username) {
        userContestHistoryRepository.deleteByUsername(username);
        Logger.info("Evicted contest history data for user: {}", username);
    }

    public Optional<UserContestRankingHistory> getBestContestRanking(String username) {
        if(!userContestHistoryRepository.existsByUsername(username)){
            Logger.warn("No contest history found for user: {}", username);
            fetchContestHistoryFromLeetCode(username);
        }
        UserContestHistoryEntity entity = userContestHistoryRepository.findTopByUsernameAndRankingGreaterThanOrderByRankingAsc(username,0);
        if (entity == null) {
            Logger.warn("No contest history found for user: {}", username);
            throw new UserNotFoundException(username);
        }

        UserContestRankingHistory dto = new UserContestRankingHistory();
        dto.setAttended(entity.isAttended());
        dto.setTrendDirection(entity.getTrendDirection());
        dto.setProblemsSolved(entity.getProblemsSolved());
        dto.setTotalProblems(entity.getTotalProblems());
        dto.setFinishTimeInSeconds((int)entity.getFinishTimeInSeconds());
        dto.setRating(entity.getRating());
        dto.setRanking(entity.getRanking());

        UserContestRankingHistory.Contest contest = new UserContestRankingHistory.Contest();
        contest.setTitle(entity.getTitle());
        contest.setStartTime(entity.getStartTime());
        dto.setContest(contest);

        return Optional.of(dto);
    }

    public Page<ContestDTO> getContestsInfo(Pageable page){

        Page<ContestDataEntity> entityPage = globalLeetCodeContestsRepository.findAll(page);

        if(entityPage.isEmpty()){
            Logger.warn("No contests found");
            return Page.empty();
        }
        return entityPage.map(contestMapper::entityToDTO);
    }

}
