package io.github.rose.user.domain;

import io.github.rose.core.model.ExtraTenantModel;
import lombok.Data;

import java.io.Serializable;

/**
 * withCustomData
 * withDepartment
 * withLastLogin
 * withTenant
 * withUserSource
 */
@Data
public class User extends ExtraTenantModel<Long> implements Serializable {
    /**
     * 用户名，唯一
     */
    private String username;
    /**
     * 用户真实名称，不具备唯一性
     */
    private String name;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 头像链接
     */
    private String avatar;

    /**
     * 性别:
     * - `M`: 男性，`male`
     * - `F`: 女性，`female`
     * - `U`: 未知，`unknown`
     */
    private Gender gender;

    /**
     * 邮箱，不区分大小写
     */
    private String email;
    /**
     * 手机号，不带区号。如果是国外手机号，请在 phoneCountryCode 参数中指定区号。
     */
    private String phone;
    /**
     * 手机区号，中国大陆手机号可不填。完整的手机区号列表可参阅 https://en.wikipedia.org/wiki/List_of_country_calling_codes。
     */
    private String phoneCountryCode;

    /**
     * 邮箱是否验证
     */
    private Boolean emailVerified;

    /**
     * 手机号是否验证
     */
    private Boolean phoneVerified;

    private Status status;

    /**
     * 出生日期
     */
    private String birthdate;

    /**
     * 姓
     */
    private String familyName;

    /**
     * 中间名
     */
    private String middleName;

    /**
     * 名
     */
    private String givenName;

    /**
     * 用户身份证号码
     */
    private String identityNumber;

    /**
     * 注册方式：import:manual
     */
    private String registerSource;

    /**
     * 来源类型:
     * - `excel`: 通过 excel 导入
     * - `register`: 用户自主注册
     * - `adminCreated`: 管理员后台手动创建（包含使用管理 API 创建用户 ）
     * - `syncTask`: 同步中心的同步任务
     */
    private UserSourceType userSourceType;
    /**
     * 应用 ID 或者同步任务 ID
     */
    private String userSourceId;

    /**
     * 第三方外部 ID
     */
    private String externalId;

    private String customerId;

    /**
     * 状态上次修改时间
     */
    private String statusChangedTime;
}
