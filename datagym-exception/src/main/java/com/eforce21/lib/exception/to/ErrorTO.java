package com.eforce21.lib.exception.to;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Error class sent to the client.
 *
 * @author thomas.kuhlins
 */
public class ErrorTO implements Serializable {

	private static final long serialVersionUID = 6719858579577700462L;

	/**
	 * Exception key for i18n.
	 */
	private String key;

	/**
	 * Parameters for substitution in message translation.
	 */
	private List<String> params = new ArrayList<>();

	/**
	 * Default translated exception message. Not meant to be shown directly to the
	 * user but for technical clients and debugging purpose. Any real UI should use
	 * an own i18n mechanism to convert key and parameters to a message with respect
	 * to the current users language.
	 *
	 * @return
	 */
	private String msg;

	/**
	 * Exception HTTP code.
	 */
	private int code;

	/**
	 * Details or submessages. Used especially for form validation to return
	 * multiple validation-errors at once.
	 */
	private List<DetailTO> details = new ArrayList<>();

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public List<String> getParams() {
		return params;
	}

	public void addParam(String param) {
		this.params.add(param);
	}

	public List<DetailTO> getDetails() {
		return details;
	}

	public void addDetail(DetailTO detail) {
		this.details.add(detail);
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return "ErrorTO [key=" + key + ", params=" + params + ", msg=" + msg + ", details=" + details + ", code=" + code + "]";
	}

}
