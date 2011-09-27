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
package org.squashtest.csp.tm.internal.infrastructure.strategy;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;

@Component("squashtest.tm.service.TestCaseLibrarySelectionStrategy")
public class TestCaseLibrarySelectionStrategyImpl implements LibrarySelectionStrategy<TestCaseLibrary, TestCaseLibraryNode> {

	@Override
	public List<TestCaseLibrary> getSpecificLibraries(List<Project> givenProjectList) {
		List<TestCaseLibrary> toReturn = new ArrayList<TestCaseLibrary>();
		
		for (Project project : givenProjectList) {
			toReturn.add(project.getTestCaseLibrary());
		}
		
		return toReturn;
	}

}
