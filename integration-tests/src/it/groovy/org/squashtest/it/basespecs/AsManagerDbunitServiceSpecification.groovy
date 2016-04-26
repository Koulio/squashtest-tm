/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.it.basespecs

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

import org.hibernate.ObjectNotFoundException
import org.hibernate.Query
import org.hibernate.Session
import org.hibernate.transform.ResultTransformer
import org.hibernate.type.LongType
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.TestPropertySource
import org.squashtest.it.config.AsManagerServiceSpecConfig
import org.squashtest.it.config.DynamicServiceConfig
import org.squashtest.it.utils.SkipAll
import org.squashtest.tm.service.BugTrackerConfig;
import org.squashtest.tm.service.SchedulerConfig;
import org.squashtest.tm.service.TmServiceConfig;

import spock.lang.Specification

/**
 * Superclass for a DB-driven DAO test. The test will populate the database using a DBUnit dataset with the same name as the test.
 * Subclasses should be annotated @UnitilsSupport
 */
@Rollback
// inherit the same datasource and TX manager from DatasourceDependantSpecification
@ContextHierarchy(
	@ContextConfiguration(classes = [AsManagerServiceSpecConfig, DynamicServiceConfig, TmServiceConfig, BugTrackerConfig, SchedulerConfig])
)
@SkipAll
abstract class AsManagerDbunitServiceSpecification extends DatasourceDependantSpecification {

	Session getSession() {
		return em.unwrap(Session.class)
	}

	/*-------------------------------------------Private stuff-----------------------------------*/

	boolean found(String tableName, String idColumnName, Long id) {
		String sql = "select count(*) from " + tableName + " where " + idColumnName + " = :id"
		Query query = getSession().createSQLQuery(sql)
		query.setParameter("id", id)

		def result = query.uniqueResult()
		return (result != 0)
	}

	Integer countAll(String className) {
		return (Integer) getSession().createQuery("select count(entity) from " + className + " entity").uniqueResult()
	}

	boolean found(Class<?> entityClass, Object id) {
		boolean found = false

		try {
			found = (getSession().get(entityClass, id) != null)
		} catch (ObjectNotFoundException ex) {
			// Hibernate sometimes pukes the above exception instead of returning null when entity is part of a class hierarchy
			found = false
		}
		return found
	}

	boolean allDeleted(String className, List<Long> ids) {
		Query query = getSession().createQuery("from " + className + " where id in (:ids)")
		query.setParameterList("ids", ids, new LongType())
		List<?> result = query.list()

		return result.isEmpty()
	}

	Object findEntity(Class<?> entityClass, Long id) {
		return getSession().get(entityClass, id)
	}

	List<Object> findAll(String className) {
		return getSession().createQuery("from " + className).list()
	}

	boolean allNotDeleted(String className, List<Long> ids) {
		Query query = getSession().createQuery("from " + className + " where id in (:ids)")
		query.setParameterList("ids", ids, new LongType())
		List<?> result = query.list()

		return result.size() == ids.size()
	}

	NewSQLQuery newSQLQuery(String query) {
		return new NewSQLQuery(query, session)
	}

	Object executeSQL(String query) {
		def q = newSQLQuery(query)
		def expr = /(?is)\s*select.*/
		if (query ==~ expr) {
			return q.list()
		} else {
			q.update()
			return null
		}
	}


	public class NewSQLQuery {

		Query query

		public NewSQLQuery(String query, Session session) {
			this.query = session.createSQLQuery(query)
			this.query.setResultTransformer new EasyResultTransformer()
		}

		NewSQLQuery setParameter(String name, Object value) {
			query.setParameter(name, value)
			return this
		}

		NewSQLQuery setParameter(String name, Object value, Object type) {
			query.setParameter(name, value, type)
			return this
		}

		NewSQLQuery setParameterList(String name, Collection value) {
			query.setParameterList(name, value)
			return this
		}

		NewSQLQuery setParameterList(String name, Collection value, Object type) {
			query.setParameterList(name, value, type)
			return this
		}

		List list() {
			return query.list()
		}

		Object uniqueResult() {
			return query.uniqueResult()
		}

		void update() {
			query.executeUpdate()
		}
	}


	public class EasyResultTransformer implements ResultTransformer {

		@Override
		public Object transformTuple(Object[] tuple, String[] aliases) {
			for (int i = 0; i < tuple.length; i++) {
				def elem = tuple[i]
				def converted = convert(elem)
				tuple[i] = converted
			}
			return tuple
		}

		@Override
		public List transformList(List collection) {
			if (collection.findAll({ it.length == 1 }).size() == collection.size()) {
				return collection.flatten()
			} else {
				return collection
			}
		}

		Object convert(Object sourceOjbect) {
			(sourceOjbect instanceof BigInteger) ? ((BigInteger) sourceOjbect).longValue() : sourceOjbect
		}
	}
}
