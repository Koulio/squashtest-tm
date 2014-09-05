/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.core.dynamicmanager.internal.handler;

import java.lang.reflect.Method;

import javax.validation.constraints.NotNull;

import org.hibernate.SessionFactory;

/**
 * @author Gregory Fouquet
 * 
 */
public class PersistEntityHandler<ENTITY> implements DynamicComponentInvocationHandler {
	private final Class<ENTITY> entityType;
	private final SessionFactory sessionFactory;

	/**
	 * @param entityType
	 * @param sessionFactory
	 */
	public PersistEntityHandler(@NotNull Class<ENTITY> entityType, @NotNull SessionFactory sessionFactory) {
		super();
		this.entityType = entityType;
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Performs persist using the first arg as the entity to persist.
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) {
		sessionFactory.getCurrentSession().persist(args[0]);
		return null;
	}

	/**
	 * @return <code>true</code> if method signature is <code>ENTITY findById(long id)</code>
	 */
	@Override
	public boolean handles(Method method) {
		return methodNameMatchesExpectedPattern(method) && mehtodParamsMatchExpectedParams(method)
				&& methodReturnTypeMatchesExpectedType(method);
	}

	private boolean mehtodParamsMatchExpectedParams(Method method) {
		Class<?>[] params = method.getParameterTypes();
		return params.length == 1 && entityType.isAssignableFrom(params[0]);
	}

	public boolean methodNameMatchesExpectedPattern(Method method) {
		return "persist".equals(method.getName());
	}

	private boolean methodReturnTypeMatchesExpectedType(Method method) {
		return Void.TYPE.equals(method.getReturnType());
	}

}
