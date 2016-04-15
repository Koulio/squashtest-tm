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
package org.squashtest.tm.service.internal.campaign;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.DefaultColumnFiltering;
import org.squashtest.tm.core.foundation.collection.DefaultFiltering;
import org.squashtest.tm.core.foundation.collection.DelegatePagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.MultiSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Pagings;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.service.campaign.IndexedIterationTestPlanItem;
import org.squashtest.tm.service.campaign.IterationTestPlanManagerService;
import org.squashtest.tm.service.campaign.TestSuiteTestPlanManagerService;
import org.squashtest.tm.service.internal.repository.IterationTestPlanDao;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.user.UserAccountService;

@Service("squashtest.tm.service.TestSuiteTestPlanManagerService")
@Transactional
public class TestSuiteTestPlanManagerServiceImpl implements TestSuiteTestPlanManagerService {

	private static final String HAS_LINK_PERMISSION_ID = "hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'LINK') ";
	private static final String HAS_LINK_PERMISSION_OBJECT = "hasPermission(#testSuite, 'LINK') ";

	@Inject
	private IterationTestPlanManagerService delegateIterationTestPlanManagerService;

	@Inject
	private TestSuiteDao testSuiteDao;

	@Inject
	private UserAccountService userService;

	@Inject
	private IterationTestPlanDao itemTestPlanDao;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Override
	@PreAuthorize("hasPermission(#testSuiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'READ') "
			+ OR_HAS_ROLE_ADMIN)
	public TestSuite findTestSuite(long testSuiteId) {
		return testSuiteDao.findById(testSuiteId);
	}

	@Override
	@PreAuthorize(HAS_LINK_PERMISSION_ID + OR_HAS_ROLE_ADMIN)
	public void bindTestPlan(long suiteId, List<Long> itemTestPlanIds) {
		TestSuite suite = testSuiteDao.findById(suiteId);
		suite.bindTestPlanItemsById(itemTestPlanIds);
	}

	@Override()
	public void bindTestPlanToMultipleSuites(List<Long> suiteIds, List<Long> itemTestPlanIds) {

		for (Long id : suiteIds) {
			bindTestPlan(id, itemTestPlanIds);
		}
	}

	@Override
	@PreAuthorize(HAS_LINK_PERMISSION_OBJECT + OR_HAS_ROLE_ADMIN)
	public void bindTestPlanObj(TestSuite testSuite, List<IterationTestPlanItem> itemTestPlans) {
		testSuite.bindTestPlanItems(itemTestPlans);
	}

	@Override()
	public void bindTestPlanToMultipleSuitesObj(List<TestSuite> testSuites, List<IterationTestPlanItem> itemTestPlans) {

		for (TestSuite suite : testSuites) {
			bindTestPlanObj(suite, itemTestPlans);
		}
	}

	@Override
	@PreAuthorize(HAS_LINK_PERMISSION_OBJECT + OR_HAS_ROLE_ADMIN)
	public void unbindTestPlanObj(TestSuite testSuite, List<IterationTestPlanItem> itemTestPlans) {
		testSuite.unBindTestPlan(itemTestPlans);
	}

	@Override
	public void unbindTestPlanToMultipleSuites(List<Long> unboundTestSuiteIds, List<Long> itpIds) {
		List<TestSuite> unboundTestSuites = testSuiteDao.findAllByIds(unboundTestSuiteIds);
		List<IterationTestPlanItem> iterationTestPlanItems = itemTestPlanDao.findAllByIdIn(itpIds);
		for (TestSuite suite : unboundTestSuites) {
			unbindTestPlanObj(suite, iterationTestPlanItems);
		}

	}

	/**
	 * @see TestSuiteTestPlanManagerService#findAssignedTestPlan(long, PagingAndMultiSorting)
	 **/
	@Override
	public PagedCollectionHolder<List<IndexedIterationTestPlanItem>> findAssignedTestPlan(long testSuiteId,
			PagingAndMultiSorting sorting, ColumnFiltering columnFiltering) {

		// configure the filter, in case the test plan must be restricted to what the user can see.
		Filtering userFiltering = DefaultFiltering.NO_FILTERING;
		try {
			PermissionsUtils.checkPermission(permissionEvaluationService, Arrays.asList(testSuiteId),
					"READ_UNASSIGNED", TestSuite.class.getCanonicalName());
		} catch (AccessDeniedException ade) {
			String userLogin = userService.findCurrentUser().getLogin();
			userFiltering = new DefaultFiltering("User.login", userLogin);
		}

		List<IndexedIterationTestPlanItem> indexedItems = testSuiteDao.findIndexedTestPlan(testSuiteId, sorting,
				userFiltering, columnFiltering);
		long testPlanSize = testSuiteDao.countTestPlans(testSuiteId, userFiltering, columnFiltering);

		return new PagingBackedPagedCollectionHolder<List<IndexedIterationTestPlanItem>>(sorting, testPlanSize,
				indexedItems);
	}

	@Override
	@PreAuthorize(HAS_LINK_PERMISSION_ID + OR_HAS_ROLE_ADMIN)
	public void changeTestPlanPosition(long suiteId, int newIndex, List<Long> itemIds) {

		TestSuite suite = testSuiteDao.findById(suiteId);

		List<IterationTestPlanItem> items = testSuiteDao.findTestPlanPartition(suiteId, itemIds);

		suite.reorderTestPlan(newIndex, items);
	}

	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'LINK') " + OR_HAS_ROLE_ADMIN)
	public void reorderTestPlan(long suiteId, MultiSorting newSorting) {

		Paging noPaging = Pagings.NO_PAGING;
		PagingAndMultiSorting sorting = new DelegatePagingAndMultiSorting(noPaging, newSorting);
		Filtering filtering = DefaultFiltering.NO_FILTERING;
		ColumnFiltering columnFiltering = DefaultColumnFiltering.NO_FILTERING;

		List<IterationTestPlanItem> items = testSuiteDao.findTestPlan(suiteId, sorting, filtering, columnFiltering);

		TestSuite testSuite = testSuiteDao.findById(suiteId);

		testSuite.getTestPlan().clear();
		testSuite.getTestPlan().addAll(items);
	}

	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'LINK') " + OR_HAS_ROLE_ADMIN)
	public void addTestCasesToIterationAndTestSuite(List<Long> testCaseIds, long suiteId) {

		TestSuite testSuite = testSuiteDao.findById(suiteId);

		Iteration iteration = testSuite.getIteration();

		List<IterationTestPlanItem> listTestPlanItemsToAffectToTestSuite = delegateIterationTestPlanManagerService
				.addTestPlanItemsToIteration(testCaseIds, iteration);

		bindTestPlanObj(testSuite, listTestPlanItemsToAffectToTestSuite);
	}

	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'LINK') " + OR_HAS_ROLE_ADMIN)
	public void detachTestPlanFromTestSuite(List<Long> testPlanIds, long suiteId) {

		TestSuite testSuite = testSuiteDao.findById(suiteId);
		List<IterationTestPlanItem> listTestPlanItems = new ArrayList<>();

		for (long testPlanId : testPlanIds) {
			IterationTestPlanItem iterTestPlanItem = itemTestPlanDao.findById(testPlanId);
			listTestPlanItems.add(iterTestPlanItem);
		}

		unbindTestPlanObj(testSuite, listTestPlanItems);
	}

	@Override
	@PreAuthorize("hasPermission(#suiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'LINK') " + OR_HAS_ROLE_ADMIN)
	public boolean detachTestPlanFromTestSuiteAndRemoveFromIteration(List<Long> testPlanIds, long suiteId) {
		TestSuite testSuite = testSuiteDao.findById(suiteId);

		Iteration iteration = testSuite.getIteration();

		return delegateIterationTestPlanManagerService.removeTestPlansFromIterationObj(testPlanIds, iteration);
	}

	/**
	 * @see TestSuiteTestPlanManagerService#findPlannedTestCasesIds(Long)
	 */
	@Override
	@Transactional(readOnly = true)
	public	List<Long> findPlannedTestCasesIds(Long suiteId) {
		return testSuiteDao.findPlannedTestCasesIds(suiteId);
	}

}