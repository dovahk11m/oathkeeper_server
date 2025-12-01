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



}
