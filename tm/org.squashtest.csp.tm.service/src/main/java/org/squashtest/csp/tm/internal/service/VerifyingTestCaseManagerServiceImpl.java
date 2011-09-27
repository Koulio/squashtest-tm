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

import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.internal.infrastructure.strategy.LibrarySelectionStrategy;
import org.squashtest.csp.tm.internal.repository.RequirementDao;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.csp.tm.internal.repository.TestCaseLibraryDao;
import org.squashtest.csp.tm.service.ProjectFilterModificationService;
import org.squashtest.csp.tm.service.VerifyingTestCaseManagerService;

@Service("squashtest.tm.service.VerifyingTestCaseManagerService")
@Transactional
public class VerifyingTestCaseManagerServiceImpl implements VerifyingTestCaseManagerService {

	@Inject
	private TestCaseDao testCaseDao;
	@Inject
	private TestCaseLibraryDao testCaseLibraryDao;
	@Inject
	private RequirementDao requirementDao;
	@Inject
	private ProjectFilterModificationService projectFilterModificationService;
	@Inject
	@Qualifier("squashtest.tm.service.TestCaseLibrarySelectionStrategy")
	private LibrarySelectionStrategy<TestCaseLibrary, TestCaseLibraryNode> libraryStrategy;

	@Override
	public Requirement findRequirement(long requirementId) {
		return requirementDao.findById(requirementId);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public List<TestCaseLibrary> findLinkableTestCaseLibraries() {
		ProjectFilter pf = projectFilterModificationService.findProjectFilterByUserLogin();
		return pf.getActivated() ? libraryStrategy.getSpecificLibraries(pf.getProjects()) : testCaseLibraryDao
				.findAll();
	}

	@Override
	@PostFilter("hasPermission(#requirementId, 'org.squashtest.csp.tm.domain.requirement.Requirement', 'WRITE') or hasRole('ROLE_ADMIN')")	
	public void addVerifyingTestCasesToRequirement(List<Long> testCasesIds, long requirementId) {

		List<TestCase> tcs = testCaseDao.findAllByIdList(testCasesIds);

		if (!tcs.isEmpty()) {
			Requirement requirement = requirementDao.findById(requirementId);

			for (TestCase testcase : tcs) {
				requirement.addVerifyingTestCase(testcase);
			}
		}
	}

	@Override
	@PostFilter("hasPermission(#requirementId, 'org.squashtest.csp.tm.domain.requirement.Requirement', 'WRITE') or hasRole('ROLE_ADMIN')")	
	public void removeVerifyingTestCasesFromRequirement(List<Long> testCasesIds, long requirementId) {

		List<TestCase> tcs = testCaseDao.findAllByIdList(testCasesIds);

		if (!tcs.isEmpty()) {
			Requirement requirement = requirementDao.findById(requirementId);

			for (TestCase testcase : tcs) {
				requirement.removeVerifyingTestCase(testcase);
			}
		}

	}

	@Override
	@PostFilter("hasPermission(#requirementId, 'org.squashtest.csp.tm.domain.requirement.Requirement', 'WRITE') or hasRole('ROLE_ADMIN')")	
	public void removeVerifyingTestCaseFromRequirement(long requirementId, long testCaseId) {

		Requirement req = requirementDao.findById(requirementId);
		TestCase testCase = testCaseDao.findById(testCaseId);

		req.removeVerifyingTestCase(testCase);

	}

}
