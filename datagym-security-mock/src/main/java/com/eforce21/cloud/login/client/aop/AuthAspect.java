package com.eforce21.cloud.login.client.aop;

import com.eforce21.cloud.login.api.model.OauthUser;
import com.eforce21.cloud.login.client.ctx.SecurityContext;
import com.eforce21.lib.exception.ForbiddenException;
import com.eforce21.lib.exception.UnauthorizedException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

@Component
@Aspect
@Order(100)
public class AuthAspect {

	/**
	 * Catch method executions annotated with {@link AuthUser} or within classes
	 * annotated with {@link AuthUser} and make sure there's an authorized user in
	 * {@link SecurityContext} or else throw {@link UnauthorizedException}.
	 *
	 * @param pjp
	 * @param anno
	 * @return
	 * @throws Throwable
	 */
	@Around("@annotation(anno) || @within(anno)")
	public Object method(ProceedingJoinPoint pjp, AuthUser anno) throws Throwable {
		if (SecurityContext.get() == null) {
			throw new UnauthorizedException();
		}

		return pjp.proceed();
	}

	/**
	 * Catch method executions annotated with {@link AuthScope} or within classes
	 * annotated with {@link AuthScope} and make sure there's an authorized user in
	 * {@link SecurityContext} or else throw {@link UnauthorizedException}. Additionally
	 * the user must have one of the specified scopes otherwise an {@link ForbiddenException}
	 * is thrown.
	 *
	 * @param pjp
	 * @param anno Only to build the expression string. Spring won't wire it.
	 * @return
	 * @throws Throwable
	 */
	@Around("@annotation(anno) || @within(anno)")
	public Object method(ProceedingJoinPoint pjp, AuthScope anno) throws Throwable {
		OauthUser usr = SecurityContext.get();
		if (usr == null) {
			throw new UnauthorizedException();
		}

		MethodSignature sig = (MethodSignature) pjp.getSignature();
		Method method = sig.getMethod();
		AuthScope a = AnnotationUtils.findAnnotation(method, AuthScope.class);

		if (!usr.scopes().containsAll(Arrays.asList(a.all()))) {
			throw new UnauthorizedException();
		}

		if (a.any().length > 0 && !CollectionUtils.containsAny(usr.scopes(), Arrays.asList(a.any()))) {
			throw new ForbiddenException();
		}

		return pjp.proceed();
	}
}
