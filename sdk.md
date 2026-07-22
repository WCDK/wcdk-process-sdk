# WCDK Process SDK 使用手册

版本：`wcdk-process-sdk 1.0.0`  
适用项目：Spring Boot 3.5.x / Java 21  
包名：`com.wcdk.process`

## 1. SDK 概述

`wcdk-process-sdk` 用于业务系统接入 WCDK 流程中心。SDK 提供两类能力：

- 客户端自动注册：业务系统启动后自动向流程中心注册 `clientId`、回调地址和本地 `@ProcessBean` 处理器。
- 流程接口调用：通过 `WcdkProcessFlowClient` 调用流程申请、流程实例、任务、模型、部署和客户端管理接口。

SDK 的主要入口类如下：

| 类型 | 类名 | 说明 |
|---|---|---|
| 自动装配 | `WcdkProcessAutoConfiguration` | 根据 `wcdk.process` 配置自动创建 SDK Bean |
| 流程客户端 | `WcdkProcessFlowClient` | 推荐业务代码直接使用的流程方法封装 |
| 通用客户端 | `WcdkProcessClient` | 底层 HTTP 客户端，可调用自定义路径 |
| 回调注解 | `@ProcessBean` | 标记业务系统内的流程回调处理方法 |
| 回调入口 | `WcdkProcessBeanController` | SDK 自动暴露 `/wcdk_process/{processBeanName}` |

## 2. Maven 依赖

在业务系统中引入 SDK：

```xml
<dependency>
    <groupId>com.wcdk.process</groupId>
    <artifactId>wcdk-process-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

SDK 依赖 Java 21，并基于 Spring Boot 自动装配机制加载。

## 3. 配置说明

在业务系统 `application.yaml` 中配置：

```yaml
wcdk:
  process:
    client-id: demo-client
    client-name: wcdk-process-demo
    endpoint: http://localhost:58082
    timeout-seconds: 30
    username: admin
    password: admin123
    callback-url: http://localhost:58083
    auth-flg: WCDK
    active-report: 10
```

| 配置项 | 必填 | 默认值 | 说明 |
|---|---:|---|---|
| `client-id` | 是 | 无 | 业务系统客户端唯一标识 |
| `client-name` | 是 | 无 | 客户端展示名称 |
| `endpoint` | 是 | 无 | WCDK 流程中心服务地址 |
| `username` | 是 | 无 | 调用流程中心接口的 Basic Auth 用户名 |
| `password` | 是 | 无 | 调用流程中心接口的 Basic Auth 密码 |
| `callback-url` | 否 | 空 | 业务系统回调根地址，例如 `http://localhost:58083` |
| `timeout-seconds` | 是 | `30` | HTTP 连接和请求超时时间，单位秒 |
| `active-report` | 是 | `10` | 客户端注册/心跳上报间隔，单位秒，最小值为 1 |
| `auth-flg` | 否 | 空 | 回调认证标识，服务端与客户端约定使用 |

## 4. 自动装配 Bean

引入 SDK 并完成配置后，Spring 容器会自动创建以下 Bean：

| Bean | 类型 | 说明 |
|---|---|---|
| `wcdkProcessConnectionConfig` | `WcdkProcessConnectionConfig` | 客户端连接配置 |
| `wcdkProcessServerConfig` | `WcdkProcessServerConfig` | 流程中心服务端配置 |
| `wcdkProcessHttpClient` | `java.net.http.HttpClient` | SDK 使用的 HTTP 客户端 |
| `wcdkProcessObjectMapper` | `ObjectMapper` | JSON 序列化组件 |
| `wcdkProcessClient` | `WcdkProcessClient` | 通用 HTTP 调用客户端 |
| `wcdkProcessFlowClient` | `WcdkProcessFlowClient` | 流程业务调用客户端 |
| `processBeanRegistry` | `ProcessBeanRegistry` | 扫描和调用 `@ProcessBean` 方法 |
| `wcdkProcessBeanController` | `WcdkProcessBeanController` | 暴露本地回调接口 |
| `wcdkProcessClientAutoRegisterRunner` | `WcdkProcessClientAutoRegisterRunner` | 应用启动后自动注册客户端并定时上报 |

## 5. 快速开始

### 5.1 注入流程客户端

```java
import com.wcdk.process.WcdkProcessFlowClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class DemoProcessService {

    @Resource
    private WcdkProcessFlowClient flowClient;
}
```

### 5.2 定义流程回调处理器

```java
import com.wcdk.process.annotation.ProcessBean;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DemoProcessHandler {

    @ProcessBean("test")
    public void handleTestProcess(Map<String, Object> payload) {
        // payload 为流程中心回调传入的业务数据
    }
}
```

`@ProcessBean` 方法规则：

- `value` 必须填写，且同一应用内不能重复。
- 方法最多只能有一个参数。
- 参数可以是 `WcdkProcessConnectionEvent`、`Map`、`Object`，也可以是自定义 DTO。
- 如果参数是自定义 DTO，SDK 会使用 `ObjectMapper.convertValue(payload, 参数类型)` 转换。
- 应用启动时，SDK 会扫描所有 `@ProcessBean` 方法，并将名称集合注册到流程中心。

### 5.3 回调地址

SDK 会在业务系统中暴露：

```text
POST /wcdk_process/{processBeanName}
拦截器 身份验证等需要放行地址 /wcdk_process/*
```

流程中心调用该地址时，SDK 根据 `processBeanName` 找到本地 `@ProcessBean` 方法并执行。

## 6. 返回包装与异常

流程中心接口统一返回：

```java
ApiResponse<T>
```

字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | `Integer` | 成功时为 `200` |
| `message` | `String` | 返回消息 |
| `data` | `T` | 实际业务数据 |

SDK 内部会自动解包 `data`。当 HTTP 状态码不是 2xx，或 `ApiResponse.code != 200` 时，会抛出：

```java
WcdkProcessClientException
```

调用方需要按业务需要捕获该运行时异常。

分页返回类型：

```java
PageResponse<T>
```

字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `total` | `Long` | 总记录数 |
| `pageNum` | `Long` | 当前页码 |
| `pageSize` | `Long` | 每页大小 |
| `records` | `List<T>` | 当前页数据 |

## 7. `WcdkProcessFlowClient` 方法说明

### 7.1 流程申请

| 方法 | 返回值 | 说明 |
|---|---|---|
| `createProcessRequest(ProcessRequestCreateRequest request)` | `ProcessRequestResponse` | 创建流程申请，可按 `submit` 决定是否立即提交 |
| `submitProcessRequest(Long id)` | `ProcessRequestResponse` | 提交已创建的流程申请 |
| `getProcessRequest(Long id)` | `ProcessRequestResponse` | 查询流程申请详情 |
| `getProcessRequestDiagramDetail(Long id)` | `ProcessDefinitionDetailResponse` | 查询申请对应的流程图详情和当前活动节点 |
| `listProcessRequest(Long pageNum, Long pageSize, String processNo, String starter, String businessTitle, String category, String processDefinitionKey, String status)` | `PageResponse<ProcessRequestResponse>` | 分页查询流程申请 |
| `approveProcessRequest(ProcessRequestApproveRequest request)` | `void` | 审批流程申请当前任务 |
| `deleteProcessRequest(Long id, String deleteReason)` | `void` | 删除流程申请 |

#### `ProcessRequestCreateRequest`

| 字段 | 类型 | 说明 |
|---|---|---|
| `processDefinitionKey` | `String` | 流程定义 Key |
| `taskName` | `String` | 业务标题或任务名称 |
| `formData` | `Map<String, Object>` | 表单数据 |
| `submit` | `Boolean` | 是否创建后立即提交 |
| `processBeanName` | `String` | 绑定的业务回调处理器名称 |

示例：

```java
ProcessRequestCreateRequest request = ProcessRequestCreateRequest.builder()
        .processDefinitionKey("leave_process")
        .taskName("请假申请")
        .formData(Map.of("days", 2, "reason", "年休假"))
        .submit(true)
        .processBeanName("leaveHandler")
        .build();

ProcessRequestResponse response = flowClient.createProcessRequest(request);
```

#### `ProcessRequestApproveRequest`

| 字段 | 类型 | 说明 |
|---|---|---|
| `taskId` | `String` | 当前待办任务 ID |
| `approved` | `Boolean` | 是否通过 |
| `comment` | `String` | 审批意见 |

示例：

```java
flowClient.approveProcessRequest(ProcessRequestApproveRequest.builder()
        .taskId("taskId")
        .approved(true)
        .comment("同意")
        .build());
```

### 7.2 Flowable 流程实例与任务

| 方法 | 返回值 | 说明 |
|---|---|---|
| `startProcess(StartProcessRequest request)` | `ProcessInstanceResponse` | 直接启动流程实例 |
| `getProcessInstance(String processInstanceId)` | `ProcessInstanceResponse` | 查询流程实例 |
| `listTask(String assignee)` | `List<TaskResponse>` | 查询指定办理人的待办任务 |
| `completeTask(TaskCompleteRequest request)` | `void` | 完成任务并提交变量 |
| `deleteProcessInstance(String processInstanceId, String deleteReason)` | `void` | 删除流程实例 |
| `deleteTask(String taskId, String deleteReason)` | `void` | 删除任务 |

#### `StartProcessRequest`

| 字段 | 类型 | 说明 |
|---|---|---|
| `processDefinitionKey` | `String` | 流程定义 Key |
| `businessKey` | `String` | 外部业务主键 |
| `starter` | `String` | 发起人 |
| `processBeanName` | `String` | 回调处理器名称 |
| `variables` | `Map<String, Object>` | 流程变量 |

示例：

```java
ProcessInstanceResponse instance = flowClient.startProcess(StartProcessRequest.builder()
        .processDefinitionKey("leave_process")
        .businessKey("LEAVE-20260720-001")
        .starter("zhangsan")
        .processBeanName("leaveHandler")
        .variables(Map.of("days", 2))
        .build());
```

#### `TaskCompleteRequest`

| 字段 | 类型 | 说明 |
|---|---|---|
| `taskId` | `String` | 任务 ID |
| `variables` | `Map<String, Object>` | 完成任务时提交的流程变量 |

### 7.3 模型管理

| 方法 | 返回值 | 说明 |
|---|---|---|
| `createModel(ModelCreateRequest request)` | `ModelResponse` | 创建流程模型 |
| `updateModel(String modelId, ModelUpdateRequest request)` | `ModelResponse` | 更新流程模型 |
| `listModel()` | `List<ModelResponse>` | 查询全部模型 |
| `listModel(String modelName, String modelKey, String category, String deployed)` | `List<ModelResponse>` | 按条件查询模型 |
| `getModelXml(String modelId)` | `String` | 查询模型 BPMN XML |
| `deployModel(String modelId, String processBeanName)` | `DeploymentResponse` | 部署模型，不指定客户端 |
| `deployModel(String modelId, String clientId, String processBeanName)` | `DeploymentResponse` | 部署模型并绑定客户端回调 |
| `deleteModel(String modelId)` | `void` | 删除模型 |

#### `ModelCreateRequest`

| 字段 | 类型 | 说明 |
|---|---|---|
| `modelName` | `String` | 模型名称 |
| `modelKey` | `String` | 模型 Key |
| `category` | `String` | 分类 |
| `bpmnXml` | `String` | BPMN XML 内容 |

#### `ModelUpdateRequest`

| 字段 | 类型 | 说明 |
|---|---|---|
| `modelName` | `String` | 模型名称 |
| `category` | `String` | 分类 |
| `bpmnXml` | `String` | BPMN XML 内容 |

注意：`deployModel(modelId, clientId, processBeanName)` 要求 `clientId` 与 `processBeanName` 同时为空或同时非空。

### 7.4 设计器导出

| 方法 | 返回值 | 说明 |
|---|---|---|
| `exportDesignerProcess(ProcessDesignerExportRequest request)` | `ProcessDesignerExportResponse` | 将前端设计器节点和连线导出为 BPMN 文件内容 |

#### `ProcessDesignerExportRequest`

| 字段 | 类型 | 说明 |
|---|---|---|
| `format` | `String` | 导出格式 |
| `canvasWidth` | `Integer` | 画布宽度 |
| `canvasHeight` | `Integer` | 画布高度 |
| `nodes` | `List<ProcessDesignerExportNodeRequest>` | 节点集合 |
| `edges` | `List<ProcessDesignerExportEdgeRequest>` | 连线集合 |

#### `ProcessDesignerExportResponse`

| 字段 | 类型 | 说明 |
|---|---|---|
| `fileName` | `String` | 文件名 |
| `contentType` | `String` | 内容类型 |
| `contentBase64` | `String` | Base64 编码后的文件内容 |
| `skippedNodeLabels` | `List<String>` | 导出时跳过的节点标签 |

### 7.5 部署管理

| 方法 | 返回值 | 说明 |
|---|---|---|
| `deployProcess(String deploymentName, String category, String clientId, String processBeanName, Path filePath)` | `DeploymentResponse` | 上传本地 BPMN 文件并部署 |
| `deployProcess(String deploymentName, String category, String clientId, String processBeanName, String fileName, String contentType, byte[] fileContent)` | `DeploymentResponse` | 上传字节数组并部署 |
| `listDeployment()` | `List<DeploymentResponse>` | 查询全部部署 |
| `listDeployment(String deploymentName, String category, String clientId)` | `List<DeploymentResponse>` | 按条件查询部署 |
| `listDeployClient(Long pageNum, Long pageSize, String clientId, String clientName)` | `PageResponse<WcdkProcessClientResponse>` | 查询可用于部署绑定的客户端 |
| `listClientProcessBean(String clientId)` | `List<String>` | 查询客户端注册的 `ProcessBean` 名称 |
| `listProcessDefinition()` | `List<ProcessDefinitionResponse>` | 查询流程定义 |
| `getProcessDefinitionDetail(String processDefinitionId)` | `ProcessDefinitionDetailResponse` | 查询流程定义详情、表单、按钮和流程图结构 |
| `updateDeploymentBinding(String deploymentId, DeploymentBindingUpdateRequest request)` | `void` | 更新部署与客户端处理器的绑定关系 |
| `deleteDeployment(String deploymentId, Boolean cascade)` | `void` | 删除部署 |

#### `DeploymentBindingUpdateRequest`

| 字段 | 类型 | 说明 |
|---|---|---|
| `clientId` | `String` | 客户端 ID |
| `processBeanName` | `String` | 绑定的处理器名称 |

示例：

```java
DeploymentResponse deployment = flowClient.deployProcess(
        "请假流程部署",
        "HR",
        "demo-client",
        "leaveHandler",
        Path.of("processes/leave.bpmn20.xml")
);
```

### 7.6 客户端管理

| 方法 | 返回值 | 说明 |
|---|---|---|
| `listClient(Long pageNum, Long pageSize, String clientId, String clientName, String callbackUrl, String processBeanName, String sortProp, String sortOrder)` | `PageResponse<WcdkProcessClientResponse>` | 分页查询已注册客户端 |
| `detectClient(String clientId)` | `Boolean` | 探测客户端是否可用 |
| `removeClient(String clientId)` | `void` | 移除客户端 |

## 8. `WcdkProcessClient` 通用方法说明

当 `WcdkProcessFlowClient` 没有封装某个接口时，可以使用底层 `WcdkProcessClient` 调用自定义路径。

| 方法 | 说明 |
|---|---|
| `registerClient(Set<String> processBeanNames)` | 向流程中心注册客户端和本地处理器名称 |
| `callback(WcdkProcessConnectionEvent event)` | 向流程中心发送连接/流程事件 |
| `postForVoid(String path, Object body)` | 发送 POST 请求，无返回数据 |
| `post(String path, Object body, Class<T> responseType)` | 发送 POST 请求，按 Class 解析返回数据 |
| `post(String path, Object body, TypeReference<T> responseType)` | 发送 POST 请求，支持泛型返回值 |
| `putForVoid(String path, Object body)` | 发送 PUT 请求，无返回数据 |
| `put(String path, Object body, Class<T> responseType)` | 发送 PUT 请求，按 Class 解析返回数据 |
| `put(String path, Object body, TypeReference<T> responseType)` | 发送 PUT 请求，支持泛型返回值 |
| `get(String path, Class<T> responseType)` | 发送 GET 请求 |
| `get(String path, TypeReference<T> responseType)` | 发送 GET 请求，支持泛型返回值 |
| `get(String path, Map<String, ?> queryParams, Class<T> responseType)` | 发送带查询参数的 GET 请求 |
| `get(String path, Map<String, ?> queryParams, TypeReference<T> responseType)` | 发送带查询参数的 GET 请求，支持泛型返回值 |
| `postMultipart(...)` | 发送 `multipart/form-data` 文件上传请求 |
| `delete(String path, Map<String, ?> queryParams)` | 发送 DELETE 请求 |

通用客户端约定：

- `path` 可以带或不带开头 `/`，SDK 会自动拼接 `endpoint`。
- 请求头自动设置 `Accept: application/json`。
- JSON 请求自动设置 `Content-Type: application/json`。
- 文件上传请求自动设置 `Content-Type: multipart/form-data; boundary=...`。
- 认证头使用 `Authorization: Basic base64(username:password)`。
- 返回值必须符合 `ApiResponse<T>` 结构，SDK 返回其中的 `data`。

## 9. 核心 DTO 字段速查

### 9.1 流程申请返回 `ProcessRequestResponse`

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | `Long` | 申请 ID |
| `processNo` | `String` | 流程编号 |
| `starter` | `String` | 发起人 |
| `taskName` | `String` | 任务名称 |
| `businessTitle` | `String` | 业务标题 |
| `formData` | `Map<String, Object>` | 表单数据 |
| `status` | `String` | 流程状态 |
| `processInstanceId` | `String` | 流程实例 ID |
| `currentTaskId` | `String` | 当前任务 ID |
| `currentTaskName` | `String` | 当前任务名称 |
| `processDefinitionKey` | `String` | 流程定义 Key |
| `processDefinitionId` | `String` | 流程定义 ID |
| `processBeanName` | `String` | 回调处理器名称 |
| `activeNodeIds` | `List<String>` | 当前活动节点 ID |
| `createTime` | `LocalDateTime` | 创建时间 |
| `updateTime` | `LocalDateTime` | 更新时间 |

### 9.2 流程实例返回 `ProcessInstanceResponse`

| 字段 | 类型 | 说明 |
|---|---|---|
| `processInstanceId` | `String` | 流程实例 ID |
| `processDefinitionId` | `String` | 流程定义 ID |
| `processDefinitionKey` | `String` | 流程定义 Key |
| `businessKey` | `String` | 外部业务主键 |
| `processBeanName` | `String` | 回调处理器名称 |
| `suspended` | `Boolean` | 是否挂起 |

### 9.3 任务返回 `TaskResponse`

| 字段 | 类型 | 说明 |
|---|---|---|
| `taskId` | `String` | 任务 ID |
| `taskName` | `String` | 任务名称 |
| `currentTaskName` | `String` | 当前任务名称 |
| `assignee` | `String` | 办理人 |
| `processInstanceId` | `String` | 流程实例 ID |
| `processDefinitionId` | `String` | 流程定义 ID |
| `processRequestId` | `Long` | 关联流程申请 ID |

### 9.4 模型返回 `ModelResponse`

| 字段 | 类型 | 说明 |
|---|---|---|
| `modelId` | `String` | 模型 ID |
| `modelName` | `String` | 模型名称 |
| `modelKey` | `String` | 模型 Key |
| `category` | `String` | 分类 |
| `processBeanName` | `String` | 绑定处理器名称 |
| `version` | `Integer` | 版本 |
| `deploymentId` | `String` | 部署 ID |
| `createTime` | `Date` | 创建时间 |
| `lastUpdateTime` | `Date` | 最后更新时间 |

### 9.5 部署返回 `DeploymentResponse`

| 字段 | 类型 | 说明 |
|---|---|---|
| `deploymentId` | `String` | 部署 ID |
| `deploymentName` | `String` | 部署名称 |
| `fileName` | `String` | 文件名 |
| `category` | `String` | 分类 |
| `deployTime` | `Date` | 部署时间 |
| `clientIds` | `List<String>` | 绑定客户端 ID |
| `clientNames` | `List<String>` | 绑定客户端名称 |
| `processBeanNames` | `List<String>` | 绑定处理器名称 |

### 9.6 流程定义返回 `ProcessDefinitionResponse`

| 字段 | 类型 | 说明 |
|---|---|---|
| `processDefinitionId` | `String` | 流程定义 ID |
| `processDefinitionKey` | `String` | 流程定义 Key |
| `processDefinitionName` | `String` | 流程定义名称 |
| `category` | `String` | 分类 |
| `version` | `Integer` | 版本 |
| `deploymentId` | `String` | 部署 ID |
| `resourceName` | `String` | 资源文件名 |
| `suspended` | `Boolean` | 是否挂起 |
| `clientIds` | `List<String>` | 绑定客户端 ID |
| `clientNames` | `List<String>` | 绑定客户端名称 |
| `processBeanNames` | `List<String>` | 绑定处理器名称 |

`ProcessDefinitionDetailResponse` 在以上字段基础上增加 `deploymentName`、`nodeCount`、`userTaskCount`、`sequenceFlowCount`、`bpmnXml`、`formFields`、`actionButtons`、`nodes`、`sequenceFlows`、`activeNodeIds`。

### 9.7 客户端返回 `WcdkProcessClientResponse`

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | `Long` | 记录 ID |
| `clientId` | `String` | 客户端 ID |
| `clientName` | `String` | 客户端名称 |
| `callbackUrl` | `String` | 回调地址 |
| `authFlg` | `String` | 认证标识 |
| `clientStatus` | `String` | 客户端状态 |
| `processBeanNames` | `List<String>` | 注册的处理器名称 |
| `processNames` | `List<String>` | 绑定流程名称 |
| `processBeanCount` | `Long` | 处理器数量 |
| `processBindingCount` | `Long` | 流程绑定数量 |
| `createTime` | `LocalDateTime` | 创建时间 |
| `updateTime` | `LocalDateTime` | 更新时间 |

## 10. 推荐接入流程

1. 在业务系统中引入 `wcdk-process-sdk`。
2. 配置 `wcdk.process` 连接信息。
3. 定义至少一个 `@ProcessBean` 处理流程回调。
4. 启动业务系统，确认 SDK 自动注册客户端成功。
5. 在流程中心部署流程，并绑定 `clientId` 与 `processBeanName`。
6. 使用 `WcdkProcessFlowClient` 创建申请、提交、审批或查询流程数据。

## 11. 常见问题

### 11.1 启动时报配置校验失败

检查 `client-id`、`client-name`、`endpoint`、`username`、`password` 是否为空。SDK 对这些配置启用了校验。

### 11.2 流程中心无法回调业务系统

检查：

- `callback-url` 是否能被流程中心访问。
- 业务系统是否暴露了 `/wcdk_process/{processBeanName}`。
- `@ProcessBean` 名称是否与部署绑定的 `processBeanName` 一致。
- 如启用了认证标识，确认 `auth-flg` 双方配置一致。

### 11.3 `ProcessBean` 方法无法注册

检查：

- 方法是否位于 Spring 管理的 Bean 中。
- 注解值是否为空。
- 同一应用内是否存在重复的 `@ProcessBean` 名称。
- 方法参数是否超过一个。

### 11.4 调用 SDK 方法抛出 `WcdkProcessClientException`

常见原因：

- 流程中心地址不可达。
- Basic Auth 用户名或密码错误。
- 流程中心接口返回非 2xx 状态码。
- 流程中心返回的 `ApiResponse.code` 不是 `200`。
- 请求参数与服务端接口要求不一致。

