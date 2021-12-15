package com.eforce21.cloud.login.api.model;

import java.io.Serializable;

/**
 * JSON Web Key (JWK) - https://tools.ietf.org/html/rfc7517
 * <p>
 * Only one JWK class with EC field.
 * For simplicity and since we only use EC with P-256.
 * <p>
 * If one want's to support different types like RSA: Think about subclassing...
 *
 * @author t
 */
public class OidcJwk implements Serializable {

	private static final long serialVersionUID = 1L;

	private String kty;

	private String crv;

	private String x;

	private String y;

	private String kid;

	public String getKty() {
		return kty;
	}

	public void setKty(String kty) {
		this.kty = kty;
	}

	public String getCrv() {
		return crv;
	}

	public void setCrv(String crv) {
		this.crv = crv;
	}

	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

	public String getKid() {
		return kid;
	}

	public void setKid(String kid) {
		this.kid = kid;
	}

	@Override
	public String toString() {
		return "OidcJwk [kty=" + kty + ", crv=" + crv + ", x=" + x + ", y=" + y + ", kid=" + kid + "]";
	}

}
