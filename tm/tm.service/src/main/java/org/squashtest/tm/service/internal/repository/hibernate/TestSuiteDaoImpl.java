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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.SingleToMultiSortingAdapter;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.campaign.IndexedIterationTestPlanItem;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.CustomTestSuiteDao;

public class TestSuiteDaoImpl extends HibernateEntityDao<TestSuite> implements CustomTestSuiteDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestSuiteDaoImpl.class);

	/*
	 * Because it is impossible to sort over the indices of ordered collection in a criteria query we must then build an
	 * hql string which will let us do that.
	 */
	private static final String HQL_INDEXED_TEST_PLAN = "select index(IterationTestPlanItem), IterationTestPlanItem," +
			"(select min(m.endDate) from IterationTestPlanItem itpi " +
			"left join itpi.referencedTestCase ctc left join ctc.milestones m where itpi.id = IterationTestPlanItem.id) as endDate "
			+ "from TestSuite as TestSuite inner join TestSuite.testPlan as IterationTestPlanItem "
			+ "left outer join IterationTestPlanItem.referencedTestCase as TestCase "
			+ "left outer join TestCase.project as Project "
			+ "left outer join IterationTestPlanItem.referencedDataset as Dataset "
			+ "left outer join IterationTestPlanItem.user as User " + "where TestSuite.id = :suiteId ";


	private static final Map<String, Map<String, String>> VALUE_DEPENDENT_FILTER_CLAUSES = new HashMap<String, Map<String, String>>();
	
	private static final String VDFC_DEFAULT_KEY = "VDFC_DEFAULT_KEY";
	
	static {
		Map<String, String> modeDataMap = new HashMap<String, String>(2);
		modeDataMap.put(TestCaseExecutionMode.MANUAL.name(),
				TestPlanFilteringHelper.HQL_INDEXED_TEST_PLAN_MODEMANUAL_FILTER);
		modeDataMap.put(VDFC_DEFAULT_KEY, TestPlanFilteringHelper.HQL_INDEXED_TEST_PLAN_MODEAUTO_FILTER);
		VALUE_DEPENDENT_FILTER_CLAUSES.put(TestPlanFilteringHelper.MODE_DATA, modeDataMap);

		Map<String, String> userData = new HashMap<String, String>(2);
		userData.put("0", TestPlanFilteringHelper.HQL_INDEXED_TEST_PLAN_NULL_USER_FILTER);
		userData.put(VDFC_DEFAULT_KEY, TestPlanFilteringHelper.HQL_INDEXED_TEST_PLAN_USER_FILTER);
		VALUE_DEPENDENT_FILTER_CLAUSES.put(TestPlanFilteringHelper.USER_DATA, userData);

	}
	
	
	@Override
	public TestPlanStatistics getTestSuiteStatistics(final long testSuiteId) {
		
		Query q = currentSession().getNamedQuery("TestSuite.countStatuses");
		q.setParameter("id", testSuiteId);
		List<Object[]> result = q.list();

		return new TestPlanStatistics(result);
	}

	@Override
	public TestPlanStatistics getTestSuiteStatistics(long testSuiteId, String userLogin) {
		
		Query q = currentSession().getNamedQuery("TestSuite.countStatusesForUser");
		q.setParameter("id", testSuiteId);
		q.setParameter("login", userLogin);
		List<Object[]> result = q.list();

		return new TestPlanStatistics(result);

	}



	@Override
	public List<IterationTestPlanItem> findTestPlan(long suiteId, PagingAndMultiSorting sorting, Filtering filtering,
			ColumnFiltering columnFiltering) {
		List<Object[]> tuples = findIndexedTestPlanAsTuples(suiteId, sorting, filtering, columnFiltering);
		return buildItems(tuples);
	}

	@Override
	public List<IndexedIterationTestPlanItem> findIndexedTestPlan(long suiteId, PagingAndSorting sorting,
			Filtering filtering, ColumnFiltering columnFiltering) {

		return findIndexedTestPlan(suiteId, new SingleToMultiSortingAdapter(sorting), filtering, columnFiltering);
	}

	@Override
	public List<IndexedIterationTestPlanItem> findIndexedTestPlan(final long suiteId, PagingAndMultiSorting sorting,
			Filtering filtering, ColumnFiltering columnFiltering) {

		List<Object[]> tuples = findIndexedTestPlanAsTuples(suiteId, sorting, filtering, columnFiltering);
		return buildIndexedItems(tuples);

	}
	

	@Override
	public long countTestPlans(Long suiteId, Filtering filtering, ColumnFiltering columnFiltering) {

		StringBuilder hqlbuilder = buildTestPlanQueryBody(filtering, columnFiltering);

		Query query = assignParameterValuesToTestPlanQuery(hqlbuilder.toString(), suiteId, filtering, columnFiltering);

		return query.list().size();
	}



	private StringBuilder buildTestPlanQueryBody(Filtering filtering, ColumnFiltering columnFiltering) {

		StringBuilder hqlbuilder = new StringBuilder(HQL_INDEXED_TEST_PLAN);

		// check if we want to filter on the user login
		if (filtering.isDefined()) {
			hqlbuilder.append("and User.login = :userLogin ");
		}

		// additional where clauses
		TestPlanFilteringHelper.appendFilteringRestrictions(hqlbuilder, columnFiltering);

		for (Entry<String, Map<String, String>> valueDependantFilterClause : VALUE_DEPENDENT_FILTER_CLAUSES.entrySet()) {
			String filterName = valueDependantFilterClause.getKey();
			Map<String, String> clausesByValues = valueDependantFilterClause.getValue();
			if (columnFiltering.hasFilter(filterName)) {
				String filterValue = columnFiltering.getFilter(filterName);
				String clause = clausesByValues.get(filterValue);
				if (clause == null) {
					clause = clausesByValues.get(VDFC_DEFAULT_KEY);
				}
			}
		}

		return hqlbuilder;
	}


	private Query assignParameterValuesToTestPlanQuery(String queryString, Long suiteId, Filtering filtering,
			ColumnFiltering columnFiltering) {
		Query query = currentSession().createQuery(queryString);
		query.setParameter("suiteId", suiteId);
		TestPlanFilteringHelper.setFilters(query, filtering, columnFiltering);

		return query;
	}

	@SuppressWarnings("unchecked")
	private List<Object[]> findIndexedTestPlanAsTuples(final long suiteId, PagingAndMultiSorting sorting,
			Filtering filtering, ColumnFiltering columnFiltering) {

		StringBuilder hqlbuilder = buildTestPlanQueryBody(filtering, columnFiltering);

		// tune the sorting to make hql happy
		LevelImplementorSorter wrapper = new LevelImplementorSorter(sorting);
		wrapper.map("TestCase.importance", TestCaseImportance.class);
		wrapper.map("IterationTestPlanItem.executionStatus", ExecutionStatus.class);

		SortingUtils.addOrder(hqlbuilder, wrapper);

		Query query = assignParameterValuesToTestPlanQuery(hqlbuilder.toString(), suiteId, filtering, columnFiltering);

		PagingUtils.addPaging(query, sorting);

		return query.list();
	}


	// ************************ utils ********************

	private List<IterationTestPlanItem> buildItems(List<Object[]> tuples) {

		List<IterationTestPlanItem> items = new ArrayList<IterationTestPlanItem>(tuples.size());

		for (Object[] tuple : tuples) {
			IterationTestPlanItem itpi = (IterationTestPlanItem) tuple[1];
			items.add(itpi);
		}

		return items;
	}

	private List<IndexedIterationTestPlanItem> buildIndexedItems(List<Object[]> tuples) {
		List<IndexedIterationTestPlanItem> indexedItems = new ArrayList<IndexedIterationTestPlanItem>(tuples.size());

		for (Object[] tuple : tuples) {
			Integer index = (Integer) tuple[0];
			IterationTestPlanItem itpi = (IterationTestPlanItem) tuple[1];
			indexedItems.add(new IndexedIterationTestPlanItem(index, itpi));
		}

		return indexedItems;
	}


}