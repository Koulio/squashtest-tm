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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.domain.execution.ExecutionStatusReport;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.infrastructure.filter.CollectionFilter;
import org.squashtest.csp.tm.internal.repository.ExecutionDao;

@Repository
public class HibernateExecutionDao extends HibernateEntityDao<Execution> implements ExecutionDao {

	/*
	 * as long as the ordering of a collection is managed by @OrderColumn, but you can't explicitely reference the
	 * ordering column in the join table, initialize the collection itself is the only solution
	 *
	 * (non-Javadoc)
	 *
	 * @see org.squashtest.csp.tm.internal.repository.ExecutionDao#findOrderedExecutionStepsByExecutionId(long)
	 */
	@Override
	public List<ExecutionStep> findExecutionSteps(long executionId) {
		Execution execution = findById(executionId);
		List<ExecutionStep> toReturn = new LinkedList<ExecutionStep>();

		for (ExecutionStep step : execution.getSteps()) {
			Hibernate.initialize(step);
			toReturn.add(step);
		}

		return toReturn;
	}

	@Override
	public Execution findAndInit(long executionId) {
		Execution execution = findById(executionId);
		Hibernate.initialize(execution.getReferencedTestCase());
		Hibernate.initialize(execution.getSteps());
		return execution;
	}

	@Override
	public int findExecutionRank(long executionId) {
		IterationTestPlanItem testPlan = (IterationTestPlanItem) currentSession().createCriteria(IterationTestPlanItem.class)
				.createCriteria("executions").add(Restrictions.eq("id", executionId)).uniqueResult();

		int index = 0;
		for (Execution execution : testPlan.getExecutions()) {
			if (execution.getId().equals(executionId)) {
				return index;
			}
			index++;
		}
		return index;
	}

	@Override
	public ExecutionStatusReport getStatusReport(final long executionId) {

		ExecutionStatusReport report = new ExecutionStatusReport();

		Map<String, Integer> statusMap = new HashMap<String, Integer>();

		for (ExecutionStatus status : ExecutionStatus.values()) {
			final ExecutionStatus fStatus = status;

			SetQueryParametersCallback newCallBack = new SetQueryParametersCallback() {

				@Override
				public void setQueryParameters(Query query) {
					query.setLong("execId", executionId);
					query.setParameter("status", fStatus);
				}
			};

			Long lResult = executeEntityNamedQuery("execution.countStatus", newCallBack);

			Integer result = lResult.intValue();

			statusMap.put(status.name(), result);
		}

		report.setBloqued(statusMap.get(ExecutionStatus.BLOCKED.name()));
		report.setFailure(statusMap.get(ExecutionStatus.FAILURE.name()));
		report.setSuccess(statusMap.get(ExecutionStatus.SUCCESS.name()));
		report.setRunning(statusMap.get(ExecutionStatus.RUNNING.name()));
		report.setReady(statusMap.get(ExecutionStatus.READY.name()));

		return report;
	}

	@Override
	public Integer countSuccess(final long executionId) {
		SetQueryParametersCallback newCallBack = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setLong("execId", executionId);
				query.setParameter("status", ExecutionStatus.SUCCESS.name());
			}
		};

		return executeEntityNamedQuery("execution.countStatus", newCallBack);
	}

	@Override
	public Integer countReady(final long executionId) {
		SetQueryParametersCallback newCallBack = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setLong("execId", executionId);
				query.setParameter("status", ExecutionStatus.READY.name());
			}
		};

		return executeEntityNamedQuery("execution.Status", newCallBack);
	}

	
	/* same than for HibernateTestCaseDao#findStepsByIdFiltered :
	 * 
	 * because we need to get the ordered list and we can't access the join table to sort them (again),
	 * we can't use the Criteria API. So we're playing it old good java here.
	 * 
	 */
	
	@Override
	public List<ExecutionStep> findStepsFiltered(final Long executionId,
			final CollectionFilter filter) {
		
		Execution execution = findById(executionId);
		int listSize=execution.getSteps().size();
		
		int startIndex = filter.getFirstItemIndex();
		int lastIndex = filter.getFirstItemIndex()+filter.getMaxNumberOfItems();
		
		//prevent IndexOutOfBoundException :
		if (startIndex>=listSize){
			return new LinkedList<ExecutionStep>(); //ie resultset is empty
		}
		if (lastIndex>=listSize){
			lastIndex=listSize;
		}
		
		return execution.getSteps().subList(startIndex,lastIndex);

	}

}
