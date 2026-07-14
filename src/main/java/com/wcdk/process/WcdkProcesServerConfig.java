package com.wcdk.process;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流程服务配置。
 *
 * @author WCDK
 * @date 2026/7/13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WcdkProcesServerConfig {

    private String baseUrl;
}
