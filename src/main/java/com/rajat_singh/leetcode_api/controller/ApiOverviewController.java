package com.rajat_singh.leetcode_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This controller provides a root "index" endpoint for the API,
 * documenting all available routes for users.
 */
@RestController
@RequestMapping("/api/v1")
public class ApiOverviewController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getApiOverview() {
        // Using LinkedHashMap to preserve the insertion order for a clean JSON output
        Map<String, Object> overview = new LinkedHashMap<>();

        overview.put("apiOverview", 
            "Welcome to leetcode-stats-api! This is a custom API wrapper for LeetCode data, providing stable, well-documented endpoints.");
        
        overview.put("apiDocumentationLink", 
            "https://github.com/rajat069/leetcode-stats-api/blob/main/README.md");

        Map<String, Object> routes = new LinkedHashMap<>();

        Map<String, Object> userDetails = new LinkedHashMap<>();
        userDetails.put("description", "Endpoints for retrieving detailed user profile information.");
        userDetails.put("Method", "GET");
        
        Map<String, String> userPaths = new LinkedHashMap<>();
        userPaths.put("/users/{username}/profile", "Get question progress (accepted, failed, untouched).");
        userPaths.put("/users/{username}/languageStats", "Get stats on languages used and problems solved per language.");
        userPaths.put("/users/{username}/publicInfo", "Get public profile info (avatar, ranking, social links).");
        userPaths.put("/users/{username}/badges", "Get a list of badges earned by the user.");
        userPaths.put("/users/{username}/userSkillStats", "Get advanced, intermediate, and fundamental skill stats.");
        userPaths.put("/users/{username}/recentUserSubmissions/{limit}", "Get the {limit} most recent AC submissions.");
        userPaths.put("/users/{username}/userCalendarStats/{year}", "Get submission calendar, streak, and active days for a given {year}.");
        
        userDetails.put("endpoints", userPaths);
        routes.put("userDetails", userDetails);

        Map<String, Object> userContests = new LinkedHashMap<>();
        userContests.put("description", "Endpoints for retrieving user contest history and rankings.");
        userContests.put("Method", "GET");

        Map<String, String> contestPaths = new LinkedHashMap<>();
        contestPaths.put("/users/{username}/contests/", "Get user contest ranking and full contest history.");
        contestPaths.put("/users/{username}/contests/ranking", "Get just the user's contest ranking details.");
        contestPaths.put("/users/{username}/contests/bestRanking", "Get the user's single best-ranking contest performance.");
        contestPaths.put("/users/{username}/contests/rankingHistory", "Get the user's entire contest history.");
        contestPaths.put("/users/{username}/contests/contest-name/{contestTitle}", "Find contest history by matching part of a {contestTitle}.");
        contestPaths.put("/users/{username}/contests/hasAttended/{attended}", "Filter history by attendance (true or false).");
        contestPaths.put("/users/{username}/contests/trendDirection/{direction}", "Filter history by rating trend (UP, DOWN, NONE).");
        contestPaths.put("/users/{username}/contests/problemSolvedGTE/{count}", "Filter history for contests where problems solved were >= {count}.");
        contestPaths.put("/users/{username}/contests/finishTime/{timeInSeconds}", "Filter history for contests finished in less than {timeInSeconds}.");
        contestPaths.put("/users/{username}/contests/biggestJumpInRating", "Get the contest with the user's biggest rating increase.");
        
        userContests.put("endpoints", contestPaths);
        routes.put("userContests", userContests);

        Map<String, Object> questions = new LinkedHashMap<>();
        questions.put("description", "Endpoints for fetching and searching questions from the local database.");
        
        Map<String, String> questionPaths = new LinkedHashMap<>();
        questionPaths.put("/questions/ (GET)", "Get a paginated list of all questions. Supports ?page=, &size=, &sort=.");
        questionPaths.put("/questions/potd (GET)", "Get the current LeetCode Problem of the Day (POTD).");
        questionPaths.put("/questions/search (POST)", "A powerful search endpoint to filter questions. See README for request body.");
        
        questions.put("endpoints", questionPaths);
        routes.put("questions", questions);


        Map<String, Object> globalContests = new LinkedHashMap<>();
        globalContests.put("description", "Endpoints for retrieving global contest data.");
        globalContests.put("Method", "GET");

        Map<String, String> globalContestPaths = new LinkedHashMap<>();
        globalContestPaths.put("/globalContestInfo/fetchContests", "Get a paginated list of all global contests from the local database.");
        
        globalContests.put("endpoints", globalContestPaths);
        routes.put("globalContests", globalContests);

        overview.put("routes", routes);

        return ResponseEntity.ok(overview);
    }

    @GetMapping(path = "/robots.txt", produces = "text/plain")
    public ResponseEntity<String> robotsTxt() {
        String content = """
        User-agent: *
        Disallow: /
        """;
        return ResponseEntity.ok(content);
    }

    @GetMapping(path = "", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getApiOverviewNoSlash() {
        return getApiOverview();
    }
}