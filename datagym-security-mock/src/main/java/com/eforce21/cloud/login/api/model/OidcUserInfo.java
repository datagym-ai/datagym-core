package com.eforce21.cloud.login.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * OpenIdConnect UserInfo.
 *
 * @author t
 */
public class OidcUserInfo implements OauthUser{

	private String sub;
	private String email;
	private String name;
	private Set<OidcOrgInfo> orgs = new HashSet<>();
	private Set<String> scopes = new HashSet<>();
	private boolean isOpenCoreEnvironment = false;

	public static OidcUserInfo getMock(){
		HashSet<String> scopes = new HashSet<>(Arrays.asList("offline_access", "orgs"));

		Set<OidcOrgInfo> dummyOrgs = new HashSet<>();
		OidcOrgInfo e = new OidcOrgInfo();
		e.setName("OpenCore Organisation");
		e.setRole("ADMIN");
		e.setSub("open-core-dummy-org-sub");
		e.setPersonal(true);
		dummyOrgs.add(e);

		OidcUserInfo mockUserInfo = new OidcUserInfo();
		mockUserInfo.setName("OpenCore User");
		mockUserInfo.setEmail("open-core-dummy-user-mail@datagym.ai");
		mockUserInfo.setSub("open-core-dummy-user-sub");
		mockUserInfo.setOrgs(dummyOrgs);
		mockUserInfo.setScopes(scopes);
		mockUserInfo.setOpenCoreEnvironment(true);
		return mockUserInfo;
	}

	public String getSub() {
		return sub;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<OidcOrgInfo> getOrgs() {
		return orgs;
	}

	public void setOrgs(Set<OidcOrgInfo> orgs) {
		this.orgs = orgs;
	}

	public Set<String> getScopes() {
		return scopes;
	}

	public void setScopes(Set<String> scopes) {
		this.scopes = scopes;
	}

	@JsonProperty("isOpenCoreEnvironment")
	public boolean isOpenCoreEnvironment() {
		return isOpenCoreEnvironment;
	}

	public void setOpenCoreEnvironment(boolean openCoreEnvironment) {
		isOpenCoreEnvironment = openCoreEnvironment;
	}

	@Override
	public String token() {
		return null;
	}

	@Override
	public String id() {
		return this.sub;
	}

	@Override
	public Set<String> scopes() {
		Set<String> result = new HashSet<>();
		result.add("type_oauth");

		result.addAll(scopes);

		return result;
	}

	@Override
	public Map<String, String> orgs() {
		Map<String, String> result = new HashMap<>();

		for (OidcOrgInfo org : orgs) {
			String orgId = org.getSub();
			String role = org.getRole();

			result.put(orgId, role);
		}

		return result;
	}

	@Override
	public String toString() {
		return "OidcUserInfo [sub=" + sub + ", email=" + email + ", name=" + name + ", scopes=" + scopes + ", orgs=" + orgs + "]";
	}


}
