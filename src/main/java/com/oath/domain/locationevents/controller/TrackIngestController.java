// com/oath/domain/locationevents/controller/TrackIngestController.java
package com.oath.domain.locationevents.controller;

import com.oath.domain.locationevents.dto.EventReq;
import com.oath.domain.locationevents.dto.SimpleRes;
import com.oath.domain.locationevents.dto.TrackBatchReq;
import com.oath.domain.locationevents.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/track")
@RequiredArgsConstructor
public class TrackIngestController {

    private final TrackService trackService;

    @PostMapping("/tracks/bulk")
    public ResponseEntity<SimpleRes> bulk(@RequestBody TrackBatchReq req) {
        int stored = trackService.storeTracks(req);
        return ResponseEntity.ok(new SimpleRes(stored));
    }

    @PostMapping("/events")
    public ResponseEntity<Void> event(@RequestBody EventReq req) {
        trackService.storeEvent(req);
        return ResponseEntity.ok().build();
    }
}
