<h1 align="center">leetstats-api</h1>

<p align="center">
<strong>The API for retrieving your LeetCode profile & Problems statistics</strong>
</p>

<p align="center">
<img src="https://img.shields.io/badge/License-MIT-green.svg"/>
<img src="https://img.shields.io/badge/Maven-3.9.11-blue.svg?logo=apachemaven"/>
</p>

<p align="center">
<img src="https://img.shields.io/badge/Java-17-%2320232a.svg?style=for-the-badge&logo=openjdk&logoColor=white"/>
<img src="https://img.shields.io/badge/Spring%20Boot-3.5.6-%2320232a?style=for-the-badge&logo=springboot&logoColor=6DB33F"/>
<img src="https://img.shields.io/badge/Hibernate-6.5.2-%252320232a.svg?style=for-the-badge&logo=hibernate&logoColor=59666C"/>
<img src="https://img.shields.io/badge/RestApi-%2320232a.svg?style=for-the-badge&logo=restAPI&logoColor=%23F7DF1E"/>
</p>

## About This Project

I started this project after struggling to find clear, comprehensive documentation for the leetcode.com/graphql endpoint. To fill this gap for other developers, I decided to build the solution I was looking for.

**leetstats-api** is that solution: a custom API wrapper designed to provide stable, well-documented, and easy-to-use endpoints for LeetCode data.

It provides simple access to:

- **User Info**: Profile, Badges, Submissions, Language Stats, Skill Stats
- **Contest Data**: History, Details, Rankings, and granular filtering
- **Problem Data**: Paginated and searchable list of all questions

## API URL 🌐

The API base path is `/api/v1`. When run locally, it will be available at:

```
http://localhost:8080/api/v1
```

## Run with Maven 🔧

```bash
./mvnw spring-boot:run
```

## Endpoints 🚀

All endpoints are relative to the base path `/api/v1`.

### 👤 User Endpoints

**Base Path**: `/api/v1/users/{username}`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/profile` | Get a user's question progress (accepted, failed, untouched). |
| GET | `/languageStats` | Get stats on languages used and problems solved per language. |
| GET | `/publicInfo` | Get a user's public profile info (name, avatar, ranking, social links). |
| GET | `/badges` | Get a list of badges earned by the user. |
| GET | `/userSkillStats` | Get advanced, intermediate, and fundamental skill stats. |
| GET | `/recentUserSubmissions/{limit}` | Get the {limit} most recent AC submissions for a user. |
| GET | `/userCalendarStats/{year}` | Get a user's submission calendar, streak, and active days for a given {year}. |

### 🏆 User Contest Endpoints

**Base Path**: `/api/v1/users/{username}/contests`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get user contest ranking and full contest history in one call. |
| GET | `/ranking` | Get just the user's contest ranking details (rating, global rank, etc.). |
| GET | `/bestRanking` | Get the user's single best-ranking contest performance. |
| GET | `/rankingHistory` | Get the user's entire contest history. |
| GET | `/contest-name/{contestTitle}` | Find contest history by matching part of a {contestTitle}. |
| GET | `/hasAttended/{attended}` | Filter history by attendance (true or false). |
| GET | `/trendDirection/{direction}` | Filter history by rating trend (UP, DOWN, NONE). |
| GET | `/problemSolvedGTE/{count}` | Filter history for contests where problems solved were >= {count}. |
| GET | `/problemSolvedLTE/{count}` | Filter history for contests where problems solved were <= {count}. |
| GET | `/finishTime/{timeInSeconds}` | Filter history for contests finished in less than {timeInSeconds}. |
| GET | `/biggestJumpInRating` | Get the contest that resulted in the user's biggest rating increase. |
| DELETE | `/evictUserData` | (Requires API Key) Evicts a user's contest data from the cache and DB. |

### ❓ Questions Endpoints

**Base Path**: `/api/v1/questions`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get a paginated list of all questions from the local database. Supports `?page=`, `&size=`, and `&sort=`. |
| POST | `/search` | A powerful search endpoint. See request body details below. |

#### POST /search Request Body

This endpoint allows for complex filtering and sorting of questions stored in the API's database.

**Example Request Body**:

```json
{
    "skip": 0,
    "limit": 20,
    "searchKeyword": "two sum",
    "sortBy": {
        "sortField": "AC_RATE",
        "sortOrder": "DESCENDING"
    },
    "filters": {
        "filterCombineType": "ALL",
        "difficultyFilter": {
            "difficulties": ["EASY", "MEDIUM"],
            "operator": "IS"
        },
        "topicFilter": {
            "topicSlugs": ["array", "hash-table"],
            "operator": "IS"
        },
        "acceptanceFilter": {
            "rangeLeft": 30.0,
            "rangeRight": 70.0
        }
    }
}
```
