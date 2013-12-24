/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.util.List;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.service.internal.repository.BoundEntityDao;

@Repository
public class HibernateBoundEntityDao implements BoundEntityDao {
	
	private static final String TEST_CASE_QUERY_NAME = "BoundEntityDao.findAllTestCasesForProject";
	private static final String REQUIREMENT_QUERY_NAME = "BoundEntityDao.findAllReqVersionsForProject";
	private static final String CAMPAIGN_QUERY_NAME = "BoundEntityDao.findAllCampaignsForProject";
	private static final String ITERATION_QUERY_NAME = "BoundEntityDao.findAllIterationsForProject";
	private static final String TEST_SUITE_QUERY_NAME = "BoundEntityDao.findAllTestSuitesForProject";
	private static final String TEST_STEP_QUERY_NAME = "BoundEntityDao.findAllTestStepsForProject";
	private static final String EXECUTION_QUERY_NAME = "BoundEntityDao.findAllExecutionsForProject";
	private static final String EXECUTION_STEP_QUERY_NAME = "BoundEntityDao.findAllExecutionStepsForProject";

	@Inject
	private SessionFactory factory;
	
	
	@Override
	@SuppressWarnings("unchecked")
	public List<BoundEntity> findAllForBinding(CustomFieldBinding customFieldBinding) {
		
		String queryName = "";
		switch(customFieldBinding.getBoundEntity()){
		case TEST_CASE 			 : queryName = TEST_CASE_QUERY_NAME; 
								   break;
		
		case REQUIREMENT_VERSION : queryName = REQUIREMENT_QUERY_NAME;
								   break;
									
		case CAMPAIGN			 : queryName =CAMPAIGN_QUERY_NAME;
									break;
									
		case ITERATION			 : queryName =ITERATION_QUERY_NAME;
									break;
		
		case TEST_SUITE			: queryName = TEST_SUITE_QUERY_NAME;
									break;
									
		case TEST_STEP			: queryName = TEST_STEP_QUERY_NAME;
									break;
		
		case EXECUTION 			: queryName = EXECUTION_QUERY_NAME;
									break;

		case EXECUTION_STEP 	: queryName = EXECUTION_STEP_QUERY_NAME;
									break;
		
		}
		
		Session session  = factory.getCurrentSession();
		Query q = session.getNamedQuery(queryName);
		q.setParameter("projectId", customFieldBinding.getBoundProject().getId());
		
		return q.list();
	}
	
	
	//uh, a bit sloppy here
	@Override
	public BoundEntity findBoundEntity(CustomFieldValue customFieldValue) {
		return findAllForBinding(customFieldValue.getBinding()).get(0);
	}
	
	@Override
	public BoundEntity findBoundEntity(Long boundEntityId,
			BindableEntity entityType) {
	
		Class<?> entityClass = entityType.getReferencedClass();
		return (BoundEntity)factory.getCurrentSession().load(entityClass, boundEntityId);
		
	}
	
	
	@Override
	public boolean hasCustomField(Long boundEntityId, BindableEntity entityType) {
		
		Query query = factory.getCurrentSession().getNamedQuery("BoundEntityDao.hasCustomFields");
		query.setParameter("boundEntityId", boundEntityId, LongType.INSTANCE);
		query.setParameter("boundEntityType", entityType);
		
		return ((Long)query.uniqueResult() != 0);
		
	}
	
	
}
