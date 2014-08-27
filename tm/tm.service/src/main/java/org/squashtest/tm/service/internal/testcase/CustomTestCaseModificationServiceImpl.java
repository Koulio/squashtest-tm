/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.internal.testcase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.core.foundation.lang.PathUtils;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.domain.testcase.TestStepVisitor;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.exception.UnallowedTestAssociationException;
import org.squashtest.tm.exception.testautomation.MalformedScriptPathException;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.library.NodeManagementService;
import org.squashtest.tm.service.internal.repository.ActionTestStepDao;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.TestStepDao;
import org.squashtest.tm.service.internal.testautomation.UnsecuredAutomatedTestManagerService;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;
import org.squashtest.tm.service.testcase.CustomTestCaseModificationService;
import org.squashtest.tm.service.testcase.ParameterModificationService;
import org.squashtest.tm.service.testcase.TestCaseImportanceManagerService;

/**
 * @author Gregory Fouquet
 * 
 */
@Service("CustomTestCaseModificationService")
@Transactional
public class CustomTestCaseModificationServiceImpl implements CustomTestCaseModificationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomTestCaseModificationServiceImpl.class);
	private static final String WRITE_TC_OR_ROLE_ADMIN = "hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')";

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	@Qualifier("squashtest.tm.repository.TestCaseLibraryNodeDao")
	private LibraryNodeDao<TestCaseLibraryNode> testCaseLibraryNodeDao;

	@Inject
	private ActionTestStepDao actionStepDao;

	@Inject
	private TestCaseImportanceManagerService testCaseImportanceManagerService;

	@Inject
	private TestStepDao testStepDao;

	@Inject
	private RequirementVersionDao requirementVersionDao;

	@Inject
	@Named("squashtest.tm.service.internal.TestCaseManagementService")
	private NodeManagementService<TestCase, TestCaseLibraryNode, TestCaseFolder> testCaseManagementService;

	@Inject
	private TestCaseNodeDeletionHandler deletionHandler;

	@Inject
	private UnsecuredAutomatedTestManagerService taService;

	@Inject
	protected PrivateCustomFieldValueService customFieldValuesService;

	@Inject
	private TestCaseCallTreeFinder callTreeFinder;

	@Inject
	private ParameterModificationService parameterModificationService;

	/* *************** TestCase section ***************************** */

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public void rename(long testCaseId, String newName) throws DuplicateNameException {
		testCaseManagementService.renameNode(testCaseId, newName);
	}

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'READ') or hasRole('ROLE_ADMIN')")
	@Transactional(readOnly = true)
	public List<TestStep> findStepsByTestCaseId(long testCaseId) {

		TestCase testCase = testCaseDao.findByIdWithInitializedSteps(testCaseId);
		return testCase.getSteps();

	}

	/* *************** TestStep section ***************************** */

	@Override
	@PreAuthorize("hasPermission(#parentTestCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public ActionTestStep addActionTestStep(long parentTestCaseId, ActionTestStep newTestStep) {
		TestCase parentTestCase = testCaseDao.findById(parentTestCaseId);

		testStepDao.persist(newTestStep);
		// will throw a nasty NullPointerException if the parent test case can't
		// be found
		parentTestCase.addStep(newTestStep);
		customFieldValuesService.createAllCustomFieldValues(newTestStep, newTestStep.getProject());
		parameterModificationService.createParamsForStep(newTestStep.getId());
		return newTestStep;
	}

	@Override
	@PreAuthorize("hasPermission(#parentTestCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public ActionTestStep addActionTestStep(long parentTestCaseId, ActionTestStep newTestStep,
			Map<Long, String> customFieldValues) {

		ActionTestStep step = addActionTestStep(parentTestCaseId, newTestStep);
		initCustomFieldValues((ActionTestStep) step, customFieldValues);
		parameterModificationService.createParamsForStep(step.getId());
		return step;
	}

	@Override
	@PreAuthorize("hasPermission(#testStepId, 'org.squashtest.tm.domain.testcase.TestStep' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public void updateTestStepAction(long testStepId, String newAction) {
		ActionTestStep testStep = actionStepDao.findById(testStepId);
		testStep.setAction(newAction);
		parameterModificationService.createParamsForStep(testStepId);
	}

	@Override
	@PreAuthorize("hasPermission(#testStepId, 'org.squashtest.tm.domain.testcase.TestStep' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public void updateTestStepExpectedResult(long testStepId, String newExpectedResult) {
		ActionTestStep testStep = actionStepDao.findById(testStepId);
		testStep.setExpectedResult(newExpectedResult);
		parameterModificationService.createParamsForStep(testStepId);
	}

	@Override
	@Deprecated
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public void changeTestStepPosition(long testCaseId, long testStepId, int newStepPosition) {
		TestCase testCase = testCaseDao.findById(testCaseId);
		int index = findTestStepInTestCase(testCase, testStepId);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("**************** change step order : old index = " + index + ",new index : "
					+ newStepPosition);
		}

		testCase.moveStep(index, newStepPosition);
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public void changeTestStepsPosition(long testCaseId, int newPosition, List<Long> stepIds) {

		TestCase testCase = testCaseDao.findById(testCaseId);
		List<TestStep> steps = testStepDao.findListById(stepIds);

		testCase.moveSteps(newPosition, steps);

	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public void removeStepFromTestCase(long testCaseId, long testStepId) {
		TestCase testCase = testCaseDao.findById(testCaseId);
		TestStep testStep = testStepDao.findById(testStepId);
		deletionHandler.deleteStep(testCase, testStep);
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public void removeStepFromTestCaseByIndex(long testCaseId, int index) {
		TestCase testCase = testCaseDao.findById(testCaseId);
		TestStep testStep = testCase.getSteps().get(index);
		deletionHandler.deleteStep(testCase, testStep);
	}

	/*
	 * given a TestCase, will search for a TestStep in the steps list (identified with its testStepId)
	 * 
	 * returns : the index if found, -1 if not found or if the provided TestCase is null
	 */
	@Transactional(readOnly = true)
	private int findTestStepInTestCase(TestCase testCase, long testStepId) {
		return testCase.getPositionOfStep(testStepId);
	}

	@Override
	@PostAuthorize("hasPermission(returnObject, 'READ') or hasRole('ROLE_ADMIN')")
	@Transactional(readOnly = true)
	public TestCase findTestCaseWithSteps(long testCaseId) {
		return testCaseDao.findAndInit(testCaseId);
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public List<TestStep> removeListOfSteps(long testCaseId, List<Long> testStepIds) {
		TestCase testCase = testCaseDao.findById(testCaseId);

		for (Long id : testStepIds) {
			TestStep step = testStepDao.findById(id);
			deletionHandler.deleteStep(testCase, step);
		}
		return testCase.getSteps();
	}

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'READ') or hasRole('ROLE_ADMIN')")
	@Transactional(readOnly = true)
	public PagedCollectionHolder<List<TestStep>> findStepsByTestCaseIdFiltered(long testCaseId, Paging paging) {
		List<TestStep> list = testCaseDao.findAllStepsByIdFiltered(testCaseId, paging);
		long count = findStepsByTestCaseId(testCaseId).size();
		return new PagingBackedPagedCollectionHolder<List<TestStep>>(paging, count, list);
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public boolean pasteCopiedTestStep(long testCaseId, long idToCopyAfter, long copiedTestStepId) {
		Integer position = testStepDao.findPositionOfStep(idToCopyAfter) + 1;
		return pasteTestStepAtPosition(testCaseId, copiedTestStepId, position);
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public boolean pasteCopiedTestStepToLastIndex(long testCaseId, long copiedTestStepId) {
		return pasteTestStepAtPosition(testCaseId, copiedTestStepId, null);

	}

	// FIXME il faut vérifier un éventuel cycle ! // pour l'instant vérifié au
	// niveau du controller
	/**
	 * 
	 * @param testCaseId
	 * @param copiedTestStepId
	 * @param position
	 * @return true if copied step is instance of CallTestStep
	 */
	private boolean pasteTestStepAtPosition(long testCaseId, long copiedTestStepId, Integer position) {
		TestStep original = testStepDao.findById(copiedTestStepId);
		TestStep copyStep = original.createCopy();
		testStepDao.persist(copyStep);
		TestCase testCase = testCaseDao.findById(testCaseId);
		if (position != null) {
			try {
				testCase.addStep(position, copyStep);
			} catch (IndexOutOfBoundsException ex) {
				testCase.addStep(copyStep);
			}
		} else {
			testCase.addStep(copyStep);
		}

		if (!testCase.getSteps().contains(original)) {
			updateImportanceIfCallStep(testCase, copyStep);
			parameterModificationService.createParamsForStep(copyStep);
		}

		copyStep.accept(new TestStepCustomFieldCopier(original));
		return copyStep instanceof CallTestStep;
	}

	private final class TestStepCustomFieldCopier implements TestStepVisitor {
		TestStep original;

		private TestStepCustomFieldCopier(TestStep original) {
			this.original = original;
		}

		@Override
		public void visit(ActionTestStep visited) {
			customFieldValuesService.copyCustomFieldValues((ActionTestStep) original, visited);
			Project origProject = original.getTestCase().getProject();
			Project newProject = visited.getTestCase().getProject();

			if (!origProject.equals(newProject)) {
				customFieldValuesService.migrateCustomFieldValues(visited);
			}
		}

		@Override
		public void visit(CallTestStep visited) {
			// NOPE
		}

	}

	private void updateImportanceIfCallStep(TestCase parentTestCase, TestStep copyStep) {
		if (copyStep instanceof CallTestStep) {
			TestCase called = ((CallTestStep) copyStep).getCalledTestCase();
			testCaseImportanceManagerService.changeImportanceIfCallStepAddedToTestCases(called, parentTestCase);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public PagedCollectionHolder<List<TestCase>> findCallingTestCases(long testCaseId, PagingAndSorting sorting) {

		List<TestCase> callers = testCaseDao.findAllCallingTestCases(testCaseId, sorting);
		Long countCallers = testCaseDao.countCallingTestSteps(testCaseId);
		return new PagingBackedPagedCollectionHolder<List<TestCase>>(sorting, countCallers, callers);

	}

	@Override
	public PagedCollectionHolder<List<CallTestStep>> findCallingTestSteps(long testCaseId, PagingAndSorting sorting) {
		List<CallTestStep> callers = testCaseDao.findAllCallingTestSteps(testCaseId, sorting);
		Long countCallers = testCaseDao.countCallingTestSteps(testCaseId);
		return new PagingBackedPagedCollectionHolder<List<CallTestStep>>(sorting, countCallers, callers);
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public void changeImportanceAuto(long testCaseId, boolean auto) {
		TestCase testCase = testCaseDao.findById(testCaseId);
		testCase.setImportanceAuto(auto);
		testCaseImportanceManagerService.changeImportanceIfIsAuto(testCaseId);
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public Collection<TestAutomationProjectContent> findAssignableAutomationTests(long testCaseId) {

		TestCase testCase = testCaseDao.findById(testCaseId);

		return taService.listTestsInProjects(testCase.getProject().getTestAutomationProjects());
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public AutomatedTest bindAutomatedTest(Long testCaseId, Long taProjectId, String testName) {


		TestAutomationProject project = taService.findProjectById(taProjectId);

		AutomatedTest newTest = new AutomatedTest(testName, project);

		AutomatedTest persisted = taService.persistOrAttach(newTest);

		TestCase testCase = testCaseDao.findById(testCaseId);

		AutomatedTest previousTest = testCase.getAutomatedTest();

		testCase.setAutomatedTest(persisted);

		taService.removeIfUnused(previousTest);

		return newTest;
	}

	@Override
	@PreAuthorize(WRITE_TC_OR_ROLE_ADMIN)
	public AutomatedTest bindAutomatedTest(Long testCaseId, String testPath) {

		if (StringUtils.isBlank(testPath)){
			removeAutomation(testCaseId);
			return null;
		}
		else{

			Couple<Long, String> projectAndTestname = extractAutomatedProjectAndTestName(testCaseId, testPath);

			// once it's okay we commit the test association
			return bindAutomatedTest(testCaseId, projectAndTestname.getA1(), projectAndTestname.getA2());
		}

	}


	@Override
	public void removeAutomation(long testCaseId) {
		TestCase testCase = testCaseDao.findById(testCaseId);
		AutomatedTest previousTest = testCase.getAutomatedTest();
		testCase.removeAutomatedScript();
		taService.removeIfUnused(previousTest);
	}

	/**
	 * initialCustomFieldValues maps the id of a CustomField to the value of the corresponding CustomFieldValues for
	 * that BoundEntity. read it again until it makes sense. it assumes that the CustomFieldValues instances already
	 * exists.
	 * 
	 * @param entity
	 * @param initialCustomFieldValues
	 */
	protected void initCustomFieldValues(BoundEntity entity, Map<Long, String> initialCustomFieldValues) {

		List<CustomFieldValue> persistentValues = customFieldValuesService.findAllCustomFieldValues(entity);

		for (CustomFieldValue value : persistentValues) {
			Long customFieldId = value.getCustomField().getId();

			if (initialCustomFieldValues.containsKey(customFieldId)) {
				String newValue = initialCustomFieldValues.get(customFieldId);
				value.setValue(newValue);
			}

		}
	}

	/**
	 * @see org.squashtest.tm.service.testcase.CustomTestCaseFinder#findAllByAncestorIds(java.util.List)
	 */
	@Override
	@PostFilter("hasPermission(filterObject , 'READ') or hasRole('ROLE_ADMIN')")
	public List<TestCase> findAllByAncestorIds(Collection<Long> folderIds) {
		List<TestCaseLibraryNode> nodes = testCaseLibraryNodeDao.findAllByIds(folderIds);
		return new TestCaseNodeWalker().walk(nodes);
	}

	/**
	 * @see org.squashtest.tm.service.testcase.CustomTestCaseFinder#findAllCallingTestCases(long)
	 */
	@Override
	@PostFilter("hasPermission(filterObject , 'READ') or hasRole('ROLE_ADMIN')")
	public List<TestCase> findAllCallingTestCases(long calleeId) {
		return testCaseDao.findAllCallingTestCases(calleeId);
	}

	@Override
	public TestCase findTestCaseFromStep(long testStepId) {
		return testCaseDao.findTestCaseByTestStepId(testStepId);
	}


	/**
	 * @see org.squashtest.tm.service.testcase.CustomTestCaseFinder#findImpTCWithImpAuto(Collection)
	 */
	@Override
	public Map<Long, TestCaseImportance> findImpTCWithImpAuto(Collection<Long> testCaseIds) {
		return testCaseDao.findAllTestCaseImportanceWithImportanceAuto(testCaseIds);
	}

	/**
	 * @see org.squashtest.tm.service.testcase.CustomTestCaseFinder#findCallingTCids(long, Collection)
	 */
	@Override
	public Set<Long> findCallingTCids(long updatedId, Collection<Long> callingCandidates) {
		List<Long> callingCandidatesClone = new ArrayList<Long>(callingCandidates);
		List<Long> callingLayer = testCaseDao.findAllTestCasesIdsCallingTestCases(Arrays.asList(Long
				.valueOf(updatedId)));
		Set<Long> callingTCToUpdate = new HashSet<Long>();
		while (!callingLayer.isEmpty() && !callingCandidatesClone.isEmpty()) {
			// filter found calling test cases
			callingLayer.retainAll(callingCandidatesClone);
			// save
			callingTCToUpdate.addAll(callingLayer);
			// reduce test case of interest
			callingCandidatesClone.removeAll(callingLayer);
			//go next layer
			callingLayer = testCaseDao.findAllTestCasesIdsCallingTestCases(callingLayer);
		}
		return callingTCToUpdate;
	}



	// ******************** private stuffs *********************



	// returns a tuple-2 with first element : project ID, second element : test name
	private Couple<Long, String> extractAutomatedProjectAndTestName(Long testCaseId, String testPath){

		// first we reject the operation if the script name is malformed
		if (! PathUtils.isPathWellFormed(testPath)){
			throw new MalformedScriptPathException();
		}

		// now it's clear to go, let's find which TA project it is. The first slash must be removed because it doesn't count.
		String path = testPath.replaceFirst("^/", "");
		int idxSlash = path.indexOf('/');

		String projectLabel = path.substring(0,idxSlash);
		String testName = path.substring(idxSlash+1);

		TestCase tc = testCaseDao.findById(testCaseId);
		GenericProject tmproject = tc.getProject();

		TestAutomationProject tap = (TestAutomationProject) CollectionUtils.find(tmproject.getTestAutomationProjects(), new HasSuchLabel(projectLabel));

		// if the project couldn't be found we must also reject the operation
		if (tap == null){
			throw new UnallowedTestAssociationException();
		}

		return new Couple<Long, String>(tap.getId(), testName);
	}

	private static final class HasSuchLabel implements Predicate{
		private String label;

		HasSuchLabel(String label) {
			this.label = label;
		}

		@Override
		public boolean evaluate(Object object) {
			TestAutomationProject tap = (TestAutomationProject) object;
			return (tap.getLabel().equals(label));
		}
	}


}
