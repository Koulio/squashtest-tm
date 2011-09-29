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

package org.squashtest.csp.tm.domain.testcase

import org.squashtest.csp.tm.domain.UnknownEntityException;
import org.squashtest.csp.tm.domain.attachment.Attachment;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory;

import java.util.Set;

import spock.lang.Specification;

class TestCaseTest extends Specification {
	TestCase testCase = new TestCase();

	def "should add a step at the end of the list"() {
		given:
		testCase.steps << new ActionTestStep(action: "1")
		testCase.steps << new ActionTestStep(action: "2")

		and:
		def newStep = new ActionTestStep(action: "3")

		when:
		testCase.addStep(newStep)

		then:
		testCase.steps[2] == newStep
	}

	def "should not add a null step"() {
		when:
		testCase.addStep(null)

		then:
		thrown(IllegalArgumentException)
	}

	def "should move step from given index to a greater index"() {
		given:
		def step0 = new ActionTestStep(action:"0")
		def step1 = new ActionTestStep(action:"1")
		def step2 = new ActionTestStep(action:"2")
		def step3 = new ActionTestStep(action:"3")

		testCase.steps << step0
		testCase.steps << step1
		testCase.steps << step2
		testCase.steps << step3

		when:
		testCase.moveStep(1, 3)

		then:
		testCase.steps == [step0, step2, step3, step1]
	}

	def "should move step from given index to a lesser index"() {
		given:
		def step0 = new ActionTestStep(action:"0")
		def step1 = new ActionTestStep(action:"1")
		def step2 = new ActionTestStep(action:"2")
		def step3 = new ActionTestStep(action:"3")

		testCase.steps << step0
		testCase.steps << step1
		testCase.steps << step2
		testCase.steps << step3

		when:
		testCase.moveStep(2, 0)

		then:
		testCase.steps == [step2, step0, step1, step3]
	}

	def "should move a list of steps to a lesser index"(){
		
		given :
			def step0 = new ActionTestStep(action:"0")
			def step1 = new ActionTestStep(action:"1")
			def step2 = new ActionTestStep(action:"2")
			def step3 = new ActionTestStep(action:"3")
	
			testCase.steps << step0
			testCase.steps << step1
			testCase.steps << step2
			testCase.steps << step3
		

			def tomove = [step2, step3]	
			def position = 1
			def result = [step0, step2, step3, step1]
			
			
		when :
		
			testCase.moveSteps(position, tomove);
		
		then :
			testCase.steps == result
		
	}
	
	
	
	def "should move a list of steps to a greater index"(){
		
		given :
			def step0 = new ActionTestStep(action:"0")
			def step1 = new ActionTestStep(action:"1")
			def step2 = new ActionTestStep(action:"2")
			def step3 = new ActionTestStep(action:"3")
	
			testCase.steps << step0
			testCase.steps << step1
			testCase.steps << step2
			testCase.steps << step3


			def tomove = [step0, step1]
			def position = 3;
			def result = [step2, step3, step0, step1]
			
		when :
			testCase.moveSteps(position, tomove)
		
		then :
			testCase.steps == result
		
		
	}
	
	def "should add a verified requirement"() {
		given:
		Requirement r = new Requirement()

		when:
		testCase.addVerifiedRequirement(r)

		then:
		testCase.verifiedRequirements.contains r
	}

	def "should remove a verified requirement"() {
		given:
		Requirement r = new Requirement()
		testCase.verifiedRequirements.add r

		when:
		testCase.removeVerifiedRequirement(r)

		then:
		!testCase.verifiedRequirements.contains(r)
	}

	def "should return position of step"() {
		given:
		TestStep step10 = Mock()
		step10.id >> 10
		testCase.steps << step10

		TestStep step20 = Mock()
		step20.id >> 20
		testCase.steps << step20


		when:
		def pos = testCase.getPositionOfStep(20)

		then:
		pos == 1
	}

	def "should throw exception when position of unknown step is asked"() {
		given:
		TestStep step10 = Mock()
		step10.id >> 10
		testCase.steps << step10

		when:
		def pos = testCase.getPositionOfStep(20)

		then:
		thrown(UnknownEntityException)
	}
}
