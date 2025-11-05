package com.oath.document;

public class ApiResponseExamples {

    /**
     * GoogleMap의 API 성공 응답만을 담는 정적 클래스(코드 200)
     */
    public static class GoogleMap {

        /**
         * Matrix API가 성공한 경우
         */
        public static final String MATRIX_SUCCESS_200 =
                """
                        {
                             "success": true,
                             "data": {
                                 "matrixElements": [
                                     {
                                         "originIndex": 1,
                                         "destinationIndex": 0,
                                         "status": {
                                             "code": null,
                                             "message": null
                                         },
                                         "distanceMeters": 2646,
                                         "duration": "2229s",
                                         "condition": "ROUTE_EXISTS"
                                     },
                                     {
                                         "originIndex": 1,
                                         "destinationIndex": 1,
                                         "status": {
                                             "code": null,
                                             "message": null
                                         },
                                         "distanceMeters": 9749,
                                         "duration": "2445s",
                                         "condition": "ROUTE_EXISTS"
                                     },
                                     {
                                         "originIndex": 0,
                                         "destinationIndex": 0,
                                         "status": {
                                             "code": null,
                                             "message": null
                                         },
                                         "distanceMeters": 508,
                                         "duration": "418s",
                                         "condition": "ROUTE_EXISTS"
                                     },
                                     {
                                         "originIndex": 0,
                                         "destinationIndex": 1,
                                         "status": {
                                             "code": null,
                                             "message": null
                                         },
                                         "distanceMeters": 14417,
                                         "duration": "4549s",
                                         "condition": "ROUTE_EXISTS"
                                     }
                                 ]
                             },
                             "message": "각 거리 계산이 완료 되었습니다."
                         }
                        """;

        /**
         * TODO Matrix API가 실패한 경우
         * @cause
         */
    }

    /**
     * 네이버 맵의 API 성공 응답만을 담는 정적 클래스(코드 200)
     */
    public static class NaverMap {

        /**
         * 지오코딩 API가 성공한 경우
         */
        public static final String GEOCODING_SUCCESS_200 =
                """
                        {
                          "success": true,
                          "data": {
                            "latitude": 35.099655,
                            "longitude": 128.989365
                          },
                          "message": "지오코딩을 완료했습니다."
                        }
                        """;

        /**
         * 리버스 지오코딩 API가 성공한 경우
         */
        public static final String REVERSE_GEOCODING_SUCCESS_200 =
                """
                        {
                          "success": true,
                          "data": "전북특별자치도 전주시 완산구 삼천동1가",
                          "message": "리버스 지오코딩을 완료했습니다."
                        }
                        """;

        /**
         * 지오코딩 API가 실패한 경우
         *
         * @cause 주소가 우리나라 주소가 아님 (code 400)
         */
        public static final String GEOCODING_MISMATCH_ADDRESS_400 = """
                {
                  "success": false,
                  "data": null,
                  "message": "해당 주소는 우리나라 주소가 아닙니다."
                }
                """;

        /**
         * 지오코딩 API가 실패한 경우
         *
         * @cause 주소가 누락됨 (code 400)
         */
        public static final String GEOCODING_MISSING_ADDRESS_400 = """
                {
                  "success": false,
                  "data": null,
                  "message": "주소가 입력되지 않았습니다."
                }
                """;

        /**
         * 리버스 지오코딩 API가 실패한 경우
         *
         * @cause 좌표에 해당하는 주소가 우리나라 주소가 아님 (code 400)
         */
        public static final String REVERSE_GEOCODING_MISMATCH_POSITION_400 = """
                {
                  "success": false,
                  "data": null,
                  "message": "해당 좌표의 주소가 우리나라 주소가 아닙니다."
                }
                """;

        /**
         * 리버스 지오코딩 API가 실패한 경우
         *
         * @cause 좌표가 Double 값이 아님 (code 400)
         */
        public static final String REVERSE_GEOCODING_INVALID_POSITION_FORMAT_400 = """
                {
                  "success": false,
                  "data": null,
                  "message": "좌표는 Double 값으로만 입력해주세요."
                }
                """;

        /**
         * 리버스 지오코딩 API가 실패한 경우
         *
         * @cause 좌표가 Double 값이 아님 (code 400)
         */
        public static final String REVERSE_GEOCODING_MISSING_POSITION_400 = """
                {
                  "success": false,
                  "data": null,
                  "message": "좌표가 입력되지 않았습니다."
                }
                """;
    }
}
