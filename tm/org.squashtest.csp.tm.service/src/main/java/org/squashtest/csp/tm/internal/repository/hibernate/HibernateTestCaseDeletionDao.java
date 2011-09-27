/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.internal.repository.TestCaseDeletionDao;


/*
 * we'll perform a lot of operation using SQL because Hibernate whine at bulk-delete on polymorphic entities.
 * 
 * See bugs : HHH-4183, HHH-1361, HHH-1657 
 * 
 */

@Repository
public class HibernateTestCaseDeletionDao extends HibernateDeletionDao implements TestCaseDeletionDao {




	@Override
	public void removeEntities(final List<Long> entityIds) {
		if (!entityIds.isEmpty()) {
			
			Query query=getSession().createSQLQuery(NativeQueries.testCase_sql_removeFromFolder);
			query.setParameterList("ancIds", entityIds, LongType.INSTANCE);
			query.setParameterList("descIds", entityIds, LongType.INSTANCE);
			query.executeUpdate();

			executeDeleteSQLQuery(NativeQueries.testCase_sql_removeFromLibrary, "testCaseIds", entityIds);
	
			executeDeleteSQLQuery(NativeQueries.testCaseFolder_sql_remove, "nodeIds", entityIds);
			executeDeleteSQLQuery(NativeQueries.testCase_sql_remove, "nodeIds", entityIds);
			executeDeleteSQLQuery(NativeQueries.testCaseLibraryNode_sql_remove, "nodeIds", entityIds);

			
		}

	}

	@Override
	public void removeAllSteps(List<Long> testStepIds) {
		if (!testStepIds.isEmpty()) {
			executeDeleteSQLQuery(NativeQueries.testCase_sql_removeTestStepFromList, "testStepIds", testStepIds);
			
			executeDeleteSQLQuery(NativeQueries.testStep_sql_removeActionSteps, "testStepIds", testStepIds);
			executeDeleteSQLQuery(NativeQueries.testStep_sql_removeCallSteps, "testStepIds", testStepIds);
			executeDeleteSQLQuery(NativeQueries.testStep_sql_removeTestSteps, "testStepIds", testStepIds);
		}
	}

	@Override
	public List<Long> findTestSteps(List<Long> testCaseIds) {
		if (! testCaseIds.isEmpty()){
			return executeSelectNamedQuery("testCase.findAllSteps", "testCaseIds", testCaseIds);
		}
		return Collections.emptyList();
	}

	@Override
	public List<Long> findTestCaseAttachmentListIds(List<Long> testCaseIds) {
		if (! testCaseIds.isEmpty()){
			return executeSelectNamedQuery("testCase.findAllAttachmentLists", "testCaseIds",testCaseIds);
		}
		return Collections.emptyList();
	}

	@Override
	public List<Long> findTestStepAttachmentListIds(List<Long> testStepIds) {
		if (! testStepIds.isEmpty()){
			return executeSelectNamedQuery("testStep.findAllAttachmentLists", "testStepIds",testStepIds);
		}
		return Collections.emptyList();
	}



	@Override
	/*
	 * we're bound to use sql since hql offers no solution here.
	 * 
	 * that method will perform the following : 
	 * 
	 * - update the order of all campaign item test plan ranked after the ones we're about to delete
	 * - delete the campaign item test plans.
	 * 
	 * Also, because MySQL do not support sub queries selecting from the table being updated we have to proceed with the awkward treatment that follows : 
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void removeCallingCampaignItemTestPlan(List<Long> testCaseIds) {
		
		if (! testCaseIds.isEmpty()){
			
			//first we must reorder the campaign_item_test_plans
 			Query query1 = getSession().createSQLQuery(NativeQueries.testCase_sql_getCallingCampaignItemTestPlanOrderOffset);
			query1.setParameterList("testCaseIds1", testCaseIds, LongType.INSTANCE);
			query1.setParameterList("testCaseIds2", testCaseIds, LongType.INSTANCE);
			List<Object[]> pairIdOffset = query1.list();
			
			Map<Integer, List<Long>> mapOffsets = buildMapOfOffsetAndIds(pairIdOffset);
			
			for (Integer offset : mapOffsets.keySet()){
				Query query = getSession().createSQLQuery(NativeQueries.testCase_sql_updateCallingCampaignItemTestPlan);
				query.setParameter("offset", offset, IntegerType.INSTANCE);
				query.setParameterList("ctpiIds", mapOffsets.get(offset), LongType.INSTANCE);
				query.executeUpdate();
			}
			
			//now we can delete the items
			executeDeleteSQLQuery(NativeQueries.testCase_sql_removeCallingCampaignItemTestPlan, "testCaseIds", testCaseIds);
			
		}
				
	}
	
	
	private Map<Integer, List<Long>> buildMapOfOffsetAndIds(List<Object[]> list){
		Map<Integer, List<Long>> result = new HashMap<Integer, List<Long>>();
		
		for (Object[] pair : list){
			Integer offset = ((BigInteger)pair[1]).intValue();
			
			//we skip if the offset is 0
			if (offset==0) continue;
			
			if (! result.containsKey(offset)){
				result.put(offset, new LinkedList<Long>());
			}
			
			result.get(offset).add(((BigInteger)pair[0]).longValue());
		}
		
		return result;
		
	}


/*
 * same comment than for HibernateTestCaseDeletionDao#removeCallingCampaignItemTestPlan
 * 
 * (non-Javadoc)
 * @see org.squashtest.csp.tm.internal.repository.TestCaseDeletionDao#removeOrSetNullCallingIterationItemTestPlan(java.util.List)
 */
	@Override
	@SuppressWarnings("unchecked")
	public void removeOrSetNullCallingIterationItemTestPlan(
			List<Long> testCaseIds) {
	
		if (! testCaseIds.isEmpty()){
			SQLQuery query1 = getSession().createSQLQuery(NativeQueries.testCase_sql_selectCallingIterationItemTestPlanHavingExecutions);
			query1.addScalar("item_test_plan_id", LongType.INSTANCE);
			query1.setParameterList("testCaseIds", testCaseIds, LongType.INSTANCE);
			List<Long> itpHavingExecIds = query1.list();

			
			SQLQuery query2 = getSession().createSQLQuery(NativeQueries.testCase_sql_selectCallingIterationItemTestPlanHavingNoExecutions);
			query2.addScalar("item_test_plan_id", LongType.INSTANCE);
			query2.setParameterList("testCaseIds", testCaseIds, LongType.INSTANCE);
			List<Long> itpHavingNoExecIds = query2.list();
			
			setNullCallingIterationItemTestPlanHavingExecutions(itpHavingExecIds);
			removeCallingIterationItemTestPlanHavingNoExecutions(itpHavingNoExecIds);
		}
		
	}

	
	private void setNullCallingIterationItemTestPlanHavingExecutions(List<Long> itpHavingExecIds){
		if (! itpHavingExecIds.isEmpty()){
			executeDeleteSQLQuery(NativeQueries.testCase_sql_setNullCallingIterationItemTestPlanHavingExecutions, "itpHavingExecIds", itpHavingExecIds);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void removeCallingIterationItemTestPlanHavingNoExecutions(List<Long> itpHavingNoExecIds){
		if (!itpHavingNoExecIds.isEmpty()){
			
			Query query0 = getSession().createSQLQuery(NativeQueries.testCase_sql_getCallingIterationItemTestPlanOrderOffset);
			query0.setParameterList("itpHavingNoExecIds1", itpHavingNoExecIds);
			query0.setParameterList("itpHavingNoExecIds2", itpHavingNoExecIds);
			List<Object[]> pairIdOffset = query0.list();
			
			Map<Integer, List<Long>> mapOffsets = buildMapOfOffsetAndIds(pairIdOffset);
			
			for (Integer offset : mapOffsets.keySet()){
				Query query = getSession().createSQLQuery(NativeQueries.testCase_sql_updateCallingIterationItemTestPlanOrder);
				query.setParameter("offset", offset, IntegerType.INSTANCE);
				query.setParameterList("itpIds", mapOffsets.get(offset), LongType.INSTANCE);
				query.executeUpdate();
			}			

			executeDeleteSQLQuery(NativeQueries.testCase_sql_removeCallingIterationItemTestPlanFromList, "itpHavingNoExecIds", itpHavingNoExecIds);
			executeDeleteSQLQuery(NativeQueries.testCase_sql_removeCallingIterationItemTestPlan, "itpHavingNoExecIds", itpHavingNoExecIds);
			
		}
	}


	@Override
	public void setNullCallingExecutionSteps(List<Long> testStepIds) {
		if (! testStepIds.isEmpty()){
			Query query = getSession().createSQLQuery(NativeQueries.testCase_sql_setNullCallingExecutionSteps);
			query.setParameterList("testStepIds", testStepIds, LongType.INSTANCE);
			query.executeUpdate();
		}		
	}



	@Override
	public void setNullCallingExecutions(List<Long> testCaseIds) {
		Query query = getSession().createSQLQuery(NativeQueries.testCase_sql_setNullCallingExecutions);
		query.setParameterList("testCaseIds", testCaseIds, LongType.INSTANCE);
		query.executeUpdate();
	}



	@Override
	public void removeFromVerifyingTestCaseLists(List<Long> testCaseIds) {
		if (! testCaseIds.isEmpty()){
			Query query = getSession().createSQLQuery(NativeQueries.testCase_sql_removeVerifyingTestCaseList);
			query.setParameterList("testCaseIds", testCaseIds, LongType.INSTANCE);
			query.executeUpdate();
			
		}
	}
	
	

}
