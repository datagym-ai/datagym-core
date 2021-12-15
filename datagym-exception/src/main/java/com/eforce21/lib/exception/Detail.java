package com.eforce21.lib.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Detail to wrap multiple messages/causes in one exception.
 *
 * @author thomas.kuhlins
 */
public class Detail {

	/**
	 * Name of the detail/field that causes the error.
	 */
	private String name;

	/**
	 * Error message key for i18n.
	 */
	private String key;

	/**
	 * Parameters for substitution in message translation.
	 */
	private List<Object> params = new ArrayList<>();

	public Detail(String name, String key, Object... params) {
		this.name = name;
		this.key = key;
		this.params.addAll(Arrays.asList(params));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<Object> getParams() {
		return params;
	}

	public void addParam(Object param) {
		this.params.add(param);
	}

	@Override
	public String toString() {
		return "Detail [name=" + name + ", key=" + key + ", params=" + params + "]";
	}


}
