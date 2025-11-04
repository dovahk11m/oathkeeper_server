package com.oath.domain.terms;

import com.oath.common.CommonResponse;
import com.oath.domain.terms.dto.TermResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class TermController {

    private final TermService termService;

    @GetMapping
    public ResponseEntity<CommonResponse<List<TermResponse>>> getAllTerms() {
        List<TermResponse> terms = termService.getTermsList();
        return ResponseEntity.ok(CommonResponse.success(terms, "약관 목록 조회 성공"));
    }

    @GetMapping("/{termId}")
    public ResponseEntity<CommonResponse<TermResponse>> getTermDetails(@PathVariable Long termId) {
        TermResponse term = termService.getTermById(termId);
        return ResponseEntity.ok(CommonResponse.success(term, "약관 상세 조회 성공"));
    }
}
