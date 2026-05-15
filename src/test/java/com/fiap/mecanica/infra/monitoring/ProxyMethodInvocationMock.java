package com.fiap.mecanica.infra.monitoring;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.ProxyMethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ProxyMethodInvocationMock implements ProxyMethodInvocation {

	private final Object target;
	private final Method method;
	private final Object[] arguments;
	private final Object proxy;
	private final Map<String, Object> userAttributes = new HashMap<>();

	public ProxyMethodInvocationMock(Object target, Method method, Object[] arguments) {
		this.target = target;
		this.method = method;
		this.arguments = arguments != null ? arguments.clone() : new Object[0];
		this.proxy = target;
	}

	@Override
	public Object getProxy() {
		return proxy;
	}

	@Override
	public MethodInvocation invocableClone() {
		return new ProxyMethodInvocationMock(target, method, arguments);
	}

	@Override
	public MethodInvocation invocableClone(Object... arguments) {
		return new ProxyMethodInvocationMock(target, method, arguments);
	}

	@Override
	public void setArguments(Object... arguments) {
		System.arraycopy(arguments, 0, this.arguments, 0, Math.min(arguments.length, this.arguments.length));
	}

	@Override
	public void setUserAttribute(String key, Object value) {
		if (value != null) {
			userAttributes.put(key, value);
		} else {
			userAttributes.remove(key);
		}
	}

	@Override
	public Object getUserAttribute(String key) {
		return userAttributes.get(key);
	}

	@Override
	public Method getMethod() {
		return method;
	}

	@Override
	public Object[] getArguments() {
		return arguments != null ? arguments.clone() : new Object[0];
	}

	@Override
	public Object proceed() throws Throwable {
		if (method == null && target == null) {
			throw new IllegalArgumentException("Method and target cannot be null");
		}

		return method.invoke(target, arguments);
	}

	@Override
	public Object getThis() {
		return target;
	}

	@Override
	public AccessibleObject getStaticPart() {
		return method;
	}
}
