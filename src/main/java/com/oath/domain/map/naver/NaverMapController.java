package com.oath.domain.map.naver;

import com.oath.common.CommonResponse;
import com.oath.common.Position;
import com.oath.common.exception.Exception400;
import com.oath.common.exception.Exception401;
import com.oath.document.MapApiResponseExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map/naver")
@Tag(name = "NaverMap Controller", description = "네이버 맵 컨트롤러")
public class NaverMapController {

    private final NaverMapService naverMapService;

    @Operation(summary = "주소를 이용해 좌표로 변환해주는 API입니다.", description = "좌표는 responseBody의 data 필드에 담깁니다.")
    @ApiResponses(value = {
            // 200 OK (성공)
            @ApiResponse(responseCode = "200", description = "좌표 변환이 성공한 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(
                                    name = "정상 응답",
                                    value = MapApiResponseExamples.NaverMap.GEOCODING_SUCCESS_200
                            )
                    )
            ),

            // 400 Bad Request (실패 - 주소 형식이 잘못된 경우)
            @ApiResponse(responseCode = "400", description = "좌표 변환이 실패한 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "주소 형식 오류",
                                            value = MapApiResponseExamples.NaverMap.GEOCODING_MISMATCH_ADDRESS_400
                                    ),
                                    @ExampleObject(
                                            name = "주소 누락",
                                            value = MapApiResponseExamples.NaverMap.GEOCODING_MISSING_ADDRESS_400
                                    )
                            }
                    )
            )
    })
    @GetMapping("/geocoding")
    public ResponseEntity<?> getPos(@Parameter(name = "Address", description = "QueryString에 입력해야 하는 필수 값입니다.", example = "부산시 동래구 명륜1동", required = true) @RequestParam(value = "address", required = false) String address,
                                    @Parameter(name = "Naver Client ID", description = "네이버 API 요청을 위해 꼭 넘어와야 하는 필수 값입니다.", required = true) @RequestHeader(value = "Client-ID", required = false) String clientId) {

        if (clientId == null || clientId.trim().isEmpty())
            throw new Exception401("클라이언트 ID가 올바르지 않습니다.");

        if (address == null || address.trim().isEmpty())
            throw new Exception400("주소가 입력되지 않았습니다.");

        Position position = naverMapService.getPos(address, clientId);
        return ResponseEntity.ok().body(CommonResponse.success(position, "지오코딩을 완료했습니다."));
    }

    @Operation(summary = "좌표를 이용해 주소로 변환해주는 API입니다.", description = "주소는 responseBody의 data 필드에 담깁니다.")
    @ApiResponses(value = {
            // 200 OK (성공)
            @ApiResponse(responseCode = "200", description = "주소 변환이 성공한 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(
                                    name = "정상 응답",
                                    value = MapApiResponseExamples.NaverMap.REVERSE_GEOCODING_SUCCESS_200
                            )
                    )
            ),

            // 400 Bad Request (실패 - 좌표 형식이 잘못된 경우)
            @ApiResponse(responseCode = "400", description = "주소 변환이 실패한 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "지정된 좌표와 우리나라 주소 불일치",
                                            value = MapApiResponseExamples.NaverMap.REVERSE_GEOCODING_MISMATCH_POSITION_400
                                    ),
                                    @ExampleObject(
                                            name = "좌표가 Double 값이 아닌 경우",
                                            value = MapApiResponseExamples.NaverMap.REVERSE_GEOCODING_INVALID_POSITION_FORMAT_400
                                    ),
                                    @ExampleObject(
                                            name = "좌표 누락",
                                            value = MapApiResponseExamples.NaverMap.REVERSE_GEOCODING_MISSING_POSITION_400
                                    )
                            }
                    )
            )
    })
    @GetMapping("/reverse-geocoding")
    public ResponseEntity<?> getAddress(@Parameter(name = "Latitude", description = "QueryString에 입력해야 하는 필수 위도 값입니다.", example = "35.26", required = true) @RequestParam(value = "latitude", required = false) String latitude,
                                        @Parameter(name = "Longitude", description = "QueryString에 입력해야 하는 필수 경도 값입니다.", example = "127.12", required = true) @RequestParam(value = "longitude", required = false) String longitude,
                                        @Parameter(name = "Naver Client ID", description = "네이버 API 요청을 위해 꼭 넘어와야 하는 필수 값입니다.", required = true) @RequestHeader(value = "Client-ID", required = false) String clientId) {


        if (clientId == null || clientId.trim().isEmpty())
            throw new Exception401("클라이언트 ID가 올바르지 않습니다.");

        if (latitude == null || latitude.trim().isEmpty() || longitude == null || longitude.trim().isEmpty())
            throw new Exception400("좌표는 반드시 입력해주세요.");

        Double convertedLatitude = null;
        Double convertedLongitude = null;

        try {
            convertedLatitude = Double.parseDouble(latitude);
            convertedLongitude = Double.parseDouble(longitude);
        } catch (NumberFormatException e) {
            throw new Exception400("좌표는 Double 값으로만 입력해주세요.");
        }

        String address = naverMapService.getAddress(convertedLatitude, convertedLongitude, clientId);
        return ResponseEntity.ok().body(CommonResponse.success(address, "리버스 지오코딩을 완료했습니다."));
    }
}
