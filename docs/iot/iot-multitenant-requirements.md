# 企业级多租户物联网管理平台 需求规格说明书（SRS）

文档版本：v1.0
状态：草案（待评审）
最后更新：2025-08-14
适用阶段：需求冻结前评审、设计/实现/测试依据

## 1. 文档信息
- 文档类型：系统需求规格说明书（SRS）
- 读者对象：产品经理、架构师、后端/前端/移动/数据工程、测试、运维、安全与合规
- 术语
  - 租户：平台内独立的业务实体与数据边界
  - 产品/物模型：设备能力抽象（属性/事件/服务）
  - 设备三元组：阿里云设备接入的 ProductKey、DeviceName、DeviceSecret
  - 设备影子：设备状态的云端缓存
  - 规则引擎：IoT数据流转/处理/转发能力

## 2. 背景与目标
- 背景：利用阿里云 IoT Platform 提供的设备接入、物模型、规则引擎、设备影子、OTA 等核心能力，构建面向多租户的企业级物联网管理平台。
- 总体目标：提供端到端设备生命周期管理、可视化监控、规则自动化、告警通知、远程指令与OTA、数据开放与集成；在多租户模式下实现强隔离、安全合规、可计量计费、可持续扩展。
- 业务动因：
  - 多行业/多客户共用平台，要求数据强隔离与差异化策略配置。
  - 通过阿里云 IoT Platform 降低设备接入与消息收发的自建成本与风险。
- 成功度量（KPI）：
  - T0：首批3租户、共≥5万设备接入，连续30天平台可用性≥99.9%。
  - T1：半年内支持≥10个租户、≥20万设备，重大事故（SEV1）为0。
  - 效率：常见运维操作（批量注册、OTA、规则变更）P95≤3分钟完成。
- 价值目标：
  - 缩短新租户上线周期至≤3个工作日（含资源开通与基线策略）。
  - 降低运维人力/设备比至≤1:5000。

## 3. 范围与不在范围
- 范围（本期必须交付）：
  - 多租户隔离、RBAC/ABAC、设备/产品/物模型、告警/规则、OTA、API/Webhook、数据查询/导出、审计。
- 不在范围：
  - UI视觉稿/交互原型细节、具体数据库/中间件选型、底层网络/机房规划。
- 前置依赖：
  - 阿里云账号与 IoT Platform 实例可用，目标地域/可用区已评估配额。

## 4. 角色与使用者
- 系统管理员（平台级）：
  - 目标：平台运营、安全与合规、资源与配额管理、租户全局审计。
  - 能力：创建/审核租户、配置全局策略、紧急停服与公告。
- 租户管理员：
  - 目标：本租户组织、用户与资源的自治管理。
  - 能力：组织/角色/成员、产品与设备、规则与告警、API密钥管理。
- 运营/运维：
  - 目标：保障设备稳定在线、快速定位与处置告警与故障。
  - 能力：批量操作、批量指令/OTA、规则优化、健康度巡检。
- 开发者：
  - 目标：对接API/SDK、集成Webhook，构建行业应用。
  - 能力：使用AK/SK或OAuth2、订阅事件、调用查询与控制接口。
- 审计/只读：
  - 目标：满足合规检查与监管报送；无变更权限。
- 设备（机对机）：通过阿里云 IoT 平台进行数据上报与接收指令。

## 5. 多租户模型与隔离要求
- 数据隔离：所有核心实体均带 tenantId；查询/写入必须校验租户域；跨租户不可见。
- 身份隔离：用户与API凭据在租户域内生效；平台管理员具备跨租户权限仅限运维场景使用。
- 资源配额：租户级设备数、消息TPS、存储容量/保留期、API速率、Webhook并发；配额超限可触发软/硬阈值告警、限流/拒绝、临时提升申请。
- 可视化隔离：仪表盘、报表、规则、告警策略、API密钥等均为租户作用域。
- 租户生命周期：审核通过→启用→运行中→到期预警→禁用→删除（含数据保留/导出）。
- 计量项：在线设备数、上下行条数与带宽、存储容量、API调用量、规则执行次数、OTA流量。

## 6. 与阿里云 IoT Platform 的集成要求
- 接入与认证：设备采用三元组或双向TLS证书接入，支持MQTT/HTTPS/CoAP；接入域名与实例信息由平台统一管理与分发。
- 物模型与产品：在阿里云侧创建产品与物模型，并在平台内一一映射与版本记录。
- 设备影子：平台提供影子读写与部分更新接口，定义冲突处理规范。
- 规则引擎数据流转：支持按租户将上行数据流转至SLS/TSDB/OTS/MQ/FC/RDS等目标；支持多目标、灰度与回滚，并提供变更审计。
- OTA：使用阿里云OTA任务能力，平台提供任务编排、分批策略与进度可视化。
- 幂等与一致性：跨云操作使用幂等键；失败重试与补偿；最终一致性窗口≤1分钟。

## 7. 功能性需求

### 7.1 租户与组织管理
- 租户生命周期：支持创建/审核/启用/禁用/删除/续期，提供到期前N天自动提醒与续期申请流程；禁用后所有设备与API请求立即阻断但保留数据查询（可配置）。
- 配额管理：可为租户设置设备数、消息TPS、存储容量/保留期、API速率、Webhook并发等配额；达到软阈值预警，超硬阈值按策略限流/拒绝，并支持临时提升申请与审核。
- 组织结构：租户内支持部门/项目/分组三级结构，用于资源归属与权限边界；支持按组织维度统计与报表。
- 白名单/黑名单：支持来源IP、地域白名单/黑名单；命中黑名单的请求直接拒绝并记录审计。
- 数据保留期策略：可按租户设置遥测/日志类数据保留期与归档策略（受平台上限约束）。
- 租户标签：可添加自定义标签用于ABAC策略与报表分组。

### 7.2 用户与权限（RBAC/ABAC）
- 认证：支持账号密码登录，强制密码复杂度与过期策略，可启用MFA；支持会话管理与强制下线。
- 单点登录（可选）：支持OIDC/SAML对接，映射至平台角色。
- 授权：RBAC定义角色→权限集合（资源+操作）；ABAC基于资源标签/地域/组织等属性做条件控制；提供最小权限角色模板。
- 审批流（可选）：对敏感操作（如大批量删除、停服、密钥导出）支持二人复核与审计留存。

### 7.3 产品与物模型管理
- 产品定义：创建/更新/禁用产品，记录协议类型与物模型版本；同一租户内ProductKey命名规范校验。
- 物模型：支持属性/事件/服务编辑，草稿→校验→发布流程；提供模型校验报告与示例载荷。
- 版本与兼容：支持版本对比与影响分析，标记向后兼容/不兼容，禁止对运行中设备造成破坏性变更。
- 模板库：提供常见设备模板（可复制），减少建模成本。

### 7.4 设备生命周期管理
- 批量注册/导入/删除/冻结；模板校验与错误报告；支持按标签/分组自动归属。
- 状态管理：激活/在线/离线/冻结/退网；展示最后上线时间与原因码。
- 标签与位置：支持多标签、站点/经纬度与地理围栏绑定，围栏事件可用于规则条件。
- 密钥安全：支持设备密钥轮换与吊销，泄露处置流程与审计。

### 7.5 设备连接与通信
- 上行：遥测与事件上报；下行：服务调用与属性设置；统一消息格式、编码与签名约定。
- QoS/离线：QoS策略、离线消息队列大小与过期策略可配；断线重连退避策略统一规范。
- 限流与异常：按租户/产品/设备维度限流；异常消息进入死信队列并支持重放与告警。
- 幂等：提供幂等键规范，防止重复处理。

### 7.6 实时监控与可视化
- 仪表盘：租户级总览（在线设备数、消息量、错误率、健康度），支持时间粒度与分产品筛选。
- 设备详情：实时属性、事件时间线、地理位置、最近告警与健康度。
- 自定义看板：图表拖拽配置，可保存/共享至租户内指定用户/角色。

### 7.7 规则引擎与自动化
- 编排：条件（属性阈值/事件/地理围栏/时序模式）→ 动作（通知、指令、数据转发、触发函数）。
- 调试与发布：提供沙箱调试与事件回放，规则版本化与灰度发布（按设备分组/比例）。
- 可靠性：并发控制、幂等处理、失败重试与退避、死信队列与人工干预通道。

### 7.8 告警与通知
- 策略类型：阈值、异常检测（如突变/漂移）、离线超时、固件失败、规则失败等。
- 通知渠道：邮件/短信/IM(Webhook)/电话（可选）；支持多渠道聚合、模板变量与多语言。
- 升级与抑制：支持告警升级链路、维护时段抑制、去重与合并策略；支持值班表。
- 工单：可对接工单系统，追踪SLA与处置记录。

### 7.9 远程控制与指令下发
- 调度：单设备/批量下发，执行时间窗与超时控制；支持窗口外自动取消或延期策略。
- 可靠性：指令幂等、顺序性保障、回执与状态机（已接受/执行中/成功/失败/超时）。
- 风控：敏感指令二次确认（含原因输入），全量审计与回滚预案。

### 7.10 OTA 升级
- 包管理：固件/脚本/配置包管理，SHA256/签名校验与完整性验证。
- 策略：按设备筛选/分组/地域/在线状态分批灰度；失败自动回滚或人工回滚。
- 管控：带宽限速与维护时间窗；任务暂停/恢复；失败原因分类与统计报表。

### 7.11 设备影子与状态缓存
- 能力：影子文档读写、部分更新、版本号控制；影子与物模型字段一致性校验。
- 冲突处理：版本冲突检测与重试策略；期望值与报告值差异对齐。
- 生命周期：影子TTL与持久化策略可配置。

### 7.12 数据采集、存储与查询
- 数据类型：遥测时序、事件日志、属性快照、设备操作日志。
- 存储分层：热/冷数据分层存储与保留期（租户级可配，上限受平台约束）。
- 查询：按设备/产品/租户/时间/字段过滤，分页与排序，支持索引；复杂查询提供异步任务。
- 导出：CSV/Parquet 异步导出与结果下载；导出操作纳入审计与配额。

### 7.13 开放 API/SDK 与鉴权
- API：REST（/v1）为主，可选gRPC；统一鉴权、签名与时间戳，回放防护；统一错误码与traceId。
- 鉴权：OAuth2/OIDC 与 AK/SK 并存；最小权限与IP白名单；密钥轮换与吊销。
- 治理：速率限制与配额（全局/租户/用户/AK），分页（游标/偏移）规范与重试建议。
- SDK：至少1种服务端语言与1种前端语言，封装签名/重试/分页与示例工程。

### 7.14 Webhook/消息推送
- 事件订阅：设备上线/下线、告警、规则命中、OTA状态等；支持租户自定义事件过滤。
- 安全：HMAC签名，时间戳与重放防护；回调地址健康检查。
- 可靠性：重试退避、去重幂等、送达回执统计、死信队列与人工补偿。
- 管理：订阅管理、测试面板、失败样例与诊断日志。

### 7.15 报表与分析
- 指标：在线率、消息量、失败率、告警统计、OTA成功率、地理/设备分布等。
- 模板与定时：租户级报表模板，参数化查询，定时邮件/IM推送与导出。

### 7.16 计量与计费（可选）
- 计量项：设备数、消息条数/带宽、存储占用、API调用、规则执行次数、OTA流量。
- 账务：账期/对账、阈值提醒、逾期停服策略（含白名单豁免流程）。

### 7.17 审计与合规
- 覆盖范围：关键操作（增删改、权限变更、密钥操作、规则/OTA/数据导出等）全量审计。
- 留存与访问：审计日志留存周期可配，访问控制与脱敏；提供不可抵赖与完整性保护。
- 合规：等保/隐私/GDPR（如适用）条款映射与最小实现清单。

## 8. 非功能性需求（NFR）
- 可用性/容灾：可用性≥99.9%，RPO≤10分钟，RTO≤30分钟；区域级降级策略与切换预案。
- 性能/容量：设备≥100万、单租户≤10万（可配额）；上行稳定≥50k TPS、峰值≥100k TPS（P95≤3s）；下行P95≤2s；告警触达P95≤30s；7天热数据查询P95≤2s。
- 安全：TLS传输、数据加密（存储/备份/导出）、密钥轮换、最小权限、细粒度审计、反爬/防刷/限流。
- 可维护性/运维：健康检查、配置中心、特性开关、灰度/回滚、Runbook与自动化巡检、容量预测。
- 可观测性：指标、日志、分布式追踪、SLO/SLA仪表盘与错误预算管理。
- 国际化/本地化：多语言、多时区、单位制切换。

## 9. 数据与权限模型（需求层）

字段约定
- 基本类型：string/int32/int64/float64/bool/datetime/json/map<string,string>
- 规范：
  - id字段统一为string（UUID/ULID），时间使用ISO8601（UTC），金额/配额类使用int64（单位在字段说明中注明）
  - 字段标注：必填/可选、唯一/索引、取值范围/枚举、掩码/加密/脱敏要求

### 9.1 租户 Tenant
- tenantId: string，必填，UUID，唯一与索引（主键）
- name: string，必填，1–100字符，唯一（平台范围或同账号范围）
- status: string，必填，枚举[PENDING, ACTIVE, SUSPENDED, DISABLED]
- quota: json，必填，{deviceLimit:int64, msgTps:int64, storageGB:int64, apiRps:int64, webhookConcurrent:int32, hotRetentionDays:int32, coldRetentionDays:int32}
- expireAt: datetime，可选，到期时间；到期后进入SUSPENDED策略
- contact: json，可选，{owner:string, email:string, phone:string}
- billingPlan: string，可选，计费套餐标识
- tags: map<string,string>，可选，≤20对；用于ABAC与报表
- auditPolicy: json，可选，审计留存与级别策略
- createdAt/updatedAt: datetime，必填
- createdBy/updatedBy: string，必填/可选
- isDeleted: bool，必填（软删除标记），默认false
约束与索引：tenantId PK；name 唯一；status/expireAt 索引；tags.key 索引（前缀）

### 9.2 用户 User
- userId: string，必填，UUID，唯一（主键）
- tenantId: string，必填，FK→Tenant.tenantId（索引）
- username: string，必填，唯一（tenant内）
- email/phone: string，可选，唯一（tenant内），格式校验
- passwordHash: string，必填（SSO用户可为空），加密存储
- mfaEnabled: bool，必填，默认false
- mfaSecretRef: string，可选，密钥引用（不直接存密钥明文）
- roles: json，必填，string[]（roleId列表）
- status: string，必填，枚举[ACTIVE, LOCKED, DISABLED]
- lastLoginAt: datetime，可选
- loginFailCount: int32，必填，默认0；与锁定策略关联
- requirePasswordChange: bool，必填，默认false
- ssoSubject: string，可选，SSO主体ID
- createdAt/updatedAt/createdBy/updatedBy: 同上
约束与索引：tenantId+username 唯一；email/phone 唯一（tenant内）；tenantId 索引

### 9.3 角色与权限 Role/Permission（RBAC+ABAC）
- roleId: string，必填，UUID，唯一（主键）
- tenantId: string，必填，索引
- name: string，必填，唯一（tenant内）
- description: string，可选
- type: string，必填，枚举[SYSTEM, TENANT]
- permissions: json，必填，Permission[]；
  - Permission: {resource:string, action:string, condition:string?}
    - resource 示例：tenant|user|role|product|model|device|shadow|rule|alarm|ota|data|report|apikey|webhook
    - action 示例：read|write|delete|execute|publish|export|approve|admin
    - condition：ABAC 条件表达式（如 "device.tags['project']=='X' && geo in ['CN','SG']"）
- createdAt/updatedAt/createdBy/updatedBy: 同上
约束与索引：tenantId+name 唯一；tenantId 索引

### 9.4 产品 Product
- productKey: string，必填，唯一（主键或具备全局唯一约束），与阿里云一致
- tenantId: string，必填，索引
- name: string，必填，1–100字符
- categoryId: string，必填，FK→ProductCategory.categoryId（阿里云“产品分类”映射）
- categoryType: string，必填，枚举[STANDARD, CUSTOM]
- nodeType: string，必填，枚举[DIRECT, GATEWAY, SUB_DEVICE]（直连/网关/子设备）
- authType: string，必填，枚举[SECRET, X509, NO_AUTH]（密钥/证书/免认证）
- allowDynamicRegister: bool，可选，是否启用设备动态注册（阿里云产品级能力）
- productSecretRef: string，可选，动态注册所需 productSecret 的安全引用
- networkTypes: json，可选，string[]，枚举[WiFi,Ethernet,Cellular,NB-IoT,LoRa,BLE,ZigBee,Other]
- protocol: string，必填，枚举[MQTT, HTTPS, CoAP, LwM2M, Modbus, OPC_UA, Custom]
- protocolAdapterId: string，可选，FK→ProtocolAdapter.adapterId（协议适配器插件）
- dataFormat: string，必填，枚举[JSON, BINARY, PROTOBUF, AVRO, CUSTOM]
- status: string，必填，枚举[ENABLED, DISABLED]
- logoUrl: string，可选；manufacturer:string 可选；industry:string 可选；region:string 可选
- dtlsPSKEnabled: bool，可选，是否启用CoAP DTLS PSK；pskIdentityPolicy:string 可选
- mqttKeepaliveSec: int32，可选；maxMsgSizeKB:int32 可选；qosSupport: json 可选（{qos0:boolean,qos1:boolean}）
- deviceCountLimit:int64 可选（产品级设备数限制，覆盖租户配额的子上限）

- description: string，可选
- tags: map<string,string>，可选
- iotInstanceId: string，可选，阿里云实例映射
- createdAt/updatedAt: datetime
- topicTemplateVersion: string，可选，关联TopicTemplate.version
- gatewayConfig: json，可选，{supportsSub:boolean, autoDiscovery:boolean, topology:'STATIC'|'DYNAMIC'}
- modelVersion: string，必填，semver（当前使用的物模型版本）
- lifecycle: string，必填，枚举[PLANNING, DEV, TEST, PUBLISHED, OFFLINED, ARCHIVED]

约束与索引：productKey 唯一；tenantId+name 索引；categoryId 索引；protocolAdapterId 索引；tags.key 前缀索引

### 9.4.1 产品分类 ProductCategory（新增）

- dataParsingScriptRef: string，可选，数据格式转换/解析脚本引用（与规则引擎联动）
- payloadEncryption: json，可选，{enabled:boolean, alg:'AES256'|'SM4'|'CUSTOM'}

- categoryId: string，必填，UUID，唯一
- tenantId: string，可选（平台级标准分类可为空），索引
- name: string，必填，唯一（同级）
- parentId: string，可选，FK→ProductCategory.categoryId，形成树
- type: string，必填，枚举[STANDARD, CUSTOM]
- templateModelId: string，可选，FK→Model.modelId（标准分类可绑定模板）
- description: string，可选；tags: map<string,string> 可选
- createdAt/updatedAt: datetime
约束与索引：parentId 索引；tenantId+name 唯一（同级）


- standardTSLTemplateRef: string，可选，指向 ModelTemplate（行业标准模板）

### 9.4.2 协议适配器 ProtocolAdapter（新增）
- adapterId: string，必填，UUID
- tenantId: string，可选（平台内置可为空），索引
- name: string，必填，唯一（tenant内）
- supports: json，必填，{protocols:[MQTT,CoAP,HTTP,Modbus,...], formats:[JSON,BINARY,PROTOBUF,...]}
- version: string，必填
- artifact: json，必填，{type:'container'|'jar'|'wasm'|'script', location:string, checksum?:string}
- health: json，可选，{status:'HEALTHY'|'DEGRADED'|'DOWN', lastCheckAt:datetime}
- createdAt/updatedAt: datetime
约束与索引：tenantId+name 唯一；protocols/ formats 可建立倒排索引（视实现）

### 9.4.3 Topic 模板 TopicTemplate（新增）
- templateId: string，必填，UUID

- authzMatrix: json，可选，Topic 权限矩阵（上行/下行/属性/事件/服务），支持通配符与前缀

- version: string，必填
- productKey: string，可选（通用模板为空）
- topics: json，必填，[{topic:string, direction:'up'|'down', qos:int32, desc:string}]
- createdAt/updatedAt: datetime
约束与索引：productKey+version 唯一；topic 前缀索引

### 9.5 物模型 Model（按产品版本）
- modelId: string，必填，UUID（或 productKey+version 复合唯一）
- productKey: string，必填，索引
- version: string，必填，semver，唯一（按产品），保证同产品仅1个PUBLISHED版本
- schema: json，必填，完整TSL（属性/事件/服务定义），支持TSL标准类型与扩展（数值/枚举/结构体/数组/地理位置等）

- supportsDynamicLifecycle: bool，可选，是否允许模板驱动生成产品物模型
- templateVariables: json，可选，模板变量定义（名称/类型/默认值/校验）

- properties: json，必填，[{identifier,name,type,unit,precision,min,max,step,access('r'|'w'|'rw'),desc,required:boolean,default:any}]
- events: json，必填，[{identifier,level('INFO'|'WARN'|'ERROR'),type('info'|'alert'|'fault'),params:[{name,type,unit?}],desc}]
- services: json，必填，[{identifier,name,input:[{name,type,unit?,required?}],output:[{name,type,unit?}],async:boolean,timeoutMs:int32,desc}]
- validationRules: json，可选，字段校验规则（范围、正则、必填、互斥等）
- migrationNotes: string，可选，版本变更说明与影响范围（设备数/规则/告警）
- compatibleWith: json，可选，兼容版本列表
- status: string，必填，枚举[DRAFT, PUBLISHED, DEPRECATED]
- createdAt/updatedAt/createdBy/updatedBy: 同上
约束与索引：productKey+version 唯一；identifier 在各自集合内唯一；状态约束：同product仅允许一个PUBLISHED版本

### 9.5.1 物模型模板 ModelTemplate（新增）
- templateId: string，必填，UUID
- scope: string，必填，枚举[PLATFORM, TENANT]（平台级/租户级模板）
- name: string，必填，唯一（按scope+tenant）
- industry: string，可选（行业分类），如智能家居/工业/农业等
- version: string，必填，semver
- tsl: json，必填，TSL定义（属性/事件/服务），支持占位与片段复用
- presets: json，可选，预置属性/事件/服务清单
- validationRules: json，可选，字段校验规则
- status: string，必填，枚举[ACTIVE, DEPRECATED]
- createdAt/updatedAt/createdBy/updatedBy: 同上
约束与索引：scope+name 唯一；industry 索引；tenantId（当scope=TENANT时）索引

### 9.6 设备 Device
- deviceName: string，必填，唯一（按 productKey 范围内）
- productKey: string，必填，索引
- tenantId: string，必填，索引
- iotDeviceSecretRef: string，可选，密钥引用；authType: string，可选，枚举[SECRET,X509,NO_AUTH]
- status: string，必填，枚举[INACTIVE, ONLINE, OFFLINE, FROZEN, MAINTENANCE, DECOMMISSIONED]
- lastOnlineAt: datetime，可选；lastIp: string，可选；rssi:int32 可选
- firmwareVersion: string，可选；hardwareVersion: string 可选
- tags: map<string,string>，可选；labels ≤50
- location: json，可选，{lat:float64, lon:float64, accuracy:float32}
- groupIds: json，可选，string[]（设备分组，多对多）
- topology: json，可选，{parentGateway?:{productKey,deviceName}, children?:[{productKey,deviceName}]}
- geofenceId: string，可选；geofenceState:string 可选（IN/OUT/UNKNOWN）
- certId: string，可选；caId:string 可选（证书CA）
- connectivity: json，可选，{networkType, ip, apn?, imei?, iccid?}
- createdAt/updatedAt: datetime
约束与索引：productKey+deviceName 唯一；tenantId/productKey/deviceName 索引；tags.key 前缀索引；status/lastOnlineAt 索引；groupIds 包含索引（视数据库能力）

### 9.6.1 设备分组 DeviceGroup（新增）
- groupId: string，必填，UUID
- tenantId: string，必填，索引
- name: string，必填，唯一（tenant内同级）
- parentId: string，可选，FK→DeviceGroup.groupId
- type: string，必填，枚举[STATIC, DYNAMIC]
- dynamicQuery: string，可选（当type=DYNAMIC，保存选择器/表达式）
- description: string，可选；tags: map<string,string> 可选
- createdAt/updatedAt: datetime
约束与索引：tenantId+parentId+name 唯一；type 索引

### 9.6.2 设备拓扑 DeviceTopology（新增）
- parent: {productKey,deviceName}，必填（网关）
- child: {productKey,deviceName}，必填（子设备）
- relation: string，必填，枚举[GATEWAY_CHILD]
- createdAt/updatedAt: datetime
约束与索引：PK(parent,child)；parent 索引；child 索引

### 9.6.3 设备凭据 DeviceCredential（新增）
- productKey, deviceName：必填，复合索引
- authType: string，必填，枚举[SECRET, X509]
- secretRef: string，可选（密钥引用）
- certId: string，可选；caId: string 可选
- validFrom/validTo: datetime，可选
- status: string，必填，枚举[ACTIVE, REVOKED, EXPIRED]
- createdAt/updatedAt
约束与索引：PK(productKey,deviceName,authType)；status 索引；validTo 索引

### 9.6.4 证书颁发机构 CA（新增）
- caId: string，必填，UUID
- tenantId: string，必填/可选（平台级或租户级），索引
- name: string，必填，唯一（tenant内）
- certPem: string，必填；crlUrl: string 可选
- status: string，必填，枚举[ACTIVE, REVOKED]
- createdAt/updatedAt
约束与索引：tenantId+name 唯一；status 索引


### 9.7 设备影子 Shadow
- productKey: string，必填
- deviceName: string，必填
- version: int64，必填，影子版本号
- reported: json，必填，设备上报状态
- desired: json，可选，期望状态
- delta: json，可选，差异缓存
- eTag: string，可选，并发控制标记
- ttlSeconds: int32，可选，影子过期
- updatedAt: datetime，必填
约束与索引：PK(productKey, deviceName)；updatedAt 索引；version 单调递增校验

### 9.8 规则 Rule
- ruleId: string，必填，UUID，唯一
- tenantId: string，必填，索引
- name: string，必填，唯一（tenant内）
- status: string，必填，枚举[ENABLED, DISABLED]
- version: int32，必填，自增
- triggerType: string，必填，枚举[PROPERTY, EVENT, GEOFENCE, SCHEDULE]
- condition: string，必填，表达式（CEL/SQL-like），含去抖/窗口参数
- actions: json，必填，[{type, config(json)}]；type：notify|command|route|function
- concurrency: int32，可选；retryPolicy: json，可选；dedupKeyTpl: string，可选
- dlqRef: string，可选，死信主题引用
- createdAt/updatedAt/createdBy/updatedBy: 同上
约束与索引：tenantId+name 唯一；status/triggerType 索引

### 9.9 告警 Alarm
- alarmId: string，必填，UUID
- tenantId: string，必填，索引
- ruleId: string，可选，索引
- severity: string，必填，枚举[P1,P2,P3,P4]
- state: string，必填，枚举[OPEN, ACK, RESOLVED, SUPPRESSED]
- deviceRef: json，可选，{productKey,deviceName}
- firstSeenAt/lastSeenAt: datetime，必填
- occurrences: int64，必填，累计次数
- assignees: json，可选，处理人列表
- notifications: json，可选，通知记录
- closeReason/closedBy/closedAt: 可选
约束与索引：tenantId+state+severity 组合索引；lastSeenAt 索引

### 9.10 OTA（任务与固件）
固件 Firmware
- firmwareId: string，必填，UUID
- tenantId: string，必填，索引
- productKey: string，必填，索引（固件可按产品维度管理）
- version: string，必填，semver，唯一（tenant内+productKey）
- fileUrl: string，必填；sizeBytes: int64，必填；sha256: string，必填；signature: string，可选；signatureAlg:string 可选
- releaseNotes: string，可选；createdAt/updatedAt: datetime
任务 OTAJob
- otaJobId: string，必填，UUID
- tenantId: string，必填，索引
- productKey: string，必填，索引
- targetSelector: json，必填（按标签/分组/地域/在线状态）
- batchPolicy: json，必填（批次大小/间隔/灰度比例）
- rateLimit: json，可选，{bandwidthKbps:int32, qps:int32}
- maintenanceWindow: json，可选，{start:string,end:string,timezone:string}
- status: string，必填，枚举[PENDING,RUNNING,PAUSED,COMPLETED,FAILED,ROLLED_BACK]
- progress: json，必填，{total,success,failed,inProgress}
- rollbackStrategy: string，可选
- failReasons: json，可选，[{code:string,msg:string,count:int64}]
约束与索引：tenantId+status 索引；otaJobId 唯一；firmwareId 唯一；productKey 索引

### 9.11 审计 Audit
- auditId: string，必填，UUID
- tenantId: string，可选（平台级操作可为空），索引
- actorId: string，必填；actorType: string，必填，枚举[USER,SYSTEM,DEVICE]
- action: string，必填；resourceType/resourceId: string，必填
- requestId/traceId: string，可选；clientIp/userAgent: string，可选
- result: string，必填，枚举[SUCCESS, FAIL]；errorCode/errorMsg: 可选
- evidenceHash: string，可选；signature: string，可选（不可抵赖）
- before/after: json，可选（敏感字段脱敏/摘要化）
- createdAt: datetime，必填
约束与索引：tenantId+createdAt 索引；resourceType+resourceId 索引；actorId 索引

### 9.12 索引与唯一性要求（汇总）
- 强唯一：tenant.tenantId, user.userId, role.roleId, product.productKey, rule.ruleId, alarm.alarmId, otaJob.otaJobId, firmware.firmwareId
- 复合唯一：productKey+deviceName；productKey+model.version；tenantId+role.name；tenantId+user.username
- 常用索引：tenantId、status、updatedAt/createdAt、lastSeenAt、tags.key（前缀）、resourceType+resourceId

### 9.13 权限模型约束
- 最小权限：所有API默认拒绝，需明确授权（RBAC）
- ABAC条件：支持基于 tenantId、组织、标签、地域 的细粒度限制
- 审计覆盖：对create/update/delete/approve/execute/export 等高风险操作强制审计

### 9.14 数据保留与一致性
- 数据保留：遥测热数据默认30天；冷数据归档至对象存储；租户可配（受平台上限约束）
- 幂等与版本：跨服务操作必须提供幂等键；影子与指令采用版本/回执机制；最终一致性容忍窗口≤1分钟（指令类除外）


### 9.15 Topic 命名与权限矩阵规范（对齐阿里云）
- 命名示例（可按阿里云标准Topic调整/扩展）：
  - 上行属性：/sys/{productKey}/{deviceName}/thing/property/post
  - 上行事件：/sys/{productKey}/{deviceName}/thing/event/{eventId}/post
  - 下行服务应答：/sys/{productKey}/{deviceName}/thing/service/{serviceId}/reply
  - 下行服务调用：/sys/{productKey}/{deviceName}/thing/service/{serviceId}
  - 影子更新：/sys/{productKey}/{deviceName}/thing/shadow/update
  - 自定义扩展：/ext/{domain}/{productKey}/{deviceName}/...
- 变量与校验：productKey/deviceName 必须匹配注册信息；serviceId/eventId 与物模型一致；禁止保留关键字冲突。
- 通配：支持 MQTT 通配符（订阅侧）“+”和“#”（授权需限制范围）。
- QoS 建议：属性/事件上行默认 QoS1；大吞吐可评估 QoS0；关键控制与应答使用 QoS1。
- 与授权矩阵（authzMatrix）关系：
  - 将“方向/类型（属性/事件/服务/影子）”映射到可发布(PUB)/可订阅(SUB)集合；
  - 支持前缀授权（/sys/{pk}/{dn}/thing/property/#）与精确授权并存；
  - 不同角色（设备、租户服务、平台运维）应用不同授权模板。

### 9.16 动态注册流程与安全
- 适用前提：产品开启 allowDynamicRegister=true，配置 productSecretRef。
- 流程概述：
  1) 设备持 productKey 与签名材料请求动态注册；
  2) 平台校验签名（基于 productSecret），生成 DeviceSecret 并返回；
  3) 设备以三元组（productKey/deviceName/deviceSecret）重新建链；
  4) 平台按策略（限流/黑白名单/地域/指纹）准入与审计。
- 安全要求：
  - 限制 deviceName 策略（如设备指纹/序列号白名单）；
  - 防重放：时间戳+随机数+签名；
  - 阈值与告警：异常失败/爆破自动告警；
  - productSecret 轮换计划与生效窗口；
  - 审计记录字段：请求源IP、UA、签名校验结果、下发DeviceSecret哈希（不回传明文）。

### 9.17 物模型 TSL 与模板变量示例
- 属性示例（properties）：
  - { identifier:"temperature", name:"温度", type:"float", unit:"℃", precision:1, min:-40, max:125, access:"r", required:true }
  - { identifier:"switch", name:"开关", type:"bool", access:"rw", default:false }
- 事件示例（events）：
  - { identifier:"overheat", level:"WARN", type:"alert", params:[{name:"value", type:"float", unit:"℃"}] }
- 服务示例（services）：
  - { identifier:"setBrightness", async:false, timeoutMs:3000, input:[{name:"level", type:"int", min:0, max:100}], output:[] }
- 模板变量（templateVariables）示例：
  - { name:"TEMP_RANGE_MAX", type:"int", default:125, validate:"<=150" }
  - { name:"SUPPORT_GEOFENCE", type:"bool", default:false }
- 生成规则：模板渲染时将变量替换到 TSL 的范围/单位/是否包含某些属性或服务；发布前提供“渲染预览+校验报告”。


### 9.18 Topic 授权模板示例
- 设备侧（Device Credential）默认授权：
  - PUB：/sys/{pk}/{dn}/thing/property/post
  - PUB：/sys/{pk}/{dn}/thing/event/+/post
  - SUB：/sys/{pk}/{dn}/thing/service/+
  - SUB：/sys/{pk}/{dn}/thing/downlink/+
- 租户服务侧（Server AK）默认授权：
  - SUB：/sys/{pk}/+/thing/property/post
  - SUB：/sys/{pk}/+/thing/event/+/post
  - PUB：/sys/{pk}/+/thing/service/+
  - PUB：/sys/{pk}/+/thing/downlink/+
- 平台运维侧（Admin）默认授权：
  - SUB：/sys/+/+/thing/#
  - PUB：/sys/+/+/thing/#（谨慎，需配合RBAC、审计与特性开关）
- 约束：
  - 禁止授予“#”全局写权限给设备侧；
  - 授权变更必须记录审计，包含操作者、范围差异与审批单号。

### 9.19 动态注册签名与错误码规范
- 请求参数（示例）：
  - productKey、deviceName、timestamp（ms）、nonce、sign、signMethod（HmacSHA256）
- 签名：sign = HMAC_SHA256(productSecret, canonicalQueryString)
  - canonicalQueryString：按参数名ASCII排序拼接（不含sign），URL编码
- 时间容差：|client_ts - server_ts| ≤ 300s，否则返回签名过期
- 幂等：nonce 有效期内不可重复，重复请求返回相同结果或显式错误
- 返回：
  - 200：{ code:0, deviceSecretHash:"...", expireAt:"..." }
  - 4xx/5xx：{ code, message, requestId, traceId }
- 错误码（示例）：
  - 1001 参数缺失
  - 1002 签名算法不支持
  - 1003 签名校验失败
  - 1004 时间戳过期
  - 1005 动态注册未启用
  - 1006 deviceName 不符合策略
  - 1007 超过频率限制
  - 2001 内部错误，请稍后重试
- 审计：记录 requestId/traceId、源IP、UA、失败原因与次数

### 9.20 物模型字段校验表（样例）
- 数值型：min≤default≤max；precision∈[0,6]；step>0；单位需匹配单位库（℃、%RH、Pa、V…）
- 枚举型：枚举值唯一；默认值必须在枚举集合内；枚举标识符需遵循小写下划线或camelCase
- 布尔型：default ∈ {true,false}
- 结构体/数组：需提供子字段schema；数组提供 itemType 与最大长度
- 服务：timeoutMs ∈ [100, 60000]；async=false 时须返回reply；输入输出参数命名不可冲突
- 事件：level ∈ {INFO,WARN,ERROR}；type ∈ {info,alert,fault}

### 9.21 规则引擎目标配置 schema（对齐阿里云）
- SLS（日志服务）：{ project, logstore, endpoint, akRef, skRef, tokenRef? }
- Kafka/RocketMQ：{ brokers|endpoint, topic, acks, sasl?:{mechanism, usernameRef, passwordRef} }
- 函数计算：{ service, function, qualifier?, payloadMapping?, akRef, skRef }
- RDS/TSDB：{ type:'mysql'|'pg'|'tsdb', endpoint, db, table, userRef, passwordRef, insertMode }
- HTTP 转发：{ url, method, headers, bodyTemplate, timeoutMs, retryPolicy }
- 通用字段：retryPolicy:{maxRetries:int, backoffMs:int}，dlqRef?:string，metrics?:bool
- 安全：所有secret以“Ref”引用，禁止明文；目标变更纳入审计与灰度


### 9.22 行业物模型模板样例包（ModelTemplate Samples）
- 通用约定：以下为示例模板，字段含义与9.5/9.5.1一致；模板引擎可使用 templateVariables 在渲染时替换范围/单位/是否包含某字段。
- 变量占位：以 ${VAR} 表示；条件包含示例以 condition/includeIf 字段说明（实现可选）。

#### 9.22.1 智能灯 SmartLight（tpl_smart_light）
元数据
- scope: PLATFORM
- name: tpl_smart_light
- industry: smart_home
- version: 1.0.0
- templateVariables:
  - { name: "BRIGHTNESS_MAX", type: "int", default: 100, validate: "<=100" }
  - { name: "SUPPORT_COLOR", type: "bool", default: false }
  - { name: "COLOR_SPACE", type: "enum", default: "RGB", values:["RGB","HSI"] }
TSL 示例
```json
{
  "properties": [
    {"identifier":"powerSwitch","name":"电源","type":"bool","access":"rw","default":false},
    {"identifier":"brightness","name":"亮度","type":"int","unit":"%","min":0,"max":${BRIGHTNESS_MAX},"access":"rw","default":50},
    {"identifier":"colorMode","name":"颜色模式","type":"string","access":"rw","enum":["WHITE","COLOR"],"default":"WHITE","condition":{"includeIf":"${SUPPORT_COLOR}"}},
    {"identifier":"color","name":"颜色","type":"struct","access":"rw","schema":{"r":"int","g":"int","b":"int"},"condition":{"includeIf":"${SUPPORT_COLOR}"}}
  ],
  "events": [
    {"identifier":"fault","name":"故障","level":"ERROR","type":"fault","params":[{"name":"code","type":"int"},{"name":"msg","type":"string"}]}
  ],
  "services": [
    {"identifier":"setBrightness","name":"设置亮度","async":false,"timeoutMs":3000,
     "input":[{"name":"level","type":"int","min":0,"max":${BRIGHTNESS_MAX}}],"output":[]},
    {"identifier":"toggle","name":"开关切换","async":false,"timeoutMs":2000,"input":[],"output":[]},
    {"identifier":"setColor","name":"设置颜色","async":false,"timeoutMs":3000,
     "input":[{"name":"mode","type":"string"},{"name":"r","type":"int"},{"name":"g","type":"int"},{"name":"b","type":"int"}],"output":[],
     "condition":{"includeIf":"${SUPPORT_COLOR}"}}
  ]
}
```

#### 9.22.2 网关 Gateway（tpl_gateway_generic）
元数据
- scope: PLATFORM
- name: tpl_gateway_generic
- industry: industrial
- version: 1.0.0
- templateVariables:
  - { name: "ALLOW_DYNAMIC_CHILD", type: "bool", default: true }
TSL 示例
```json
{
  "properties": [
    {"identifier":"childCount","name":"子设备数","type":"int","min":0,"access":"r"},
    {"identifier":"uplinkRate","name":"上行速率","type":"float","unit":"msg/s","min":0,"access":"r"}
  ],
  "events": [
    {"identifier":"child_join","name":"子设备加入","level":"INFO","type":"info","params":[{"name":"deviceName","type":"string"}]},
    {"identifier":"child_leave","name":"子设备离开","level":"INFO","type":"info","params":[{"name":"deviceName","type":"string"}]}
  ],
  "services": [
    {"identifier":"addSubDevice","name":"添加子设备","async":false,"timeoutMs":5000,
     "input":[{"name":"productKey","type":"string"},{"name":"deviceName","type":"string"}],"output":[],
     "condition":{"includeIf":"${ALLOW_DYNAMIC_CHILD}"}},
    {"identifier":"removeSubDevice","name":"移除子设备","async":false,"timeoutMs":5000,
     "input":[{"name":"productKey","type":"string"},{"name":"deviceName","type":"string"}],"output":[],
     "condition":{"includeIf":"${ALLOW_DYNAMIC_CHILD}"}},
    {"identifier":"discover","name":"发现子设备","async":true,"timeoutMs":10000,"input":[],"output":[{"name":"count","type":"int"}]}
  ]
}
```

#### 9.22.3 温湿度计 TempHumidity（tpl_temp_humi）
元数据
- scope: PLATFORM
- name: tpl_temp_humi
- industry: smart_home
- version: 1.0.0
- templateVariables:
  - { name: "TEMP_MIN", type: "int", default: -40 }
  - { name: "TEMP_MAX", type: "int", default: 85 }
  - { name: "HUMI_MAX", type: "int", default: 100 }
TSL 示例
```json
{
  "properties": [
    {"identifier":"temperature","name":"温度","type":"float","unit":"℃","precision":1,"min":${TEMP_MIN},"max":${TEMP_MAX},"access":"r"},
    {"identifier":"humidity","name":"湿度","type":"float","unit":"%","precision":1,"min":0,"max":${HUMI_MAX},"access":"r"},
    {"identifier":"reportInterval","name":"上报间隔","type":"int","unit":"s","min":10,"max":3600,"default":60,"access":"rw"}
  ],
  "events": [
    {"identifier":"overheat","name":"过热","level":"WARN","type":"alert","params":[{"name":"value","type":"float","unit":"℃"}]}
  ],
  "services": [
    {"identifier":"calibrate","name":"校准","async":false,"timeoutMs":5000,
     "input":[{"name":"offsetT","type":"float"},{"name":"offsetH","type":"float"}],"output":[]},
    {"identifier":"setReportInterval","name":"设置上报间隔","async":false,"timeoutMs":3000,
     "input":[{"name":"interval","type":"int","min":10,"max":3600}],"output":[]}
  ]
}
```

使用说明
- 绑定方式：ProductCategory.standardTSLTemplateRef → ModelTemplate.templateId；或产品建模时选择模板并设置 templateVariables。
- 渲染与校验：渲染完成后生成 Model（9.5），进行 TSL 校验与兼容性检查；禁止破坏性变更影响已发布设备。

## 10. 外部接口需求
- API：统一鉴权、版本化（/v1）、签名/时间戳/回放防护、分页（游标/偏移）/排序/过滤规范、统一错误码、traceId、速率限制与配额层级（全局/租户/用户/AK）。
- SDK（可选）：至少服务端与前端各1套，内置重试/签名/分页封装与示例。
- Webhook：HMAC签名、重试退避、幂等去重、订阅管理与测试面板、死信处理与回执统计。

## 11. 交互与控制台
- 导航：总览（租户级仪表盘）、设备、产品与物模型、规则、告警、OTA、数据查询、报表、审计、设置。
- 设备页：实时属性/事件时间线、指令历史、位置、告警面板、健康度。
- 规则页：可视化编排器、调试/预览、版本与灰度、执行统计与死信。
- OTA：分批/灰度、进度图、失败分析与导出、带宽限速与时间窗配置。
- 报表：模板管理、参数化查询、导出/计划任务。

## 12. 迁移与导入
- CSV/Excel模板、字段校验、回滚/重试、断点续传与校对报表。

## 13. 发布与灰度
- 规则与告警策略灰度（设备分组/比例）、控制台功能特性开关，回滚策略可配置并留档审计。

## 14. 运维与SOP
- 监控项基线：核心接口P99、消息堆积、告警失败率、OTA失败率、规则延迟；自愈动作库与容量告警/扩容流程。

## 15. 验收标准
- 功能覆盖、性能与容量、渗透测试与合规检查、故障演练与灾备切换、文档（API手册/管理员手册/用户手册/运维Runbook）。

## 16. 约束与假设
- 约束：设备接入与数据流转依赖阿里云 IoT Platform 能力边界与配额；跨区域/多云能力视阿里云支持情况。
- 假设：各租户设备通过阿里云标准接入（设备三元组/证书等）；具备必要云账号与授权。

## 17. 参考资料
- 阿里云 IoT 平台与相关云产品文档
- OWASP ASVS/Top10、CNCF Observability、SRE Workbook

---


## 附录A：详细功能性需求（FR）

### A.1 多租户与组织（FR-TEN）
- FR-TEN-001 租户隔离：任何用户仅能访问其租户资源；平台管理员除外（必须）。
- FR-TEN-002 租户生命周期：支持创建/审核/启用/禁用/删除/续期及到期提醒（必须）。
- FR-TEN-003 配额管理：按租户配置设备数、消息吞吐、存储、API速率等（必须）。
- FR-TEN-004 组织结构：租户内支持部门/项目/分组层级管理（应当）。
- FR-TEN-005 白名单策略：支持IP/地域白名单与黑名单（应当）。

### A.2 用户与权限（FR-AUTH）
- FR-AUTH-001 认证：账号密码登录，支持MFA与密码策略（必须）。
- FR-AUTH-002 单点登录：可对接OIDC/SAML2.0（可选）。
- FR-AUTH-003 授权：RBAC+ABAC（基于资源与标签、地域等条件）（必须）。
- FR-AUTH-004 最小权限：新建角色默认最小权限模板（应当）。
- FR-AUTH-005 审批流：敏感操作需二人复核（可选）。

### A.3 产品与物模型（FR-MDL）
- FR-MDL-001 产品定义：创建/更新/禁用产品，记录协议与物模型版本（必须）。
- FR-MDL-002 物模型编辑：属性/事件/服务的草稿、校验与发布（必须）。
- FR-MDL-003 模型兼容：变更影响评估、向后兼容标记（应当）。
- FR-MDL-004 模板库：提供常见设备模板并可复制（应当）。

### A.4 设备生命周期（FR-DEV）
- FR-DEV-001 批量注册/导入/删除/冻结设备（必须）。
- FR-DEV-002 标签与分组：按产品/地域/项目等标签管理（必须）。
- FR-DEV-003 密钥轮换：支持设备密钥轮换与吊销（必须）。
- FR-DEV-004 地理属性：站点/经纬度/地理围栏绑定（应当）。

### A.5 连接与通信（FR-COMM）
- FR-COMM-001 上下行：上行遥测/事件，下行服务调用/属性设置（必须）。
- FR-COMM-002 QoS与离线：支持QoS策略、离线消息与重连退避（必须）。
- FR-COMM-003 消息规范：统一格式/编码/签名与幂等键（必须）。
- FR-COMM-004 限流与处置：按租户与设备级限流及降级策略（必须）。

### A.6 监控与可视化（FR-MON）
- FR-MON-001 仪表盘：租户级总览（在线数、流量、错误率、健康度）（必须）。
- FR-MON-002 设备详情：实时属性、事件时间线、地理位置、告警卡片（必须）。
- FR-MON-003 自定义看板：图表拖拽配置/分享（租户内）（应当）。

### A.7 规则与自动化（FR-RULE）
- FR-RULE-001 规则编排：条件（属性/事件/地理/时序）→ 动作（通知/指令/转发/函数）（必须）。
- FR-RULE-002 调试与灰度：沙箱调试、版本化、灰度发布（应当）。
- FR-RULE-003 可靠性：并发幂等、失败重试、死信队列（必须）。

### A.8 告警与通知（FR-ALM）
- FR-ALM-001 策略类型：阈值/异常/离线/固件失败/规则失败（必须）。
- FR-ALM-002 通道：邮件/短信/IM(Webhook)/电话（可选）（应当至少2种）。
- FR-ALM-003 升级/抑制：告警升级与噪声抑制、值班表（应当）。
- FR-ALM-004 工单：告警转工单并跟踪SLA（可选）。

### A.9 远程控制（FR-CTRL）
- FR-CTRL-001 批量与时窗：单/批下发、执行窗口与回执（必须）。
- FR-CTRL-002 顺序与回退：指令顺序性、失败回退（应当）。
- FR-CTRL-003 审计：敏感指令二次确认与审计日志（必须）。

### A.10 OTA 升级（FR-OTA）
- FR-OTA-001 包管理：固件/脚本/配置包、签名校验（必须）。
- FR-OTA-002 分批策略：按分组/地域/在线状态灰度与回滚（必须）。
- FR-OTA-003 限速与时间窗：带宽限速与维护窗口（应当）。

### A.11 设备影子（FR-SHD）
- FR-SHD-001 读写：影子文档读写与部分更新（必须）。
- FR-SHD-002 版本冲突：冲突检测与重试策略（应当）。
- FR-SHD-003 一致性：影子与物模型一致性校验（应当）。

### A.12 数据与查询（FR-DATA）
- FR-DATA-001 采集：遥测、事件、属性快照、操作日志（必须）。
- FR-DATA-002 存储：热/冷分层与可配置保留期（租户级）（必须）。
- FR-DATA-003 查询：按设备/时间/字段过滤、分页/排序（必须）。
- FR-DATA-004 导出：CSV/Parquet 异步导出与结果下载（应当）。

### A.13 开放API/SDK（FR-API）
- FR-API-001 统一标准：REST/gRPC（可选），版本化（/v1），签名与时间戳（必须）。
- FR-API-002 鉴权：OAuth2/OIDC、AK/SK、最小权限与IP白名单（必须）。
- FR-API-003 可观测：traceId、错误码标准、速率限制与配额（必须）。
- FR-API-004 SDK：至少1种服务端语言+1种前端SDK（应当）。

### A.14 Webhook/推送（FR-WHK）
- FR-WHK-001 事件订阅：上线/告警/规则命中/OTA状态（必须）。
- FR-WHK-002 可靠投递：HMAC签名、重试退避、去重与死信（必须）。
- FR-WHK-003 管理：订阅/测试面板/回执统计（应当）。

### A.15 报表与分析（FR-RPT）
- FR-RPT-001 指标：在线率、消息量、失败率、告警统计、OTA成功率、地理分布（必须）。
- FR-RPT-002 模板：租户级报表模板、导出与定时发送（应当）。

### A.16 计量与计费（FR-BILL）（可选）
- FR-BILL-001 计量：设备数、消息/带宽、存储、API、规则、OTA流量（应当）。
- FR-BILL-002 账务：账期/对账、阈值提醒、停服策略（可选）。

### A.17 审计与合规（FR-AUD）
- FR-AUD-001 覆盖：增删改/权限/密钥/规则/OTA/导出等关键操作（必须）。
- FR-AUD-002 保留：留存周期与访问控制，不可抵赖与完整性保护（必须）。
- FR-AUD-003 合规：等保/隐私/GDPR（如适用）（应当）。

## 附录B：非功能性需求细化（NFR）

### B.1 可用性与容灾
- NFR-AVA-001 平台可用性≥99.9%，区域级故障具备降级与切换（必须）。
- NFR-AVA-002 RPO≤10分钟，RTO≤30分钟（必须）。
- NFR-AVA-003 关键控制面与数据面独立伸缩（应当）。

### B.2 性能与容量
- NFR-PERF-001 设备总量≥1,000,000，单租户≤100,000（目标值，可配额）。
- NFR-PERF-002 上行：稳定≥50k TPS，峰值≥100k TPS，P95≤3s（目标值）。
- NFR-PERF-003 下行指令端到端 P95≤2s；告警触达 P95≤30s（目标值）。
- NFR-PERF-004 查询7天热数据 P95≤2s（索引命中）（目标值）。

### B.3 安全
- NFR-SEC-001 传输TLS，敏感数据加密存储，密钥轮换（必须）。
- NFR-SEC-002 RBAC/ABAC与最小权限，细粒度审计与告警（必须）。
- NFR-SEC-003 反爬/防刷/限流/地理限制与IP白名单（应当）。

### B.4 可维护性与运维
- NFR-OPS-001 健康检查、特性开关、配置中心、灰度（必须）。
- NFR-OPS-002 Runbook、自动化巡检与容量预测（应当）。

### B.5 可观测性
- NFR-OBS-001 指标、日志、分布式追踪三板斧（必须）。
- NFR-OBS-002 SLO/SLA 仪表盘（应当）。

### B.6 国际化/本地化
- NFR-I18N-001 多语言、多时区、单位制切换（应当）。

## 附录C：安全与合规
- C-SEC-001 账户安全：MFA、密码策略、登录保护与异常检测。
- C-SEC-002 数据安全：加密（存储/备份/导出）、脱敏、最小可见原则。
- C-SEC-003 平台安全：权限漂移检测、越权防护、CSRF/XSS/SQLi 等OWASP防护。
- C-SEC-004 合规：等保测评项映射清单、GDPR/隐私条款（如适用）。

## 附录D：关键用例与场景
- UC-001 设备批量导入与自动分组
- UC-002 规则命中触发指令与多通道通知
- UC-003 OTA 分批灰度与回滚
- UC-004 Webhook 订阅测试与签名校验
- UC-005 审计查询与合规导出

## 附录E：术语与缩写
- ABAC：基于属性的访问控制
- RBAC：基于角色的访问控制
- OTA：Over-The-Air 远程升级
- SLO/SLA：服务目标/级别协议

## 附录F：优先级与需求追踪
- MoSCoW：Must/Should/Could/Won't（本期不做）
- 跟踪矩阵：FR/NFR → 用例 → 测试用例 → 验收标准

## 附录G：数据保留与迁移/退出
- G-DATA-001 默认热数据30天、冷数据归档（可配置上限受平台约束）。
- G-DATA-002 迁移与退出：提供租户级数据导出、配置备份与恢复。

