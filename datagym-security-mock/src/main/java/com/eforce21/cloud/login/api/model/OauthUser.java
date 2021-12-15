package com.eforce21.cloud.login.api.model;

import java.util.Map;
import java.util.Set;

/**
 * Interface for user information delivered by cloud login.
 *
 * @author t
 */
public interface OauthUser {

	/**
	 * Access token to authenticate calls against OAuth2/Oidc services.
	 *
	 * @return
	 */
	String token();

	/**
	 * User ID aka subject.
	 *
	 * @return
	 */
	String id();

	/**
	 * User scopes aka roles.
	 *
	 * @return
	 */
	Set<String> scopes();

	/**
	 * Organisation IDs with role.
	 *
	 * @return
	 */
	Map<String, String> orgs();

}
