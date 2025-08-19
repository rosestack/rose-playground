package io.github.rosestack.notice;

public class SendResult {
	private boolean success;
	private String message;
	private String requestId;
	private String receiptId; // 服务商返回的消息ID

	/**
	 * 构造方法，requestId 不能为空，否则抛出异常。
	 */
	public SendResult(boolean success, String message, String requestId, String receiptId) {
		this.receiptId = receiptId;
		if (requestId == null || requestId.trim().isEmpty()) {
			throw new IllegalArgumentException("SendResult.requestId 不能为空");
		}
		this.success = success;
		this.message = message;
		this.requestId = requestId;
	}

	/**
	 * 成功工厂方法，requestId 必填
	 */
	public static SendResult success(String requestId, String receiptId) {
		return new SendResult(true, null, requestId, receiptId);
	}

	/**
	 * 失败工厂方法，requestId 必填
	 */
	public static SendResult fail(String message, String requestId, String receiptId) {
		return new SendResult(false, message, requestId, receiptId);
	}

	public static SendResult fail(String message, String requestId) {
		return new SendResult(false, message, requestId, null);
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public String getRequestId() {
		return requestId;
	}

	public String getReceiptId() {
		return receiptId;
	}
}
