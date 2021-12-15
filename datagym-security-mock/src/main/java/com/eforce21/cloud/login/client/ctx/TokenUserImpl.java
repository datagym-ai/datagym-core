package com.eforce21.cloud.login.client.ctx;

import com.eforce21.cloud.login.api.model.OauthUser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TokenUserImpl implements OauthUser {

	public static final String SCOPE_TYPE = "type_token";

	private Set<String> scopes = new HashSet<String>();
	private Map<String, String> orgs = new HashMap<>();

	public TokenUserImpl(Set<String> scopes, Map<String, String> orgs) {
		this.scopes.add(SCOPE_TYPE);
		if (scopes != null) {
			this.scopes.addAll(scopes);
		}

		if (orgs != null) {
			this.orgs.putAll(orgs);
		}
	}

	@Override
	public String token() {
		throw new UnsupportedOperationException("TokenUserImpl doesn't have a token.");
	}

	public String id() {
		throw new UnsupportedOperationException("TokenUserImpl doesn't have a user id.");
	}

	@Override
	public Set<String> scopes() {
		return scopes;
	}

	@Override
	public Map<String, String> orgs() {
		return orgs;
	}

	@Override
	public String toString() {
		return "TokenUserImpl [id=" + id() + ", scopes=" + scopes() + ", orgs=" + orgs() + "]";
	}

}
