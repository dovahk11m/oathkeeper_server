package com.oath.document;

public class MemberApiResponseExamples {

    public static class MemberCreate {
        public static final String SUCCESS_201 = """
                {
                  "success": true,
                  "data": 1,
                  "message": "회원가입 성공. 이메일 인증을 완료해주세요."
                }
                """;

        public static final String CONFLICT_409 = """
                {
                  "success": false,
                  "data": null,
                  "message": "이미 사용 중인 이메일입니다."
                }
                """;
    }

    public static class MemberLogin {
        public static final String SUCCESS_200 = """
                {
                  "success": true,
                  "data": {
                    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMUB0ZXN0LmNvbSIsInJvbGUiOiJVU0VSIiwiaWQiOjEsImV4cCI6MTcyOTI4NjQwMH0.abcdef...",
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
                """;

        public static final String UNAUTHORIZED_401 = """
                {
                  "success": false,
                  "data": null,
                  "message": "이메일 또는 비밀번호가 일치하지 않습니다."
                }
                """;

        public static final String INACTIVE_401 = """
                {
                  "success": false,
                  "data": null,
                  "message": "이메일 인증이 완료되지 않은 계정입니다. 이메일을 확인해주세요."
                }
                """;
    }
}
