# Place & Tag API Documentation

This document outlines the API specifications for places and tags.

---

## 1. 태그로 장소 검색 (Search Places by Tag)

-   **HTTP Method**: `GET`
-   **URL**: `/api/places/search-by-tag`
-   **Description**: Returns a list of place IDs that have a specific tag.
-   **Query Parameters**:
    -   `tagName` (String, required): The name of the tag to search for.
-   **Example Request**:
    ```
    GET /api/places/search-by-tag?tagName=카페
    ```
-   **Success Response (200 OK)**:
    ```json
    {
        "success": true,
        "data": [1, 5, 12],
        "message": "태그로 장소 검색 성공"
    }
    ```
-   **Error Response (404 Not Found)**:
    ```json
    {
        "success": false,
        "data": null,
        "message": "해당 태그를 찾을 수 없습니다: 존재하지않는태그"
    }
    ```

---

## 2. 태그 기반 장소 추천 (Recommend Places by Tags)

-   **HTTP Method**: `GET`
-   **URL**: `/api/places/recommend`
-   **Description**: Recommends a list of places based on a list of tags and a plan ID. The number of recommended places is limited by the number of participants in the plan. The results are sorted by the number of matching tags (descending) and then by place ID (ascending).
-   **Query Parameters**:
    -   `planId` (Long, required): The ID of the plan to base the recommendation on.
    -   `tagNames` (List<String>, required): A list of tag names to use for the recommendation.
-   **Example Request**:
    ```
    GET /api/places/recommend?planId=1&tagNames=카페,조용한,스터디
    ```
-   **Success Response (200 OK)**:
    ```json
    {
        "success": true,
        "data": [
            {
                "id": 5,
                "name": "조용한 스터디 카페",
                "address": "서울시 강남구",
                "lat": 37.504,
                "lng": 127.048,
                "description": "...",
                "imageUrl": "...",
                "tags": ["카페", "조용한", "스터디", "콘센트"]
            },
            {
                "id": 12,
                "name": "북 카페",
                "address": "서울시 마포구",
                "lat": 37.555,
                "lng": 126.924,
                "description": "...",
                "imageUrl": "...",
                "tags": ["카페", "책", "조용한"]
            }
        ],
        "message": "장소 추천 성공"
    }
    ```
-   **Error Response (404 Not Found)**:
    ```json
    {
        "success": false,
        "data": null,
        "message": "해당 계획을 찾을 수 없습니다: 999"
    }
    ```

---

## 3. 이름으로 장소 ID 검색 (Search Place ID by Name)

-   **HTTP Method**: `GET`
-   **URL**: `/api/places/search-by-name`
-   **Description**: Returns the ID of a place that matches the given name.
-   **Query Parameters**:
    -   `placeName` (String, required): The exact name of the place to search for.
-   **Example Request**:
    ```
    GET /api/places/search-by-name?placeName=스타벅스 강남점
    ```
-   **Success Response (200 OK)**:
    ```json
    {
        "success": true,
        "data": 25,
        "message": "이름으로 장소 검색 성공"
    }
    ```
-   **Error Response (404 Not Found)**:
    ```json
    {
        "success": false,
        "data": null,
        "message": "해당 장소를 찾을 수 없습니다: 존재하지않는장소"
    }
    ```

---

## 4. 장소 이름 자동 완성 (Autocomplete Place Names)

-   **HTTP Method**: `GET`
-   **URL**: `/api/places/autocomplete/name`
-   **Description**: Returns a list of place names that start with the given prefix. (Max 10 results)
-   **Query Parameters**:
    -   `prefix` (String, required): The prefix to search for.
-   **Example Request**:
    ```
    GET /api/places/autocomplete/name?prefix=부산
    ```
-   **Success Response (200 OK)**:
    ```json
    {
        "success": true,
        "data": [
            "부산역",
            "부산대학교",
            "부산시민공원"
        ],
        "message": "장소 이름 자동 완성 성공"
    }
    ```

---

## 5. 태그 이름 자동 완성 (Autocomplete Tag Names)

-   **HTTP Method**: `GET`
-   **URL**: `/api/places/autocomplete/tag`
-   **Description**: Returns a list of tag names that start with the given prefix. (Max 10 results)
-   **Query Parameters**:
    -   `prefix` (String, required): The prefix to search for.
-   **Example Request**:
    ```
    GET /api/places/autocomplete/tag?prefix=회
    ```
-   **Success Response (200 OK)**:
    ```json
    {
        "success": true,
        "data": [
            "횟집",
            "회사 근처"
        ],
        "message": "태그 이름 자동 완성 성공"
    }
    ```
