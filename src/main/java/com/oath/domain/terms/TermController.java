package com.oath.domain.terms;

import com.oath.common.CommonResponse;
import com.oath.domain.terms.dto.TermResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Term API", description = "약관 관련 API")
@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class TermController {

    private final TermService termService;

    @Operation(summary = "모든 약관 조회", description = "서비스에 등록된 모든 약관 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<List<TermResponse>>> getAllTerms() {
        List<TermResponse> terms = termService.getTermsList();
        return ResponseEntity.ok(CommonResponse.success(
                terms,
                "약관 목록 조회 성공"
        ));
    }

    @Operation(summary = "특정 약관 상세 조회", description = "ID를 통해 특정 약관의 상세 내용을 조회합니다.")
    @GetMapping("/{termId}")
    public ResponseEntity<CommonResponse<TermResponse>> getTermDetails(@Parameter(description = "조회할 약관의 ID", required = true) @PathVariable Long termId) {
        TermResponse term = termService.getTermById(termId);
        return ResponseEntity.ok(CommonResponse.success(
                term,
                "약관 상세 조회 성공"
        ));
    }
}
