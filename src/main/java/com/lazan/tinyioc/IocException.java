package com.lazan.tinyioc;

@SuppressWarnings("serial")
public class IocException extends RuntimeException {
	public IocException(String msgTemplate) {
		super(msgTemplate);
	}

	public IocException(String msgTemplate, Object... msgArgs) {
		super(String.format(msgTemplate, msgArgs));
	}

	public IocException(Throwable cause, String msgTemplate, Object... msgArgs) {
		super(String.format(msgTemplate, msgArgs), cause);
	}
}
