package com.oath.domain.visitors;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/visitor")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorService visitorService;
//
//    @GetMapping("/visitor")
//    public String visitorPage() {
//        return "visitor";
//    }

    @PostMapping("/visit")
    public void saveVisitor(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        visitorService.saveVisitor(ip, userAgent);
    }

    @GetMapping("/period")
    public List<VisitorResponse> getVisitorsByPeriod(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<VisitorResponse> visitors = visitorService.countVisitsGroupByPeriod(startDate, endDate);
        return visitors;
    }
}
