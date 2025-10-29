package com.oath.domain.visitors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class VisitorService {

    private final VisitorRepository visitorRepository;

    public void saveVisitor(String ip, String userAgent){
        LocalDate today = LocalDate.now();

        Long count = visitorRepository.countByIpAdressAndVisitedDate(ip, today);

        if(count == 0) {
            Visitor visitor = Visitor.builder()
                    .ipAddress(ip)
                    .userAgent(userAgent)
                    .build();
            visitorRepository.save(visitor);
        }

    }

    public List<VisitorResponse.PeriodCount> countVisitsGroupByPeriod(VisitorRequest.VisitCountDto request) {

        return visitorRepository.countVisitsGroupByPeriod(request.getStartDate(), request.getEndDate());
    }



}
