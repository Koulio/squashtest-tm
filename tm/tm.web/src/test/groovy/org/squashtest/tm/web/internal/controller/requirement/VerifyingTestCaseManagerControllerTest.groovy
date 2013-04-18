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
package org.squashtest.tm.web.internal.controller.requirement

import static org.junit.Assert.*

import javax.inject.Provider

import org.springframework.context.MessageSource
import org.springframework.ui.ExtendedModelMap
import org.springframework.ui.Model
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.exception.requirement.RequirementAlreadyVerifiedException;
import org.squashtest.tm.service.requirement.RequirementVersionManagerService
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder

import spock.lang.Specification

class VerifyingTestCaseManagerControllerTest extends Specification {
	VerifyingTestCaseManagerController controller = new VerifyingTestCaseManagerController()
	MessageSource messageSource = Mock()
	Provider<DriveNodeBuilder> driveNodeBuilder = driveNodeBuilderProvider()
	VerifyingTestCaseManagerService verifyingTestCaseManager = Mock()
	RequirementVersionManagerService requirementVersionManager = Mock()

	def setup() {
		controller.messageSource = messageSource
		controller.driveNodeBuilder = driveNodeBuilder
		controller.verifyingTestCaseManager = verifyingTestCaseManager
		controller.requirementVersionFinder = requirementVersionManager
	}

	def driveNodeBuilderProvider() {
		def provider = Mock(Provider)
		PermissionEvaluationService permissionEvaluationService = Mock()
		provider.get() >> new DriveNodeBuilder(permissionEvaluationService)
		return provider
	}

	def "should init model to show manager"() {
		given:
		Model model = new ExtendedModelMap()

		and:
		RequirementVersion requirementVersion = Mock()
		requirementVersionManager.findById(10L) >> requirementVersion

		and:
		verifyingTestCaseManager.findLinkableTestCaseLibraries() >> []

		when:
		controller.showManager(10L, model)

		then:
		model.asMap()['requirementVersion'] == requirementVersion
		model.asMap()['linkableLibrariesModel'] == []
	}

	def "should return manager view name"() {
		given:
		requirementVersionManager.findById(10L) >> Mock(RequirementVersion)
		verifyingTestCaseManager.findLinkableTestCaseLibraries() >> []

		when:
		def view = controller.showManager(10L, Mock(Model))

		then:
		view == "page/requirements/show-verifying-testcase-manager"
	}

	def "should return rapport of test cases which could not be added"() {
		given:
		TestCase tc = Mock()
		tc.id >> 2
		RequirementVersion req = Mock()
		RequirementAlreadyVerifiedException ex = new RequirementAlreadyVerifiedException(req, tc)
		verifyingTestCaseManager.addVerifyingTestCasesToRequirementVersion([1, 2], 10) >> [ex]

		when:
		def res = controller.addVerifyingTestCasesToRequirement([1, 2], 10)

		then:
		res.alreadyVerifiedRejections
	}
}
