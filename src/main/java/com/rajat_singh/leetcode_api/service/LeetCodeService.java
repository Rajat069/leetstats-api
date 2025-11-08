package com.rajat_singh.leetcode_api.service;

import java.time.Year;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rajat_singh.leetcode_api.dto.*;
import com.rajat_singh.leetcode_api.client.LeetCodeClient;
import com.rajat_singh.leetcode_api.enums.UserContestType;
import com.rajat_singh.leetcode_api.exceptions.BadRequestException;
import com.rajat_singh.leetcode_api.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.tinylog.Logger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeetCodeService {

    private final LeetCodeClient leetCodeClient;

    private final ObjectMapper objectMapper;

    public Optional<UserProgressResponse.UserProfileUserQuestionProgressV2> getUserProfile(String username) {
        Logger.info("Fetching user profile stats for user: {}",username);
        var response = leetCodeClient.fetchUserProgress(username);

        if (response != null && response.getData() != null) {
            var userProfile = response.getData().getUserProfileUserQuestionProgressV2();
            Logger.info("User profile stats found for: {}",username);
            Logger.debug("User profile details: {}", userProfile);
            return Optional.ofNullable(userProfile);
        }

        Logger.warn("No user profile stats found for user: {}", username);
        throw new UserNotFoundException(username);
    }

    public Optional<UserLanguageStats.MatchedUser> getUserLanguageStats(String username){
        Logger.info("Fetching language stats for user: {}", username);
        var response = leetCodeClient.fetchUserLanguageStats(username);

        if(Objects.nonNull(response) && Objects.nonNull(response.getData()) && Objects.nonNull(response.getData().getMatchedUser())) {
            Logger.info("Language stats found for user: {}", username);
            var matchedUser = response.getData().getMatchedUser();
            Logger.debug("Language stats details: {}", matchedUser);
            return Optional.ofNullable(matchedUser);
        }

        Logger.warn("No language stats found for user: {}", username);
        throw new UserNotFoundException(username);
    }

    public Optional<UserPublicInfo.MatchedUser> getUserPublicInfo(String username){
        Logger.info("Fetching public info for user: {}", username);
        var response = leetCodeClient.fetchUserPublicInfo(username);

        if(Objects.nonNull(response) && Objects.nonNull(response.getData()) && Objects.nonNull(response.getData().getMatchedUser())){
            Logger.info("Public info found for user: {}", username);
            var matchedUser = response.getData().getMatchedUser();
            Logger.debug("Public info details: {}", matchedUser);
            return Optional.of(matchedUser);
        }

        Logger.warn("No public info found for user: {}", username);
        throw new UserNotFoundException(username);
    }


    public Optional<UsersBadgeListResponse.MatchedUser> getUserBadgesList(String username) {
        Logger.info("Fetching badges for user: {}", username);
        var response = leetCodeClient.fetchUserBadgesList(username);

        if (Objects.nonNull(response) && Objects.nonNull(response.getData()) && Objects.nonNull(response.getData().getMatchedUser())) {
            Logger.info("Badges found for user: {}", username);
            var matchedUser = response.getData().getMatchedUser();
            Logger.debug("Badges details: {}", matchedUser);
            return Optional.of(matchedUser);
        }

        Logger.warn("No badges found for user: {}", username);
        throw new UserNotFoundException(username);
    }

    public Optional<UserSkillStatsResponse.MatchedUser> getUserSkillStats(String username) {
        Logger.info("Fetching skill stats for user: {}", username);
        var response = leetCodeClient.fetchUserSkillStats(username);

        if (Objects.nonNull(response) && Objects.nonNull(response.getData()) && Objects.nonNull(response.getData().getMatchedUser())) {
            Logger.info("Skill stats found for user: {}", username);
            var matchedUser = response.getData().getMatchedUser();
            Logger.debug("Skill stats details: {}", matchedUser);
            return Optional.of(matchedUser);
        }

        Logger.warn("No skill stats found for user: {}", username);
        throw new UserNotFoundException(username);
    }

    public Optional<UserRecentSubmissionsResponse.DataNode> getUserRecentSubmissions(String username, int limit) {
        if(limit <= 0){
            throw new BadRequestException("Limit must be greater than 0");
        }

        Logger.info("Fetching recent submissions for user: {}", username);
        var response = leetCodeClient.fetchUserRecentSubmissions(username,limit);

        if (Objects.nonNull(response) && Objects.nonNull(response.getData())) {
            Logger.info("Recent submissions found for user: {}", username);
            var dataNode = response.getData();
            Logger.debug("Recent submissions details: {}", dataNode);
            return Optional.of(dataNode);
        }

        Logger.warn("No recent submissions found for user: {}", username);
        throw new UserNotFoundException(username);
    }

    public Optional<UserCalendarDTO> getUserLeetCodeCalendar(String username,int year) {
        if(year < 2015 || year > Year.now().getValue()){
            throw new BadRequestException("Year must be between 2015 and current year");
        }

        Logger.info("Fetching LeetCode calendar for user: {}", username);
        var response = leetCodeClient.fetchUserLeetCodeCalendar(username,year);

        if (Objects.nonNull(response) && Objects.nonNull(response.getData())) {
            Logger.info("LeetCode calendar found for user: {}", username);

            UserLeetCodeCalendarResponse.UserCalendar rawCalendar = response.getData()
                    .getMatchedUser()
                    .getUserCalendar();
            Map<String, Integer> submissionMap;
            String calendarJsonString = rawCalendar.getSubmissionCalendar();

            try {
                TypeReference<Map<String, Integer>> typeRef = new TypeReference<>() {};
                submissionMap = objectMapper.readValue(calendarJsonString, typeRef);
            } catch (JsonProcessingException e) {
                Logger.warn("Error while parsing calenderResponse {}",e.getMessage());
                submissionMap = Map.of();
            }
            UserCalendarDTO cleanDto = new UserCalendarDTO();
            cleanDto.setActiveYears(rawCalendar.getActiveYears());
            cleanDto.setStreak(rawCalendar.getStreak());
            cleanDto.setTotalActiveDays(rawCalendar.getTotalActiveDays());
            cleanDto.setDccBadges(rawCalendar.getDccBadges());
            cleanDto.setSubmissionCalendar(submissionMap);
            return Optional.of(cleanDto);
        }

        Logger.warn("No LeetCode calendar found for user: {}", username);
        throw new UserNotFoundException(username);
    }
}