# 회원 API 명세서

이 문서는 인증, 프로필 관리, 소셜 로그인을 포함한 회원 관련 API 명세를 종합적으로 제공합니다.

---

## 1. 인증 (Authentication)

### 1.1. 회원가입

-   **HTTP Method**: `POST`
-   **URL**: `/api/member/create`
-   **설명**: 새로운 회원 계정을 `INACTIVE` 상태로 생성하고, 인증 이메일을 발송합니다.
-   **요청 본문 (Request Body)**:
    ```json
    {
      "username": "테스트유저",
      "email": "test@example.com",
      "password": "password123",
      "agreedTermIds": [1, 2, 3, 4]
    }
    ```
-   **성공 응답 (201 Created)**:
    ```json
    {
        "success": true,
        "data": 1,
        "message": "회원가입 성공. 이메일 인증을 완료해주세요."
    }
    ```
-   **에러 응답**:
    -   **409 Conflict**: 이미 사용 중인 이메일일 경우
    -   **400 Bad Request**: 필수 약관에 동의하지 않았을 경우

### 1.2. 이메일 인증

-   **HTTP Method**: `GET`
-   **URL**: `/api/member/verify`
-   **설명**: 회원가입 시 발송된 이메일의 토큰을 사용하여 계정을 활성화합니다.
-   **쿼리 파라미터 (Query Parameters)**:
    -   `token` (String): 이메일 링크에 포함된 인증 토큰
-   **성공 응답 (200 OK)**: 인증 완료 HTML 페이지를 반환합니다.
-   **에러 응답**:
    -   **404 Not Found**: 토큰이 유효하지 않을 경우
    -   **400 Bad Request**: 토큰이 만료되었을 경우

### 1.3. 일반 로그인

-   **HTTP Method**: `POST`
-   **URL**: `/api/member/login`
-   **설명**: 이메일과 비밀번호로 로그인하고 JWT를 발급받습니다.
-   **요청 본문 (Request Body)**:
    ```json
    {
      "email": "user1@test.com",
      "password": "1234"
    }
    ```
-   **성공 응답 (200 OK)**:
    ```json
    {
        "success": true,
        "data": {
            "token": "jwt.token.string",
            "member": {
                "id": 1,
                "username": "김철수",
                "email": "user1@test.com",
                "role": "USER",
                "status": "ACTIVE",
                "profileImageUrl": null,
                "socialType": "LOCAL",
                "isPremium": false
            }
        },
        "message": "로그인 성공"
    }
    ```
-   **에러 응답 (401 Unauthorized)**:
    -   "이메일 또는 비밀번호가 일치하지 않습니다."
    -   "이메일 인증이 완료되지 않은 계정입니다. 이메일을 확인해주세요."
    -   "사용이 중지된 계정입니다."
    -   "휴면계정입니다. 다시 로그인하여 활성화해주세요."

### 1.4. 로그아웃

-   **HTTP Method**: `POST`
-   **URL**: `/api/member/logout`
-   **설명**: 현재 JWT 토큰을 블랙리스트에 추가하여 세션을 무효화합니다.
-   **헤더 (Headers)**:
    -   `Authorization`: `Bearer {jwtToken}`
-   **성공 응답 (200 OK)**:
    ```json
    {
      "success": true,
      "data": null,
      "message": "로그아웃 성공"
    }
    ```
-   **에러 응답 (400 Bad Request)**: "유효하지 않은 토큰입니다."

---

## 2. 소셜 로그인 (Social Login)

### 2.1. 카카오 로그인

-   **HTTP Method**: `POST`
-   **URL**: `/api/member/kakao/token`
-   **설명**: 모바일 SDK에서 발급받은 카카오 액세스 토큰으로 로그인 또는 회원가입을 처리합니다.
-   **요청 본문 (Request Body)**:
    ```json
    {
      "access_token": "kakao_access_token_from_client_sdk"
    }
    ```
-   **성공 응답 (200 OK)**: 일반 로그인과 동일한 형식의 JWT와 회원 정보를 반환합니다.
-   **에러 응답**:
    -   **400 Bad Request**: "유효하지 않은 카카오 액세스 토큰입니다."
    -   **500 Internal Server Error**: "소셜 로그인 서비스 연동 중 서버 오류가 발생했습니다."

### 2.2. 페이스북 로그인

-   **HTTP Method**: `POST`
-   **URL**: `/api/member/facebook/doLogin`
-   **설명**: 모바일 SDK에서 발급받은 페이스북 액세스 토큰으로 로그인 또는 회원가입을 처리합니다.
-   **요청 본문 (Request Body)**:
    ```json
    {
      "access_token": "facebook_access_token_from_client_sdk"
    }
    ```
-   **성공 응답 (200 OK)**: 일반 로그인과 동일한 형식의 JWT와 회원 정보를 반환합니다.
-   **에러 응답**:
    -   **400 Bad Request**: "유효하지 않은 페이스북 액세스 토큰입니다."
    -   **500 Internal Server Error**: "소셜 로그인 서비스 연동 중 서버 오류가 발생했습니다."

---

## 3. 프로필 관리 (Profile Management)

### 3.1. 회원 정보 조회

-   **HTTP Method**: `GET`
-   **URL**: `/api/member/{memberId}`
-   **설명**: 특정 회원의 프로필 정보를 조회합니다.
-   **헤더 (Headers)**:
    -   `Authorization`: `Bearer {jwtToken}`
-   **성공 응답 (200 OK)**:
    ```json
    {
      "success": true,
      "data": {
        "id": 1,
        "username": "김철수",
        "email": "user1@test.com"
      },
      "message": "회원 조회 성공"
    }
    ```
-   **에러 응답 (404 Not Found)**: "일치하는 회원이 없습니다."

### 3.2. 회원 정보 수정

-   **HTTP Method**: `PUT`
-   **URL**: `/api/member/{memberId}`
-   **설명**: 회원의 이름, 프로필 이미지, 기본 주소 등을 수정합니다.
-   **헤더 (Headers)**:
    -   `Authorization`: `Bearer {jwtToken}`
-   **요청 본문 (Request Body)**:
    ```json
    {
      "username": "김오스",
      "profileImageUrl": "/new/image.jpg",
      "defaultAddress": "부산광역시"
    }
    ```
-   **성공 응답 (200 OK)**: 수정된 회원 정보 DTO를 반환합니다.
-   **에러 응답 (404 Not Found)**: "일치하는 회원이 없습니다."

### 3.3. 비밀번호 수정

-   **HTTP Method**: `PATCH`
-   **URL**: `/api/member/{memberId}/password`
-   **설명**: 현재 비밀번호를 확인한 후, 회원의 비밀번호를 변경합니다.
-   **헤더 (Headers)**:
    -   `Authorization`: `Bearer {jwtToken}`
-   **요청 본문 (Request Body)**:
    ```json
    {
      "currentPassword": "1234",
      "newPassword": "new_password_1234"
    }
    ```
-   **성공 응답 (200 OK)**:
    ```json
    {
      "success": true,
      "data": null,
      "message": "비밀번호 수정 성공"
    }
    ```
-   **에러 응답**:
    -   **400 Bad Request**: "현재 비밀번호가 일치하지 않습니다."
    -   **404 Not Found**: "일치하는 회원이 없습니다."

### 3.4. 비밀번호 확인

-   **HTTP Method**: `POST`
-   **URL**: `/api/member/{memberId}/check-password`
-   **설명**: 민감한 정보 변경 전, 사용자의 현재 비밀번호가 일치하는지 확인합니다.
-   **헤더 (Headers)**:
    -   `Authorization`: `Bearer {jwtToken}`
-   **요청 본문 (Request Body)**:
    ```json
    {
      "password": "1234"
    }
    ```
-   **성공 응답 (200 OK)**:
    ```json
    {
      "success": true,
      "data": null,
      "message": "비밀번호 확인 성공"
    }
    ```
-   **에러 응답 (401 Unauthorized)**: "비밀번호가 일치하지 않습니다."

### 3.5. 프로필 이미지 업로드

-   **HTTP Method**: `POST`
-   **URL**: `/api/member/profile/upload/{memberId}`
-   **설명**: 회원의 프로필 이미지를 업로드합니다. 요청 형식은 `multipart/form-data`여야 합니다.
-   **헤더 (Headers)**:
    -   `Authorization`: `Bearer {jwtToken}`
-   **폼 데이터 (Form Data)**:
    -   `image` (File): 이미지 파일
-   **성공 응답 (200 OK)**:
    ```json
    {
      "success": true,
      "data": "/path/to/uploaded_image.jpg",
      "message": "성공"
    }
    ```

### 3.6. 프로필 이미지 삭제

-   **HTTP Method**: `DELETE`
-   **URL**: `/api/member/profile/delete/{memberId}`
-   **설명**: 회원의 프로필 이미지를 삭제합니다.
-   **헤더 (Headers)**:
    -   `Authorization`: `Bearer {jwtToken}`
-   **성공 응답 (200 OK)**:
    ```json
    {
      "success": true,
      "data": null,
      "message": "프로필 이미지 삭제 성공"
    }
    ```

### 3.7. 회원 탈퇴

-   **HTTP Method**: `DELETE`
-   **URL**: `/api/member/{memberId}`
-   **설명**: 회원 계정을 비활성화 상태로 변경합니다.
-   **헤더 (Headers)**:
    -   `Authorization`: `Bearer {jwtToken}`
-   **성공 응답 (200 OK)**:
    ```json
    {
      "success": true,
      "data": null,
      "message": "회원 탈퇴 성공"
    }
    ```
-   **에러 응답 (404 Not Found)**: "일치하는 회원이 없습니다."

---

## 4. 계정 복구 (Account Recovery)

### 4.1. 아이디 찾기

-   **HTTP Method**: `POST`
-   **URL**: `/api/member/find-id`
-   **설명**: 이메일 주소를 통해 사용자의 아이디(username)를 찾습니다.
-   **요청 본문 (Request Body)**:
    ```json
    {
      "email": "user1@test.com"
    }
    ```
-   **성공 응답 (200 OK)**:
    ```json
    {
      "success": true,
      "data": "김철수",
      "message": "아이디 찾기 성공"
    }
    ```

### 4.2. 비밀번호 찾기

-   **HTTP Method**: `POST`
-   **URL**: `/api/member/find-password`
-   **설명**: 등록된 이메일로 임시 비밀번호를 발송합니다.
-   **요청 본문 (Request Body)**:
    ```json
    {
      "email": "user1@test.com"
    }
    ```
-   **성공 응답 (200 OK)**:
    ```json
    {
      "success": true,
      "data": null,
      "message": "비밀번호 찾기용 메일 보내기 성공"
    }
    ```
