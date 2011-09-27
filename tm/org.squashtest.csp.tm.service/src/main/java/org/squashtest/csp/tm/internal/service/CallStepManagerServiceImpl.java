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
package org.squashtest.csp.tm.internal.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.CyclicStepCallException;
import org.squashtest.csp.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.csp.tm.domain.testcase.CallTestStep;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.internal.infrastructure.strategy.LibrarySelectionStrategy;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.csp.tm.internal.repository.TestCaseLibraryDao;
import org.squashtest.csp.tm.internal.repository.TestStepDao;
import org.squashtest.csp.tm.service.CallStepManagerService;
import org.squashtest.csp.tm.service.ProjectFilterModificationService;

@Service("squashtest.tm.service.CallStepManagerService")
@Transactional
public class CallStepManagerServiceImpl implements CallStepManagerService{
	private static final Logger LOGGER = LoggerFactory.getLogger(CallStepManagerServiceImpl.class);
	
	@Inject
	private TestCaseDao testCaseDao;
	
	@Inject 
	private TestStepDao testStepDao;
	

	@Inject
	private TestCaseLibraryDao testCaseLibraryDao;
	
	@Inject
	private ProjectFilterModificationService projectFilterModificationService;


	@Inject
	@Qualifier("squashtest.tm.service.TestCaseLibrarySelectionStrategy")
	private LibrarySelectionStrategy<TestCaseLibrary, TestCaseLibraryNode> libraryStrategy;	

	@Override
	@PreAuthorize("(hasPermission(#parentTestCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'WRITE') " +
				   "and hasPermission(#calledTestCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'READ')) " +
				   "or hasRole('ROLE_ADMIN')")
	public void addCallTestStep(long parentTestCaseId, long calledTestCaseId){
		
		if (parentTestCaseId == calledTestCaseId) throw new CyclicStepCallException();
		
		Set<Long> callTree = getTestCaseCallTree(calledTestCaseId);
		if (callTree.contains(parentTestCaseId)) throw new CyclicStepCallException();
		
		TestCase parentTestCase = testCaseDao.findById(parentTestCaseId);
		TestCase calledTestCase = testCaseDao.findById(calledTestCaseId);
		
		CallTestStep newStep = new CallTestStep();
		newStep.setCalledTestCase(calledTestCase);
		
		testStepDao.persist(newStep);
		
		parentTestCase.addStep(newStep);
		
	}

	
	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'READ')" +
	" or hasRole('ROLE_ADMIN')	")
	public TestCase findTestCase(long testCaseId) {
		return testCaseDao.findById(testCaseId);
	}
	

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public List<TestCaseLibrary> findLinkableTestCaseLibraries() {
		ProjectFilter pf = projectFilterModificationService.findProjectFilterByUserLogin();
		return pf.getActivated() ? libraryStrategy.getSpecificLibraries(pf.getProjects()) : testCaseLibraryDao
				.findAll();

	}
	

	@Override
	@PreAuthorize("hasPermission(#rootTcId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'READ')" +
					" or hasRole('ROLE_ADMIN')	")
	public  Set<Long> getTestCaseCallTree(Long rootTcId){
		
		Set<Long> calleesIds = new HashSet<Long>();
		List<Long> prevCalleesIds = testCaseDao.findAllTestCasesIdsCalledByTestCase(rootTcId);
		
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("TestCase directly calls " + prevCalleesIds);
		}
		
		while (!prevCalleesIds.isEmpty()) {
			calleesIds.addAll(prevCalleesIds);
			prevCalleesIds = testCaseDao.findAllTestCasesIdsCalledByTestCases(prevCalleesIds);
			
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("TestCase indirectly calls " + prevCalleesIds);
			}
		}
		
		return calleesIds;
	
	}
	
	
	@Override
	public List<TestCase> findCallingTestCases(long testCaseId, CollectionSorting sorting){
		return testCaseDao.findAllCallingTestCases(testCaseId, sorting);
	}
	
	
}
