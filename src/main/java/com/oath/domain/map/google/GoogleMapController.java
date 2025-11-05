package com.oath.domain.map.google;

import com.oath.common.CommonResponse;
import com.oath.document.ApiResponseExamples;
import com.oath.domain.map.google.dto.GoogleMapRequest;
import com.oath.domain.map.google.dto.GoogleMapResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map/google")
@Tag(name = "GoogleMap Controller", description = "구글 맵 컨트롤러")
public class GoogleMapController {

    private final GoogleMapService googleMapService;

    @Operation(summary = "사용자와 도착지 간의 거리, 소요시간을 반환해주는 API입니다.")
    @ApiResponses(value = {
            // 200 OK (성공)
            @ApiResponse(responseCode = "200", description = "거리, 소요 시간 계산에 성공한 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(
                                    name = "정상 응답",
                                    value = ApiResponseExamples.GoogleMap.MATRIX_SUCCESS_200
                            )
                    )
            ),

//            // TODO 400 Bad Request (형식이 잘못된 경우)
//            @ApiResponse(responseCode = "400", description = "",
//                    content = @Content(
//                            mediaType = "application/json",
//                            schema = @Schema(implementation = CommonResponse.class),
//                            examples = {
//                                    @ExampleObject(
//                                            name = "",
//                                            value = """
//                                                    {
//                                                      "success": false,
//                                                      "data": null,
//                                                      "message": "해당 좌표는 우리나라 주소가 아닙니다."
//                                                    }
//                                                    """
//                                    ),
//                                    @ExampleObject(
//                                            name = "",
//                                            value = """
//                                                    {
//                                                      "success": false,
//                                                      "data": null,
//                                                      "message": "좌표는 Double 값으로만 입력해주세요."
//                                                    }
//                                                    """
//                                    ),
//                                    @ExampleObject(
//                                            name = "",
//                                            value = """
//                                                    {
//                                                      "success": false,
//                                                      "data": null,
//                                                      "message": "좌표가 입력되지 않았습니다."
//                                                    }
//                                                    """
//                                    )
//                            }
//                    )
//            )
    })
    @PostMapping("/matrix")
    public ResponseEntity<?> getPos(@RequestBody(description = "Matrix API를 응답받기 위한 requestBody입니다.",
            required = true,
            content = @Content(schema = @Schema(implementation = GoogleMapRequest.class)))
                                    @org.springframework.web.bind.annotation.RequestBody GoogleMapRequest googleMapRequest) {

        GoogleMapResponse response = googleMapService.getMatrix(googleMapRequest);
        return ResponseEntity.ok().body(CommonResponse.success(response, "각 거리 계산이 완료 되었습니다."));
    }
}
