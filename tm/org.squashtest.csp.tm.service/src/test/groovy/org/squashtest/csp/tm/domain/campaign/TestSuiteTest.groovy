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
package org.squashtest.csp.tm.domain.campaign

import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.testcase.TestCase;

import spock.lang.Specification;

class TestSuiteTest extends Specification {

	def "should rename normally"(){
		given :		
			def iteration = Mock(Iteration);
			iteration.checkSuiteNameAvailable(_) >> true
			
		and :
			def suite = new TestSuite(name:"bob");
			suite.iteration = iteration
			
		when :
			suite.rename("robert")
			
		then :
			suite.name == "robert"
	
	}
	
	def "should rant because cannot rename"(){
		
		given :
			def iteration = Mock(Iteration);
			iteration.checkSuiteNameAvailable(_) >> false
			
		and :
			def suite = new TestSuite(name : "bob")
			suite.iteration = iteration
			
		when :
			suite.rename("robert")
			
		then :
			thrown DuplicateNameException
			suite.name == "bob"
	}
	
	
	def "should associate with a bunch of items test plan"(){
		given :		
			def items = []
			3.times{items << Mock(IterationTestPlanItem)}			
		and :
			def suite = new TestSuite()
			
		when :
			suite.bindTestPlan(items)
			
		then :
			1 * items[0].setTestSuite(suite)
			1 * items[1].setTestSuite(suite)
			1 * items[2].setTestSuite(suite)
	}
	
	def "should associate with a bunch of item test plan"(){
		
		given :
			def items = []
			3.times{items <<  mockITP(it)}
			
		and :
			def iteration = Mock(Iteration)
			iteration.getTestPlans() >> items
			
		and :
			def suite = new TestSuite()
			suite.iteration=iteration
			
		when :
			suite.bindTestPlanById([0l, 1l, 2l])
			
		then :
			1 * items[0].setTestSuite(suite)
			1 * items[1].setTestSuite(suite)
			1 * items[2].setTestSuite(suite)
			
	}
	
	def "should reorder item test plans (1)"(){
		
		given :
			
			def iteration = new Iteration()
			def suite = new TestSuite()
			iteration.addTestSuite(suite)
			suite.iteration=iteration
			
			def items = []
			10.times{
					def item = new IterationTestPlanItem(referencedTestCase:Mock(TestCase)); 
					iteration.addTestPlan(item) 
					items << item
				}
			
		and :
			suite.bindTestPlan(items[2, 4, 6, 7, 8, 9])
		
		and :
	
			def toMove = items[4,6,7]
			
		when :
		
			suite.reorderTestPlan(0, toMove) 
		
		then :
			suite.getTestPlan() == items[4, 6, 7, 2, 8, 9]
			iteration.getTestPlans() == items[0, 1, 4, 6, 7, 2, 3, 5, 8, 9]
	}
	
	def "should reorder item test plans (2)"(){
		
		given :
			
			def iteration = new Iteration()
			def suite = new TestSuite()
			iteration.addTestSuite(suite)
			suite.iteration=iteration
			
			def items = []
			10.times{
					def item = new IterationTestPlanItem(referencedTestCase:Mock(TestCase));
					iteration.addTestPlan(item)
					items << item
				}
			
		and :
			suite.bindTestPlan(items[2, 4, 6, 7, 8, 9])
		
		and :
	
			def toMove = items[4,6,7]
			
		when :
		
			suite.reorderTestPlan(2, toMove)
		
		then :
			suite.getTestPlan() == items[2, 8, 4, 6, 7, 9 ]
			iteration.getTestPlans() == items[0, 1, 2, 3, 5, 8, 4, 6, 7, 9 ]
	}
	
	def mockITP = {
		def m = Mock(IterationTestPlanItem)
		m.getId() >> it 
		return m
	}
	
}
