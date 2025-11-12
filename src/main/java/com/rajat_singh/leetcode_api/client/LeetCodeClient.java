package com.rajat_singh.leetcode_api.client;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rajat_singh.leetcode_api.dto.*;
import com.rajat_singh.leetcode_api.enums.UserContestType;
import com.rajat_singh.leetcode_api.enums.questions.FilterOperator;
import com.rajat_singh.leetcode_api.enums.questions.SortField;
import com.rajat_singh.leetcode_api.enums.questions.SortOrder;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.function.ServerRequest;
import org.tinylog.Logger;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rajat_singh.leetcode_api.graphql.GraphQlQueries.*;

@Service
public class LeetCodeClient {

    private final RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${leetcode.graphql.url}")
    private String leetcodeApiUrl;

    /*
     * Fetches user progress data from LeetCode.
     */
    @RateLimiter(name = "leetcode-api")
    public UserProgressResponse fetchUserProgress(String username) {
        HttpHeaders headers = new HttpHeaders();
        setHeader(headers);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", userProgressQuery);

        Map<String, Object> variables = new HashMap<>();
        variables.put("userSlug", username);
        requestBody.put("variables", variables);
        requestBody.put("operationName", "userProfileUserQuestionProgressV2");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForObject(leetcodeApiUrl, entity, UserProgressResponse.class);
    }

    /*
     * Fetches user language statistics from LeetCode.
     */
    @RateLimiter(name = "leetcode-api")
    public UserLanguageStats fetchUserLanguageStats(String username) {
        HttpHeaders headers = new HttpHeaders();
        setHeader(headers);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", UserLanguageStatsQuery);

        Map<String, Object> variables = new HashMap<>();
        variables.put("username", username);
        requestBody.put("variables", variables);
        requestBody.put("operationName", "languageStats");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForObject(leetcodeApiUrl, entity, UserLanguageStats.class);
    }

    @RateLimiter(name = "leetcode-api")
    public UserPublicInfo fetchUserPublicInfo(String username){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String,Object> requestBody = new HashMap<>();
        requestBody.put("query", UserPublicInfoQuery);

        Map<String, Object> variables = new HashMap<>();
        variables.put("username", username);
        requestBody.put("variables", variables);
        requestBody.put("operationName", "userPublicProfile");

        HttpEntity<Map<String ,Object>> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForObject(leetcodeApiUrl,entity,UserPublicInfo.class);
    }

    /*
    * Fetches user contest ranking based on the specified history status.
    */
    @RateLimiter(name = "leetcode-api")
    public UserContestResponse fetchUserContestRanking(String username, UserContestType historyStatus) {

        HttpHeaders headers = new HttpHeaders();
        setHeader(headers);

        Map<String,Object> requestBody = new HashMap<>();

        String query = "";
        switch (historyStatus){
            case INCLUDE_CONTEST_HISTORY:
                query = USER_CONTEST_RANKING_WITH_HISTORY;
                break;
            case EXCLUDE_CONTEST_HISTORY:
                query = USER_CONTEST_RANKING;
                break;
            case ONLY_CONTEST_HISTORY:
                query = USER_CONTEST_RANKING_HISTORY_ONLY;
                break;
            default:
                Logger.warn("History Status not matched with any existing type.");
        }
        requestBody.put("query", query);

        Map<String, Object> variables = new HashMap<>();
        variables.put("username", username);
        requestBody.put("variables", variables);
        requestBody.put("operationName", historyStatus.equals(UserContestType.EXCLUDE_CONTEST_HISTORY)?"userContestRanking" : "userContestRankingInfo");

        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForObject(leetcodeApiUrl, entity, UserContestResponse.class);
    }

    /*
    * Fetches a list of questions from LeetCode based on the provided parameters.
    */
    @RateLimiter(name = "leetcode-api")
    public QuestionListResponse fetchQuestionList(int skip, int limit, String categorySlug, String searchKeyword, QuestionSearchRequest.SortingCriteria sortBy, QuestionSearchRequest.FilterCriteria filters) {
        HttpHeaders headers = new HttpHeaders();
        setHeader(headers);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", FETCH_QUESTIONS_QUERY);
        requestBody.put("operationName", "problemsetQuestionListV2");

        Map<String, Object> variables = new HashMap<>();
        variables.put("skip", skip);
        variables.put("limit", limit);
        variables.put("categorySlug", categorySlug);
        variables.put("searchKeyword", searchKeyword);
        variables.put("sortBy", sortBy);
        variables.put("filters", filters);
        requestBody.put("variables", variables);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForObject(leetcodeApiUrl, entity, QuestionListResponse.class);
    }

    /*
    * Fetches all questions from LeetCode with a high limit.
    */
    @RateLimiter(name = "leetcode-api")
    public QuestionListResponse fetchAllQuestions(Boolean forAcRateSync) {
        HttpHeaders headers = new HttpHeaders();
        setHeader(headers);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", forAcRateSync? FETCH_QUESTIONS_FOR_AC_RATE_SYNC_QUERY : FETCH_QUESTIONS_QUERY);
        requestBody.put("operationName", "problemsetQuestionListV2");

        Map<String, Object> variables = new HashMap<>();
        variables.put("skip", 0);
        variables.put("limit", 10000);
        variables.put("categorySlug", "all-code-essentials");
        variables.put("searchKeyword", "");
        variables.put("sortBy", QuestionSearchRequest.SortingCriteria.builder()
                .sortField(SortField.CUSTOM)
                .sortOrder(SortOrder.ASCENDING)
                .build());
        variables.put("filters", QuestionSearchRequest.FilterCriteria.builder()
                .filterCombineType("ALL")
                .difficultyFilter(QuestionSearchRequest.DifficultyFilter.builder()
                        .difficulties(new ArrayList<>())
                        .operator(FilterOperator.IS)
                        .build())
                .languageFilter(QuestionSearchRequest.LanguageFilter.builder()
                        .languageSlugs(new ArrayList<>())
                        .operator(FilterOperator.IS)
                        .build())
                .topicFilter(QuestionSearchRequest.TopicFilter.builder()
                        .topicSlugs(new ArrayList<>())
                        .operator(FilterOperator.IS)
                        .build())
                .build());
        requestBody.put("variables", variables);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForObject(leetcodeApiUrl, entity, QuestionListResponse.class);
    }

    /*
    * Sets the necessary HTTP headers for the request.
    */
    public void setHeader(HttpHeaders headers){
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        headers.add("Referer", "https://leetcode.com");
        headers.add("Origin", "https://leetcode.com");
    }

    @RateLimiter(name = "leetcode-api")
    public UsersBadgeListResponse fetchUserBadgesList(String username) {
        HttpHeaders headers = new HttpHeaders();
        setHeader(headers);

        Map<String,Object> requestBody = new HashMap<>();
        requestBody.put("query", FETCH_USER_BADGES_QUERY);

        Map<String, Object> variables = new HashMap<>();
        variables.put("username", username);
        requestBody.put("variables", variables);
        requestBody.put("operationName", "userBadges");

        HttpEntity<Map<String ,Object>> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForObject(leetcodeApiUrl,entity,UsersBadgeListResponse.class);
    }

    @RateLimiter(name = "leetcode-api")
    public UserSkillStatsResponse fetchUserSkillStats(String username) {
        HttpHeaders headers = new HttpHeaders();
        setHeader(headers);

        Map<String,Object> requestBody = new HashMap<>();
        requestBody.put("query", FETCH_USER_SKILL_STATS);

        Map<String, Object> variables = new HashMap<>();
        variables.put("username", username);
        requestBody.put("variables", variables);
        requestBody.put("operationName", "skillStats");

        HttpEntity<Map<String ,Object>> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForObject(leetcodeApiUrl,entity,UserSkillStatsResponse.class);
    }

    @RateLimiter(name = "leetcode-api")
    public UserRecentSubmissionsResponse fetchUserRecentSubmissions(String username,int limit) {
        HttpHeaders headers = new HttpHeaders();
        setHeader(headers);

        Map<String,Object> requestBody = new HashMap<>();
        requestBody.put("query", FETCH_USER_RECENT_SUBMISSIONS);

        Map<String, Object> variables = new HashMap<>();
        variables.put("username", username);
        variables.put("limit", limit);
        requestBody.put("variables", variables);
        requestBody.put("operationName", "recentAcSubmissions");

        HttpEntity<Map<String ,Object>> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForObject(leetcodeApiUrl,entity,UserRecentSubmissionsResponse.class);
    }

    @RateLimiter(name = "leetcode-api")
    public UserLeetCodeCalendarResponse fetchUserLeetCodeCalendar(String username, int year) {
        HttpHeaders headers = new HttpHeaders();
        setHeader(headers);

        Map<String,Object> requestBody = new HashMap<>();
        requestBody.put("query", FETCH_USER_CALENDAR_SUBMISSIONS);

        Map<String, Object> variables = new HashMap<>();
        variables.put("username", username);
        variables.put("year", year);
        requestBody.put("variables", variables);
        requestBody.put("operationName", "userProfileCalendar");

        HttpEntity<Map<String ,Object>> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForObject(leetcodeApiUrl,entity,UserLeetCodeCalendarResponse.class);
    }

    @RateLimiter(name = "leetcode-api")
    public DailyCodingChallengeResponse fetchDailyCodingChallengeQuestions() {
        HttpHeaders headers = new HttpHeaders();
        setHeader(headers);

        Map<String,Object> requestBody = new HashMap<>();
        requestBody.put("query", FETCH_POTD);

        HttpEntity<Map<String ,Object>> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForObject(leetcodeApiUrl,entity,DailyCodingChallengeResponse.class);
    }

    @RateLimiter(name = "leetcode-api")
    public List<ContestsDTO.ContestData> fetchAllPastContest() {

        int pageNo = 1;
        int numPerPage = 10;

        List<ContestsDTO.ContestData> combined = new ArrayList<>();

        while (true) {
            ContestsDTO response = fetchPage(pageNo, numPerPage);

            Logger.info("Fetched page {} of past contests", pageNo);

            if (response == null || response.getData() == null) break;

            List<ContestsDTO.ContestData> pageData = response.getData().getPastContests().getData();

            if (pageData == null || pageData.isEmpty()) break;

            combined.addAll(pageData);

            int current = response.getData().getPastContests().getCurrentPage();
            int total = response.getData().getPastContests().getPageNum();

            if (current >= total) break;

            pageNo++;
        }

        return combined;
    }

    public ContestsDTO fetchPage(int pageNo, int numPerPage) {

        HttpHeaders headers = new HttpHeaders();
        setHeader(headers);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", FETCH_ALL_PAST_CONTESTS);
        requestBody.put("operationName", "pastContests");
        requestBody.put("variables", Map.of("pageNo", pageNo, "numPerPage", numPerPage));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        return restTemplate.postForObject(leetcodeApiUrl, entity, ContestsDTO.class);
    }



}