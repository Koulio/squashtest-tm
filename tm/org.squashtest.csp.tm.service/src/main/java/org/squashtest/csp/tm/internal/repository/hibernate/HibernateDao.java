/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.internal.repository.hibernate;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.squashtest.csp.tm.infrastructure.filter.CollectionFilter;

/**
 * To implement an Hibernate DAO, subclass this class, annotate it with @Repository and work with the Hibernate session
 * provided by {@link #currentSession()}
 * 
 * @author Gregory Fouquet
 * 
 */
public abstract class HibernateDao<ENTITY_TYPE> {
	protected final Class<ENTITY_TYPE> entityType;

	@Inject
	private SessionFactory sessionFactory;

	protected final Session currentSession() {
		return sessionFactory.getCurrentSession();
	}

	@SuppressWarnings("unchecked")
	public HibernateDao() {
		super();
		ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
		entityType = (Class<ENTITY_TYPE>) type.getActualTypeArguments()[0];
	}

	@SuppressWarnings("unchecked")
	protected final ENTITY_TYPE getEntity(long objectId) {
		return (ENTITY_TYPE) currentSession().get(entityType, objectId);
	}

	protected final void persistEntity(Object entity) {
		currentSession().persist(entity);
	}

	/**
	 * Executes a no-args named query which returns a list of entities.
	 * 
	 * @param <R>
	 * @param queryName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected final <R> List<R> executeListNamedQuery(String queryName) {
		return currentSession().getNamedQuery(queryName).list();
	}

	/**
	 * Executes a named query with parameters. The parameters should be set by the callback object.
	 * 
	 * @param <R>
	 * @param queryName
	 * @param setParams
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected final <R> List<R> executeListNamedQuery(String queryName, SetQueryParametersCallback setParams) {
		Session session = currentSession();

		Query q = session.getNamedQuery(queryName);
		setParams.setQueryParameters(q);

		return q.list();
	}

	/**
	 * Executes a named query with parameters. The parameters should be set by the callback object.
	 * 
	 * @param <R>
	 * @param queryName
	 * @param queryParam
	 *            unique param of the query
	 * @param filter
	 *            collection filter used to restrict the number of results.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected final <R> List<R> executeListNamedQuery(@NotNull String queryName, @NotNull Object queryParam,
			@NotNull CollectionFilter filter) {
		Session session = currentSession();

		Query q = session.getNamedQuery(queryName);
		q.setParameter(0, queryParam);
		q.setFirstResult(filter.getFirstItemIndex());
		q.setMaxResults(filter.getMaxNumberOfItems());

		return q.list();
	}

	/**
	 * Runs a named query which returns a single entity / tuple / scalar and which accepts a unique parameter.
	 * 
	 * @param <R>
	 * @param queryName
	 *            name of the query, should not be null
	 * @param paramName
	 *            name of the parameter, should not be null
	 * @param paramValue
	 *            value of the parameter, should not be null
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected final <R> R executeEntityNamedQuery(String queryName, String paramName, Object paramValue) {
		Query q = currentSession().getNamedQuery(queryName);
		q.setParameter(paramName, paramValue);
		return (R) q.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	protected final <R> R executeEntityNamedQuery(String queryName, SetQueryParametersCallback setParams) {
		Query q = currentSession().getNamedQuery(queryName);
		setParams.setQueryParameters(q);
		return (R) q.uniqueResult();
	}

	protected final void removeEntity(ENTITY_TYPE entity) {
		currentSession().delete(entity);
	}
}
