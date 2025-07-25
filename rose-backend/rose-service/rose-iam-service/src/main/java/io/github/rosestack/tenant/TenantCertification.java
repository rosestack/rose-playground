package io.github.rosestack.tenant;

import lombok.Data;

@Data
public class TenantCertification {
    private String id;

    private String tenantId;

    /**
     * 工商登记名称
     */
    private String businessRegistrationName;

    /**
     * 营业执照
     */
    private String businessLicense;

    /**
     * 统一社会信用代码
     */
    private String unifiedSocialCredit;

    /**
     * 法人姓名
     */
    private String legalPersonName;

    /**
     * 法人身份证号
     */
    private String legalPersonIdentityNumber;

    /**
     * 法人银行卡号
     */
    private String legalPersonBankCardNumber;

    /**
     * 银行预留手机号
     */
    private String bankReservedPhone;

    /**
     * 状态
     */
    private Boolean status;

    /**
     * 创建时间
     */
    private String createdAt;
    /**
     * 修改时间
     */
    private String updatedAt;

}
