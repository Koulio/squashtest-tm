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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.LongType;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.service.internal.repository.CustomDatasetDao;


public class DatasetDaoImpl extends HibernateEntityDao<Dataset> implements CustomDatasetDao {

	@PersistenceContext
	private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<Dataset> findOwnDatasetsByTestCase(Long testCaseId) {

		Query query = em.unwrap(Session.class).getNamedQuery("Dataset.findOwnDatasetsByTestCase");
		query.setParameter("testCaseId", testCaseId);
		return query.list();
	}



	@SuppressWarnings("unchecked")
	@Override
	public List<Dataset> findOwnDatasetsByTestCases(List<Long> testCaseIds) {
		if (!testCaseIds.isEmpty()) {
			Query query = em.unwrap(Session.class).getNamedQuery("Dataset.findOwnDatasetsByTestCases");
			query.setParameterList("testCaseIds", testCaseIds);
			return query.list();
		} else {
			return Collections.emptyList();
		}
	}


	@Override
	public List<Dataset> findImmediateDelegateDatasets(Long testCaseId) {

		Query q = em.unwrap(Session.class).getNamedQuery("Dataset.findTestCasesThatInheritParameters");
		q.setParameter("srcIds", LongType.INSTANCE);

		List<Long> tcids = q.list();

		return findOwnDatasetsByTestCases(tcids);
	}

	@Override
	public List<Dataset> findAllDelegateDatasets(Long testCaseId) {
		List<Dataset> allDatasets = new LinkedList<>();

		Set<Long> exploredTc = new HashSet<>();
		List<Long> srcTc = new LinkedList<>();
		List<Long> destTc;

		Query next = em.unwrap(Session.class).getNamedQuery("dataset.findTestCasesThatInheritParameters");

		srcTc.add(testCaseId);

		while(! srcTc.isEmpty()){

			next.setParameterList("srcIds", srcTc, LongType.INSTANCE);
			destTc = next.list();

			if (! destTc.isEmpty()){
				allDatasets.addAll( findOwnDatasetsByTestCases(destTc) );
			}

			exploredTc.addAll(srcTc);
			srcTc = destTc;
			srcTc.removeAll(exploredTc);

		}

		return allDatasets;
	}


	@Override
	public List<Dataset> findOwnAndDelegateDatasets(Long testCaseId) {
		List<Dataset> allDatasets = findOwnDatasetsByTestCase(testCaseId);
		allDatasets.addAll(findAllDelegateDatasets(testCaseId));
		return allDatasets;
	}



	@Override
	public void removeDatasetFromTestPlanItems(Long datasetId) {
		Query query = em.unwrap(Session.class).getNamedQuery("dataset.removeDatasetFromItsIterationTestPlanItems");
		query.setParameter("datasetId", datasetId);
		query.executeUpdate();

		Query query2 = em.unwrap(Session.class).getNamedQuery("dataset.removeDatasetFromItsCampaignTestPlanItems");
		query2.setParameter("datasetId", datasetId);
		query2.executeUpdate();
	}
}
