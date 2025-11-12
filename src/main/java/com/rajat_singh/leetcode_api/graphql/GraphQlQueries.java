package com.rajat_singh.leetcode_api.graphql;

public class GraphQlQueries {

    public static final String userProgressQuery = """
                query userProfileUserQuestionProgressV2($userSlug: String!) {
                  userProfileUserQuestionProgressV2(userSlug: $userSlug) {
                    numAcceptedQuestions { count difficulty }
                    numFailedQuestions { count difficulty }
                    numUntouchedQuestions { count difficulty }
                    userSessionBeatsPercentage { difficulty percentage }
                    totalQuestionBeatsPercentage
                  }
                }
                """;

    public static final String UserLanguageStatsQuery = """
                   query languageStats($username: String!) {
                     matchedUser(username: $username) {
                        languageProblemCount {
                            languageName
                            problemsSolved
                        }
                      }
                   }
                """;

    public static final String UserPublicInfoQuery = """
                query userPublicProfile($username: String!) {
                  matchedUser(username: $username) {
                    contestBadge {
                      name
                      expired
                      hoverText
                      icon
                    }
                    username
                    githubUrl
                    twitterUrl
                    linkedinUrl
                    profile {
                      ranking
                      userAvatar
                      realName
                      aboutMe
                      school
                      websites
                      countryName
                      company
                      jobTitle
                      skillTags
                      postViewCount
                      postViewCountDiff
                      reputation
                      reputationDiff
                      solutionCount
                      solutionCountDiff
                      categoryDiscussCount
                      categoryDiscussCountDiff
                      certificationLevel
                    }
                  }
                }
            """;

    public static final String USER_PERSONAL_INFO_QUERY = """
                query userPublicPersonalInfo($username: String!) {
                  matchedUser(username: $username) {
                    username
                    githubUrl
                    twitterUrl
                    linkedinUrl
                    profile {
                      userAvatar
                      realName
                      aboutMe
                      school
                      websites
                      countryName
                      company
                      jobTitle
                      skillTags
                    }
                  }
                }
            """;

    public static final String USER_CONTEST_RANKING = """
                query userContestRanking($username: String!) {
                  userContestRanking(username: $username) {
                    attendedContestsCount
                    rating
                    globalRanking
                    totalParticipants
                    topPercentage
                    badge { name }
                  }
                }
            """;

    public static final String USER_CONTEST_RANKING_WITH_HISTORY = """
                query userContestRankingInfo($username: String!) {
                  userContestRanking(username: $username) {
                    attendedContestsCount
                    rating
                    globalRanking
                    totalParticipants
                    topPercentage
                    badge { name }
                  }
                  userContestRankingHistory(username: $username) {
                    attended
                    trendDirection
                    problemsSolved
                    totalProblems
                    finishTimeInSeconds
                    rating
                    ranking
                    contest { title startTime }
                  }
                }
            """;

    public static final String USER_CONTEST_RANKING_HISTORY_ONLY = """
                query userContestRankingInfo($username: String!) {
                  userContestRankingHistory(username: $username) {
                    attended
                    trendDirection
                    problemsSolved
                    totalProblems
                    finishTimeInSeconds
                    rating
                    ranking
                    contest { title startTime }
                  }
                }
            """;

    public static final String FETCH_QUESTIONS_QUERY = """
            query problemsetQuestionListV2(
              $filters: QuestionFilterInput,
              $limit: Int,
              $searchKeyword: String,
              $skip: Int,
              $sortBy: QuestionSortByInput,
              $categorySlug: String
            ) {
              problemsetQuestionListV2(
                filters: $filters
                limit: $limit
                searchKeyword: $searchKeyword
                skip: $skip
                sortBy: $sortBy
                categorySlug: $categorySlug
              ) {
                questions {
                  id
                  titleSlug
                  title
                  translatedTitle
                  difficulty
                  topicTags {
                    name
                    slug
                  }
                  acRate
                  contestPoint
                }
                totalLength
              }
            }
            """;

    public static final String FETCH_QUESTIONS_FOR_AC_RATE_SYNC_QUERY = """
            query problemsetQuestionListV2(
              $filters: QuestionFilterInput,
              $limit: Int,
              $searchKeyword: String,
              $skip: Int,
              $sortBy: QuestionSortByInput,
              $categorySlug: String
            ) {
              problemsetQuestionListV2(
                filters: $filters
                limit: $limit
                searchKeyword: $searchKeyword
                skip: $skip
                sortBy: $sortBy
                categorySlug: $categorySlug
              ) {
                questions {
                  id
                  titleSlug
                  acRate
                }
                totalLength
              }
            }
            """;

    public static final String FETCH_USER_BADGES_QUERY = """
            query userBadges($username: String!) {
              matchedUser(username: $username) {
                badges {
                  id
                  name
                  shortName
                  displayName
                  icon
                  hoverText
                  medal {
                    slug
                    config {
                      iconGif
                      iconGifBackground
                    }
                  }
                  creationDate
                  category
                }
                upcomingBadges {
                  name
                  icon
                  progress
                }
              }
            }
            """;

    public static final String FETCH_USER_SKILL_STATS = """
            query skillStats($username: String!) {
              matchedUser(username: $username) {
                tagProblemCounts {
                  advanced {
                    tagName
                    tagSlug
                    problemsSolved
                  }
                  intermediate {
                    tagName
                    tagSlug
                    problemsSolved
                  }
                  fundamental {
                    tagName
                    tagSlug
                    problemsSolved
                  }
                }
              }
            }
            """;

    public static final String FETCH_USER_RECENT_SUBMISSIONS = """
           query recentAcSubmissions($username: String!, $limit: Int!) {
              recentAcSubmissionList(username: $username, limit: $limit) {
                id
                title
                titleSlug
                timestamp
                lang
                time
                url
                statusDisplay
              }
            }
           """;

    public static final String FETCH_USER_CALENDAR_SUBMISSIONS = """
           query userProfileCalendar($username: String!, $year: Int) {
              matchedUser(username: $username) {
                userCalendar(year: $year) {
                  activeYears
                  streak
                  totalActiveDays
                  dccBadges {
                    timestamp
                    badge {
                      name
                      icon
                    }
                  }
                  submissionCalendar
                }
              }
            }
           """;

    public static final String FETCH_POTD = """
            query questionOfToday {
              activeDailyCodingChallengeQuestion {
                date
                link
                question {
                  titleSlug
                }
              }
            }
            """;

    public static final String FETCH_ALL_PAST_CONTESTS = """
            query pastContests($pageNo: Int, $numPerPage: Int) {
              pastContests(pageNo: $pageNo, numPerPage: $numPerPage) {
                pageNum
                currentPage
                totalNum
                numPerPage
                data {
                  title
                  titleSlug
                  startTime
                  originStartTime
                  cardImg
                  sponsors {
                    name
                    lightLogo
                    darkLogo
                  }
                }
              }
            }
            """;

}
