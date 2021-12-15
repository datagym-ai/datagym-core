package com.eforce21.cloud.login.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON Web Key Set (JWKS) - https://tools.ietf.org/html/rfc7517
 *
 * @author t
 */
public class OidcJwks implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<OidcJwk> keys = new ArrayList<>();

	public List<OidcJwk> getKeys() {
		return keys;
	}

	public void setKeys(List<OidcJwk> keys) {
		this.keys = keys;
	}

	public void addKey(OidcJwk key) {
		this.keys.add(key);
	}

	@Override
	public String toString() {
		return "OidcJwks [keys=" + keys + "]";
	}

}
