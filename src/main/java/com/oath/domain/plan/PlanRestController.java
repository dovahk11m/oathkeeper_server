package com.oath.domain.plan;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PlanRestController {

    private final PlanService planService;




}
