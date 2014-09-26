/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.core.dynamicmanager.internal.handler;

import java.lang.reflect.Method;

import javax.validation.constraints.NotNull;

import org.hibernate.Query;
import org.hibernate.SessionFactory;

/**
 * This {@link DynamicComponentInvocationHandler} handles any method of signature <code>ENTITY find*(..)</code> by
 * looking up a Hibernate Named Query which name matches the method's name and returning its results.
 * 
 * @author Gregory Fouquet
 * 
 */
public class EntityFinderNamedQueryHandler<ENTITY> extends AbstractNamedQueryHandler<ENTITY> {
	private final Class<ENTITY> entityType;

	/**
	 * @param sessionFactory
	 * @param entityType
	 * @param queryNamespace
	 */
	public EntityFinderNamedQueryHandler(Class<ENTITY> entityType, @NotNull SessionFactory sessionFactory) {
		super(entityType, sessionFactory);
		this.entityType = entityType;
	}

	@Override
	protected Object executeQuery(Query query) {
		return query.uniqueResult();
	}

	/**
	 * handles any method which returns an ENTITY
	 */
	@Override
	public boolean canHandle(Method method) {
		return matchesHandledMethodName(method) && matchesHandledReturnType(method);
	}

	/**
	 * @param method
	 * @return
	 */
	private boolean matchesHandledReturnType(Method method) {
		return entityType.equals(method.getReturnType());
	}

	/**
	 * @param method
	 * @return
	 */
	private boolean matchesHandledMethodName(Method method) {
		return method.getName().startsWith("find");
	}

}
