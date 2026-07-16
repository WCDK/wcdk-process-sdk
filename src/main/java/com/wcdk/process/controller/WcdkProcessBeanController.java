package com.wcdk.process.controller;

import com.wcdk.process.common.ApiResponse;
import com.wcdk.process.dto.WcdkProcessConnectionEvent;
import com.wcdk.process.support.ProcessBeanRegistry;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@RestController
@RequestMapping("/wcdk_process")
public class WcdkProcessBeanController {

    private final ProcessBeanRegistry processBeanRegistry;

    public WcdkProcessBeanController(ProcessBeanRegistry processBeanRegistry) {
        this.processBeanRegistry = processBeanRegistry;
    }

    @PostMapping("/{processBeanName}")
    @SuppressWarnings("unchecked")
    public void invoke(@PathVariable String processBeanName,
                                      @RequestBody(required = false) WcdkProcessConnectionEvent request) {
        if(processBeanName.equals("register_bak")){
            System.out.println("流程客户端注册成功"+request.getMessage());
            return;
        }
        WcdkProcessConnectionEvent event = request == null ? new WcdkProcessConnectionEvent() : request;
        if (!StringUtils.hasText(event.getProcessBeanName())) {
            event.setProcessBeanName(processBeanName);
        }
        processBeanRegistry.invoke(processBeanName, event);
    }
}
