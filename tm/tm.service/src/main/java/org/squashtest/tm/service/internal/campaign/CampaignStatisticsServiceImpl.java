/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.internal.campaign;


import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.LongType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.campaign.CampaignStatisticsService;
import org.squashtest.tm.service.internal.repository.CampaignDao;
import org.squashtest.tm.service.statistics.campaign.CampaignNonExecutedTestCaseImportanceStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignProgressionStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignStatisticsBundle;
import org.squashtest.tm.service.statistics.campaign.CampaignTestCaseStatusStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignTestCaseSuccessRateStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignTestInventoryStatistics;
import org.squashtest.tm.service.statistics.campaign.IterationTestInventoryStatistics;
import org.squashtest.tm.service.statistics.campaign.ManyCampaignStatisticsBundle;
import org.squashtest.tm.service.statistics.campaign.ScheduledIteration;

@Transactional(readOnly=true)
@Service("CampaignStatisticsService")
public class CampaignStatisticsServiceImpl implements CampaignStatisticsService{

	private static final String PERM_CAN_READ_CAMPAIGN = "hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign', 'READ') ";


	private static final Logger LOGGER = LoggerFactory.getLogger(CampaignStatisticsService.class);


	@Inject
	private SessionFactory sessionFactory;

	@Inject
	private CampaignDao campaignDao;


	// ************************************ all-in-one methods ******************************


	@Override
	@PreAuthorize(PERM_CAN_READ_CAMPAIGN + OR_HAS_ROLE_ADMIN)
	public CampaignStatisticsBundle gatherCampaignStatisticsBundle(long campaignId) {

		CampaignStatisticsBundle bundle = new CampaignStatisticsBundle();

		// perimeter
		List<Long> campaignIds = Arrays.asList(campaignId);

		// common methods
		CampaignTestCaseStatusStatistics testcaseStatuses = gatherTestCaseStatusStatistics(campaignIds);
		CampaignNonExecutedTestCaseImportanceStatistics testcaseImportance = gatherNonExecutedTestCaseImportanceStatistics(campaignIds);
		CampaignTestCaseSuccessRateStatistics testcaseSuccessRate = gatherTestCaseSuccessRateStatistics(campaignIds);

		// specific methods
		List<IterationTestInventoryStatistics> inventory = gatherCampaignTestInventoryStatistics(campaignId);
		CampaignProgressionStatistics progression = gatherCampaignProgressionStatistics(campaignId);

		// stuff it all
		bundle.setIterationTestInventoryStatisticsList(inventory);
		bundle.setCampaignProgressionStatistics(progression);
		bundle.setCampaignTestCaseStatusStatistics(testcaseStatuses);
		bundle.setCampaignNonExecutedTestCaseImportanceStatistics(testcaseImportance);
		bundle.setCampaignTestCaseSuccessRateStatistics(testcaseSuccessRate);

		// return
		return bundle;

	}


	@Override
	// TODO : security ? If never exposed through OSGI it might not be necessary
	public CampaignStatisticsBundle gatherMilestoneStatisticsBundle(long milestoneId) {

		CampaignStatisticsBundle bundle = new CampaignStatisticsBundle();

		// perimeter
		List<Long> campaignIds = campaignDao.findAllIdsByMilestone(milestoneId);

		// common methods
		CampaignTestCaseStatusStatistics testcaseStatuses = gatherTestCaseStatusStatistics(campaignIds);
		CampaignNonExecutedTestCaseImportanceStatistics testcaseImportance = gatherNonExecutedTestCaseImportanceStatistics(campaignIds);
		CampaignTestCaseSuccessRateStatistics testcaseSuccessRate = gatherTestCaseSuccessRateStatistics(campaignIds);

		// specific methods
		List<IterationTestInventoryStatistics> inventory = gatherMilestoneTestInventoryStatistics(milestoneId);
		CampaignProgressionStatistics progression = new CampaignProgressionStatistics(); // not used in the by-milestone dashboard

		// stuff it all
		bundle.setIterationTestInventoryStatisticsList(inventory);
		bundle.setCampaignProgressionStatistics(progression);
		bundle.setCampaignTestCaseStatusStatistics(testcaseStatuses);
		bundle.setCampaignNonExecutedTestCaseImportanceStatistics(testcaseImportance);
		bundle.setCampaignTestCaseSuccessRateStatistics(testcaseSuccessRate);

		// return
		return bundle;

	}


	@Override
	// TODO : security ? If never exposed through OSGI it might not be necessary
	public ManyCampaignStatisticsBundle gatherFolderStatisticsBundleByMilestone(Long folderId, Long milestoneId) {

		ManyCampaignStatisticsBundle bundle = new ManyCampaignStatisticsBundle();

		// perimeter
		List<Long> campaignIds = campaignDao.findAllCampaignIdsByNodeIds(Arrays.asList(folderId));
		campaignIds = campaignDao.filterByMilestone(campaignIds, milestoneId);

		// common methods
		CampaignTestCaseStatusStatistics testcaseStatuses = gatherTestCaseStatusStatistics(campaignIds);
		CampaignNonExecutedTestCaseImportanceStatistics testcaseImportance = gatherNonExecutedTestCaseImportanceStatistics(campaignIds);
		CampaignTestCaseSuccessRateStatistics testcaseSuccessRate = gatherTestCaseSuccessRateStatistics(campaignIds);

		// specific methods
		List<CampaignTestInventoryStatistics> inventory = gatherFolderTestInventoryStatistics(campaignIds);
		CampaignProgressionStatistics progression = new CampaignProgressionStatistics(); // not used in the by-milestone dashboard

		// stuff it all
		bundle.setIterationTestInventoryStatisticsList(inventory);
		bundle.setCampaignProgressionStatistics(progression);
		bundle.setCampaignTestCaseStatusStatistics(testcaseStatuses);
		bundle.setCampaignNonExecutedTestCaseImportanceStatistics(testcaseImportance);
		bundle.setCampaignTestCaseSuccessRateStatistics(testcaseSuccessRate);

		// return
		return bundle;
	}

	/* *********************************** common statistics methods ************************************ */

	/**
	 * Given a list of campaign id, gathers and returns the number of test cases grouped by execution status.
	 * 
	 * @param campaignIds
	 * @return
	 */
	public CampaignTestCaseStatusStatistics gatherTestCaseStatusStatistics(List<Long> campaignIds){

		List<Object[]> tuples = fetchCommonTuples("CampaignStatistics.globaltestinventory", campaignIds);

		return processTestCaseStatusStatistics(tuples);
	}


	/**
	 * Given a list of campaign id, gathers and returns the number of passed and failed test cases grouped by weight.
	 * 
	 * @param campaignIds
	 * @return
	 */
	public CampaignTestCaseSuccessRateStatistics gatherTestCaseSuccessRateStatistics(List<Long> campaignIds){
		List<Object[]> tuples = fetchCommonTuples("CampaignStatistics.successRate", campaignIds);

		return processTestCaseSuccessRateStatistics(tuples);
	}

	/**
	 * Given a list of campaign id, gathers and returns the number of non-executed test cases grouped by weight.
	 * 
	 * @param campaignIds
	 * @return
	 */
	public CampaignNonExecutedTestCaseImportanceStatistics gatherNonExecutedTestCaseImportanceStatistics(List<Long> campaignIds){
		List<Object[]> tuples = fetchCommonTuples("CampaignStatistics.nonexecutedTestcaseImportance", campaignIds);

		return processNonExecutedTestCaseImportance(tuples);
	}



	/* ************************************* statistics specific to one lone campaign************************************** */

	@Override
	@PreAuthorize(PERM_CAN_READ_CAMPAIGN + OR_HAS_ROLE_ADMIN)
	public CampaignProgressionStatistics gatherCampaignProgressionStatistics(long campaignId) {

		CampaignProgressionStatistics progression = new CampaignProgressionStatistics();

		Session session = sessionFactory.getCurrentSession();

		Query query = session.getNamedQuery("CampaignStatistics.findScheduledIterations");
		query.setParameter("id", campaignId, LongType.INSTANCE);
		List<ScheduledIteration> scheduledIterations = query.list();

		//TODO : have the db do the job for me
		Query requery = session.getNamedQuery("CampaignStatistics.findExecutionsHistory");
		requery.setParameter("id", campaignId, LongType.INSTANCE);
		requery.setParameterList("nonterminalStatuses", ExecutionStatus.getNonTerminatedStatusSet());
		List<Date> executionHistory = requery.list();

		try{

			// scheduled iterations
			progression.setScheduledIterations(scheduledIterations);	//we want them in any case
			ScheduledIteration.checkIterationsDatesIntegrity(scheduledIterations);

			progression.computeSchedule();

			// actual executions
			progression.computeCumulativeTestPerDate(executionHistory);


		}catch(IllegalArgumentException ex){
			if (LOGGER.isInfoEnabled()){
				LOGGER.info("CampaignStatistics : could not generate campaign progression statistics for campaign "+campaignId+" : some iterations scheduled dates are wrong");
			}
			progression.addi18nErrorMessage(ex.getMessage());
		}

		return progression;

	}

	@Override
	@PreAuthorize(PERM_CAN_READ_CAMPAIGN + OR_HAS_ROLE_ADMIN)
	public List<IterationTestInventoryStatistics> gatherCampaignTestInventoryStatistics(long campaignId) {

		Query query = sessionFactory.getCurrentSession().getNamedQuery("CampaignStatistics.testinventory");
		query.setParameter("id", campaignId);
		List<Object[]> tuples = query.list();


		return processIterationTestInventory(tuples);
	}



	/* ************************ statistics specific to all campaigns of one milestone******************************** */

	@Override
	@PreAuthorize(PERM_CAN_READ_CAMPAIGN + OR_HAS_ROLE_ADMIN)
	public List<IterationTestInventoryStatistics> gatherMilestoneTestInventoryStatistics(long milestoneId) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("CampaignStatistics.testinventorybymilestone");
		query.setParameter("id", milestoneId);
		List<Object[]> tuples = query.list();

		return processIterationTestInventory(tuples);
	}


	/* ************************************ statistics specific to campaign folders ****************************** */


	@Override
	public List<CampaignTestInventoryStatistics> gatherFolderTestInventoryStatistics(Collection<Long> campaignIds) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("CampaignFolderStatistics.testinventory");
		query.setParameter("campaignIds", campaignIds, LongType.INSTANCE);
		List<Object[]> tuples = query.list();

		return processCampaignTestInventory(tuples);
	}


	/* *********************** processing code ***************************** */



	/**
	 * Execute a named query that accepts as argument a parameter of type List&lt;Long&gt; named "campaignIds", and that
	 * returns a list of tuples (namely Object[])
	 * 
	 * @param queryName
	 * @param idParameter
	 * @return
	 */
	private List<Object[]> fetchCommonTuples(String queryName, List<Long> campaignIds){
		Query query = sessionFactory.getCurrentSession().getNamedQuery(queryName);
		query.setParameter("campaignIds", campaignIds);
		List<Object[]> res = query.list();
		return res;
	}

	private List<IterationTestInventoryStatistics> processIterationTestInventory(List<Object[]> res) {
		/*
		 * Process. Beware that the logic is a bit awkward here. Indeed we first insert new
		 * IterationTestInventoryStatistics in the result list, then we populate them.
		 */
		IterationTestInventoryStatistics newStatistics = new IterationTestInventoryStatistics();
		Long currentId = null;

		List<IterationTestInventoryStatistics> result = new LinkedList<IterationTestInventoryStatistics>();

		for (Object[] tuple : res){
			Long id = (Long)tuple[0];

			if (! id.equals(currentId)){
				String name = (String) tuple[1];
				newStatistics = new IterationTestInventoryStatistics();
				newStatistics.setIterationName(name);
				result.add(newStatistics);
				currentId = id;
			}

			ExecutionStatus status = (ExecutionStatus)tuple[2];
			Long howmany = (Long)tuple[3];

			if (status == null){
				continue;	// status == null iif the test plan is empty
			}
			newStatistics.setNumber(howmany.intValue(), status);


		}
		return result;
	};

	// copy-pasta of processIterationCampaignTestInventory. Java 8, where are you
	// when I need you ?
	private List<CampaignTestInventoryStatistics> processCampaignTestInventory(List<Object[]> res) {
		/*
		 * Process. Beware that the logic is a bit awkward here. Indeed we first insert new
		 * IterationTestInventoryStatistics in the result list, then we populate them.
		 */
		CampaignTestInventoryStatistics newStatistics = new CampaignTestInventoryStatistics();
		Long currentId = null;

		List<CampaignTestInventoryStatistics> result = new LinkedList<CampaignTestInventoryStatistics>();

		for (Object[] tuple : res){
			Long id = (Long)tuple[0];

			if (! id.equals(currentId)){
				String name = (String) tuple[1];
				newStatistics = new CampaignTestInventoryStatistics();
				newStatistics.setCampaignName(name);
				result.add(newStatistics);
				currentId = id;
			}

			ExecutionStatus status = (ExecutionStatus)tuple[2];
			Long howmany = (Long)tuple[3];

			if (status == null){
				continue;	// status == null iif the test plan is empty
			}
			newStatistics.setNumber(howmany.intValue(), status);


		}
		return result;
	};

	private CampaignTestCaseStatusStatistics processTestCaseStatusStatistics(List<Object[]> tuples) {
		CampaignTestCaseStatusStatistics result = new CampaignTestCaseStatusStatistics();

		for (Object[] tuple : tuples){

			ExecutionStatus status = (ExecutionStatus)tuple[0];
			Long howmany = (Long)tuple[1];

			result.addNumber(howmany.intValue(), status.getCanonicalStatus());
		}

		return result;
	}

	private CampaignNonExecutedTestCaseImportanceStatistics processNonExecutedTestCaseImportance(List<Object[]> tuples) {
		CampaignNonExecutedTestCaseImportanceStatistics result = new CampaignNonExecutedTestCaseImportanceStatistics();

		for (Object[] tuple : tuples){

			TestCaseImportance importance = (TestCaseImportance)tuple[0];
			Long howmany = (Long)tuple[1];

			switch(importance){
			case HIGH: result.setPercentageHigh(howmany.intValue()); break;
			case LOW: result.setPercentageLow(howmany.intValue()); break;
			case MEDIUM: result.setPercentageMedium(howmany.intValue()); break;
			case VERY_HIGH: result.setPercentageVeryHigh(howmany.intValue()); break;
			}
		}

		return result;
	}


	private CampaignTestCaseSuccessRateStatistics processTestCaseSuccessRateStatistics(List<Object[]> tuples) {
		CampaignTestCaseSuccessRateStatistics result = new CampaignTestCaseSuccessRateStatistics();

		for (Object[] tuple : tuples){

			TestCaseImportance importance = (TestCaseImportance)tuple[0];
			ExecutionStatus status = (ExecutionStatus)tuple[1];
			Long howmany = (Long)tuple[2];

			switch(importance){
			case HIGH: result.addNbHigh(status, howmany.intValue()); break;
			case LOW: result.addNbLow(status, howmany.intValue()); break;
			case MEDIUM: result.addNbMedium(status, howmany.intValue()); break;
			case VERY_HIGH: result.addNbVeryHigh(status, howmany.intValue()); break;
			}
		}

		return result;
	}




}
