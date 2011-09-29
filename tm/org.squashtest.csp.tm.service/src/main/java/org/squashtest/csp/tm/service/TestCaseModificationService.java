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
package org.squashtest.csp.tm.service;

import java.util.List;

import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.csp.tm.domain.testcase.TestStep;
import org.squashtest.csp.tm.infrastructure.filter.CollectionFilter;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;

public interface TestCaseModificationService {

	/* *************** TestCase section **************************** */

	TestCase findTestCaseById(long testCaseId);

	TestCase findTestCaseWithSteps(long testCaseId);

	List<TestStep> findStepsByTestCaseId(long testCaseId);

	FilteredCollectionHolder<List<TestStep>> findStepsByTestCaseIdFiltered(long testCaseId, CollectionFilter filter);

	void updateTestCaseName(long testCaseId, String newName);

	void updateTestCaseDescription(long testCaseId, String newDescription);

	void updateTestCaseExecutionMode(long testCaseId, TestCaseExecutionMode mode);


	
	
	/* *************** TestStep section ***************************** */

	TestStep addActionTestStep(long parentTestCaseId, ActionTestStep newTestStep);
	
	void updateTestStepAction(long testStepId, String newAction);

	void updateTestStepExpectedResult(long testStepId, String newExpectedResult);

	@Deprecated
	void changeTestStepPosition(long testCaseId, long testStepId, int newStepPosition);

	void changeTestStepsPosition(long testCaseId, int newPosition, List<Long> stepIds);
	
	void removeStepFromTestCase(long testCaseId, long testStepId);

	void removeListOfSteps(long testCaseId, List<Long> testStepIds);

	/**
	 * Returns the filtered list of {@link Requirement}s directly verified by a test case.
	 *
	 * @param testCaseId
	 * @param filter
	 * @return
	 */
	

	FilteredCollectionHolder<List<Requirement>> findAllDirectlyVerifiedRequirementsByTestCaseId(long testCaseId,
			CollectionSorting filter);

	FilteredCollectionHolder<List<VerifiedRequirement>> findAllVerifiedRequirementsByTestCaseId(long testCaseId,
			CollectionSorting sorting);
	
	/**
	 * That method returns the list of test cases having at least one CallTestStep directly calling the 
	 * test case identified by testCaseId. The list is wrapped in a FilteredCollectionHolder, that contains
	 * meta informations regarding the filtering, as usual.
	 *  
	 * @param testCaseId the Id of the called test case.
	 * @param sorting the sorting parameters.
	 * @return a non null but possibly empty FilteredCollectionHolder wrapping the list of first-level calling test cases.
	 */
	FilteredCollectionHolder<List<TestCase>> findCallingTestCases(long testCaseId, CollectionSorting sorting);

	/**
	 * will insert a test step into a test case script, possibly after a step (the position), given their Ids. If no
	 * position is provided, it defaults to the first position.
	 *
	 * @param testCaseId
	 *            the id of the test case.
	 * @param idToCopyAfter
	 *            the id of the step after which we'll insert the copy of a step, may be null.
	 * @param copiedTestStepId
	 *            the id of the testStep to copy.
	 *
	 */
	void pasteCopiedTestStep(Long testCaseId, Long idToCopyAfter, Long copiedTestStepId);

}
