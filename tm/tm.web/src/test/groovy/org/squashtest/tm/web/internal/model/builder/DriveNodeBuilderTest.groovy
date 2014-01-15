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
package org.squashtest.tm.web.internal.model.builder;

import static org.junit.Assert.*

import javax.inject.Provider;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode.State;

import spock.lang.Specification
import spock.lang.Unroll;

class DriveNodeBuilderTest extends Specification {
	PermissionEvaluationService permissionEvaluationService = Mock()
	Provider nodeBuilderPovider = Mock()
	DriveNodeBuilder builder = new DriveNodeBuilder(permissionEvaluationService, nodeBuilderPovider)

	def setup() {
		nodeBuilderPovider.get() >> new TestCaseLibraryTreeNodeBuilder(permissionEvaluationService)
	}
	
	def "should build root node of test case library"() {
		given:
		def library = theTestCaseLibrary(10L).ofProject("foo")

		when:
		JsTreeNode res = builder.setModel(library).build();

		then:
		res.attr['rel'] == "drive"
		res.attr['resId'] == "10"
		res.attr['resType'] == 'test-case-libraries'
		res.title == "foo"
		res.state == JsTreeNode.State.leaf.name()
		res.attr['editable'] == 'false'
	}

	def theTestCaseLibrary(long id) {
		TestCaseLibrary library = new TestCaseLibrary()

		use(ReflectionCategory) {
			TestCaseLibrary.set(field: "id", of:library, to: id)
		}

		return [ofProject: { ofProject library, it}]
	}

	def ofProject(TestCaseLibrary library, String name) {
		Project project = Mock(Project)
		project.getName() >> name
		project.getId() >> 10l
		library.project = project
		return library
	}

	def "should build editable node"() {
		given:
		permissionEvaluationService.hasRoleOrPermissionOnObject (_, _, _) >> true

		and:
		def library = theTestCaseLibrary(10L).ofProject("foo")

		when:
		JsTreeNode res = builder.setModel(library).build();

		then:
		res.attr['editable'] == 'true'
	}

	def "node should reference authorized wizards"() {
		given:
		def library = theTestCaseLibrary(10L).ofProject("foo")
		library.enablePlugin("foo");
		library.enablePlugin("bar");
		
		when:
		JsTreeNode res = builder.setModel(library).build();

		then:
		res.attr["wizards"].collect { it } ==  ["foo", "bar"]
	}
	
	def "should build an expanded node"() {
		given:
		Library library = theTestCaseLibrary(10L).ofProject("foo")
		TestCase tc = Mock()
		def visitor
		tc.accept({ visitor = it }) >> { visitor.visit(tc) }
		tc.getStatus() >> TestCaseStatus.WORK_IN_PROGRESS
		tc.getImportance() >> TestCaseImportance.LOW
		tc.getRequirementVersionCoverages() >> []
		library.addContent tc
		
		and:
		MultiMap expanded = new MultiValueMap()
		expanded.put("TestCaseLibrary", 10L);
		
		when:
		JsTreeNode res = builder.expand(expanded).setModel(library).build();

		then: 
		res.state == State.open.name()
		res.children.size() == 1

	}
	
	@Unroll
	def "should candidate [#expandedType, #expandedId] be expanded : #expected"() {
		given:
		Library library = theTestCaseLibrary(10L).ofProject("foo")
		
		and:
		MultiMap expanded = new MultiValueMap()
		expanded.put(expandedType, expandedId);
		
		expect:
		builder.expand(expanded).setModel(library).shouldExpandModel() == expected
		
		where: 
		expandedType      | expandedId | expected
		"TestCaseLibrary" | 10L        | true
		"TestCaseLibrary" | 20L        | false
		"Whatever"        | 10L        | false
		
	}
}
