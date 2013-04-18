/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.testcase;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;
import org.squashtest.tm.service.testcase.TestCaseModificationService;
import org.squashtest.tm.web.internal.model.testautomation.TATestNode;
import org.squashtest.tm.web.internal.model.testautomation.TATestNodeListBuilder;


@Controller
@RequestMapping("/test-cases/{testCaseId}/test-automation")
public class TestCaseAutomationController {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseAutomationController.class);
	private TestCaseModificationService testCaseModificationService;
	@ServiceReference
	public void setTestCaseModificationService(TestCaseModificationService testCaseModificationService) {
		this.testCaseModificationService = testCaseModificationService;
	}
	private static final String NAME_KEY = "name";
	private static final String TEST_CASE_ID = "testCaseId";
	private static final String PROJECT_ID = "projectId";
	

		@RequestMapping(value="/tests", method = RequestMethod.GET)
		@ResponseBody
		public Collection<TATestNode> findAssignableAutomatedTests(@PathVariable(TEST_CASE_ID) Long testCaseId){
			LOGGER.trace("Find assignable automated tests for TC#"+testCaseId);
			Collection<TestAutomationProjectContent> projectContents = testCaseModificationService.findAssignableAutomationTests(testCaseId);
			return new TATestNodeListBuilder().build(projectContents);
		}
		
		
		@RequestMapping(value="/tests", method = RequestMethod.POST, params = { PROJECT_ID, NAME_KEY})
		@ResponseBody
		public void bindAutomatedTest(@PathVariable(TEST_CASE_ID) long testCaseId,@RequestParam(PROJECT_ID) long projectId, @RequestParam(NAME_KEY) String testName){
			LOGGER.trace("Bind automated test "+testName+" to TC#"+testCaseId);			
			testCaseModificationService.bindAutomatedTest(testCaseId, projectId, testName);

		}
		
		@RequestMapping(method = RequestMethod.DELETE)
		@ResponseBody
		public void removeAutomation(@PathVariable(TEST_CASE_ID) long testCaseId){
			
			testCaseModificationService.removeAutomation(testCaseId);

		}
		
	

}
