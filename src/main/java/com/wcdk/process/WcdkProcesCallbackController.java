package com.wcdk.process;

import com.wcdk.process.dto.WcdkProcesCallbackRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sdk/wcdkproces")
public class WcdkProcesCallbackController {

    private final WcdkProcesCallbackService wcdkProcesCallbackService;

    public WcdkProcesCallbackController(WcdkProcesCallbackService wcdkProcesCallbackService) {
        this.wcdkProcesCallbackService = wcdkProcesCallbackService;
    }

    @PostMapping("/callback")
    public ResponseEntity<Void> callback(@RequestBody WcdkProcesCallbackRequest request) {
        wcdkProcesCallbackService.callback(request);
        return ResponseEntity.ok().build();
    }
}
