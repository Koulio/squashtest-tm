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
package org.squashtest.tm.web.internal.controller.testcase;

import java.util.Map;

import javax.inject.Provider

import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder
import org.squashtest.tm.web.internal.model.builder.TestCaseLibraryTreeNodeBuilder
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode.State

import spock.lang.Specification

class TestCaseLibraryNavigationControllerTest extends Specification {
	TestCaseLibraryNavigationController controller = new TestCaseLibraryNavigationController()
	TestCaseLibraryNavigationService testCaseLibraryNavigationService = Mock()
	VerifiedRequirementsManagerService verifiedRequirementManagerService = Mock()
	Provider driveNodeBuilder = Mock();
	Provider testCaseLibraryTreeNodeBuilder = Mock();
	PermissionEvaluationService permissionEvaluationService = Mock()
	InternationalizationHelper internationalizationHelper = Mock()
	def setup() {
		controller.testCaseLibraryNavigationService = testCaseLibraryNavigationService

		controller.driveNodeBuilder = driveNodeBuilder
		controller.testCaseLibraryTreeNodeBuilder = testCaseLibraryTreeNodeBuilder
		verifiedRequirementManagerService.testCaseHasUndirectRequirementCoverage(_)>>false
		
		driveNodeBuilder.get() >> new DriveNodeBuilder(permissionEvaluationService, null)
		testCaseLibraryTreeNodeBuilder.get() >> new TestCaseLibraryTreeNodeBuilder(permissionEvaluationService, verifiedRequirementManagerService, internationalizationHelper)
		internationalizationHelper.internationalize(_,_)>> ""
		internationalizationHelper.internationalizeYesNo(false, _)>>"non";
		internationalizationHelper.internationalizeYesNo(true, _)>>"oui";
		internationalizationHelper.getMessage(_, _, _, _)>>"message";
		}

	def "should return root nodes of library"() {
		given:
		TestCaseFolder rootFolder = Mock()
		rootFolder.name >> "root folder"
		rootFolder.id >> 5

		testCaseLibraryNavigationService.findLibraryRootContent(10) >> [rootFolder]

		when:
		def res = controller.getRootContentTreeModel(10)

		then:
		res.size() == 1
		res[0].title == rootFolder.name
		res[0].attr['resId'] == "${rootFolder.id}"
	}

	def "should create a node of leaf type"() {
		given:
		TestCase node = new TestCase(name: "tc")
		use (ReflectionCategory) {
			TestCaseLibraryNode.set field: "id", of: node, to: 15L
		}

		when:
		def res = controller.createTreeNodeFromLibraryNode(node)

		then:
		res.state == State.leaf.name()
	}
	def "should create a node of closed folder type"() {
		given:
		TestCaseFolder node = Mock()
		
		
		when:
		def res = controller.createTreeNodeFromLibraryNode(node)

		then:
		res.state == State.closed.name()
	}

	def "should return content of folder"() {
		given:
		def folderId = 10

		and:
		TestCase content = Mock()
		content.name >> "content"

		testCaseLibraryNavigationService.findFolderContent(10) >> [content]

		when:
		def res = controller.getFolderContentTreeModel(folderId)

		then:
		res.size() == 1
		res[0].title == content.name
	}

	def "should create folder at root of library and return folder tree model"() {
		given:
		TestCaseFolder folder = Mock()
		folder.id >> 50

		when:
		def res = controller.addNewFolderToLibraryRootContent(10, folder)

		then:
		1 * testCaseLibraryNavigationService.addFolderToLibrary(10, folder)
		res.attr['resId'] == "50"
	}

	def "should create test case at root of library and return test case edition view"() {
		given:
		TestCaseFormModel tcfm = Mock()
		TestCase tc = Mock()
		def visitor
		tc.accept({ visitor = it }) >> { visitor.visit(tc) }
		tc.getStatus() >> TestCaseStatus.WORK_IN_PROGRESS
		tc.getImportance() >> TestCaseImportance.LOW
		tc.getRequirementVersionCoverages() >> []
		tc.getId()>>23L
		tc.getName()>>"test case"
		tcfm.getTestCase() >> tc 		
		Map<Long, String> customFieldValues = [:]
		tcfm.getCustomFields()>>customFieldValues
		when:
		def res = controller.addNewTestCaseToLibraryRootContent(10, tcfm)

		then:
		1 * testCaseLibraryNavigationService.addTestCaseToLibrary(10, {it.getName() == "test case"}, [:])
		res.attr['name'] == "test case"
	}

	def "should create test case in folder and return test case model"() {
		given:
		TestCaseFormModel tcfm = Mock()
		TestCase tc = Mock()
		def visitor
		tc.accept({ visitor = it }) >> { visitor.visit(tc) }
		tc.getStatus() >> TestCaseStatus.WORK_IN_PROGRESS
		tc.getImportance() >> TestCaseImportance.LOW
		tc.getRequirementVersionCoverages() >> []
		tc.getId()>>23L
		tc.getName()>>"test case"
		tcfm.getTestCase() >> tc 
		Map<Long, String> customFieldValues = [:]
		tcfm.getCustomFields()>>customFieldValues
		when:
		def res = controller.addNewTestCaseToFolder(10, tcfm)

		then:
		1 * testCaseLibraryNavigationService.addTestCaseToFolder(10, {it.getName() == "test case"}, [:])
		res.attr['name'] == "test case"
	}
}
