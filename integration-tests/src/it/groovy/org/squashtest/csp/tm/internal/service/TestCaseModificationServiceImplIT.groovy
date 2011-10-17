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

package org.squashtest.csp.tm.internal.service






import javax.inject.Inject;

import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.service.TestCaseLibrariesCrudService;
import org.squashtest.csp.tm.service.TestCaseLibraryNavigationService;
import org.squashtest.csp.tm.service.TestCaseModificationService;


class TestCaseModificationServiceImplIT extends HibernateServiceSpecification {

	@Inject
	private TestCaseModificationService service

	@Inject
	private TestCaseLibraryNavigationService navService

	@Inject
	private TestCaseLibrariesCrudService libcrud




	private int testCaseId=-1;
	private int folderId = -1;

	def setup(){


		libcrud.addLibrary();

		def libList= libcrud.findAllLibraries()


		def lib = libList.get(libList.size()-1);

		def folder =  new TestCaseFolder(name:"folder")
		def testCase = new TestCase(name: "test case 1", description: "the first test case")

		navService.addFolderToLibrary(lib.id,folder)
		navService.addTestCaseToFolder (folder.id, testCase )

		folderId = folder.id;
		testCaseId= testCase.id;
	}




	def "should add a test step to test case"(){
		given :
		ActionTestStep step = new ActionTestStep(action: "action", expectedResult: "result")

		when :
		def teststep = service.addActionTestStep(testCaseId, step);

		then :
		teststep != null
		teststep.id != null
		teststep.action == step.action
		teststep.expectedResult == step.expectedResult
	}


	def "should get a test step list from a test case"(){
		given :

		ActionTestStep step1 = new ActionTestStep(action: "first step", expectedResult: "should work")
		and :
		ActionTestStep step2 = new ActionTestStep(action: "second step", expectedResult: "should work too")

		when :

		service.addActionTestStep(testCaseId, step1);
		service.addActionTestStep(testCaseId, step2);

		def list = service.findStepsByTestCaseId(testCaseId);



		then :

		list != null
		list.size() == 2
		list[0].action == step1.action
		list[1].action == step2.action
	}


	def "should rename a lone test case"(){
		given :
		def newName = "new name"
		when :
		service.updateTestCaseName(testCaseId, newName);
		def testcase = service.findTestCaseById(testCaseId)
		then :
		testcase!=null
		testcase.id == testCaseId
		testcase.name == "new name"
		testcase.description == "the first test case"
	}

	def "should rename a test case if another library node have a different name"(){
		given :
		def tc2name = "test case 2"
		def tc2desc = "should rename"
		def newName = "new name"

		def newtc= new TestCase(name: tc2name, description: tc2desc)

		when :
		navService.addTestCaseToFolder(folderId,newtc)
		service.updateTestCaseName(newtc.id, newName)
		def renewtc = service.findTestCaseById(newtc.id)
		then :
		renewtc!=null
		renewtc.id == newtc.id
		renewtc.name == "new name"
		renewtc.description == "should rename"
	}

	def "should not rename a test case if another library node have the same name"(){
		given :
		def tc2name = "test case 2"
		def tc2desc = "should fail"
		def newName = "test case 1"

		def newtc = new TestCase(name: tc2name, description: tc2desc)

		when :
		navService.addTestCaseToFolder(folderId, newtc  )
		service.updateTestCaseName(newtc.id, newName)
		then :
		thrown(DuplicateNameException)
	}



	def "should change a test case description"(){
		given :
		def tcNewDesc = "the new desc"
		when :
		service.changeDescription(testCaseId, tcNewDesc)
		def tc = service.findTestCaseById(testCaseId)

		then :
		tc.description == tcNewDesc
	}



	def "should change test case execution mode"(){
		given :
		def newExecMode = TestCaseExecutionMode.AUTOMATED

		when :
		service.changeExecutionMode(testCaseId, newExecMode)
		def tc = service.findTestCaseById(testCaseId)

		then :
		tc.executionMode == newExecMode
	}


	def "should update a test step action "(){
		given :
		ActionTestStep step = new ActionTestStep(action: "first step", expectedResult: "should work")

		and:
		def newaction = "begin"

		when :
		def tstep = service.addActionTestStep(testCaseId, step)
		service.updateTestStepAction(tstep.id, newaction)

		def listSteps = service.findStepsByTestCaseId (testCaseId)

		tstep = listSteps.get(0);


		then :
		tstep.action == newaction
	}


	def "should update a test step expected result"(){
		given :
		ActionTestStep step = new ActionTestStep(action: "first step", expectedResult: "should work")

		def newres = "confirm"

		when :
		def tstep = service.addActionTestStep(testCaseId, step)
		service.updateTestStepExpectedResult(tstep.id, newres)


		def listSteps = service.findStepsByTestCaseId (testCaseId)
		tstep = listSteps.get(0);

		then :
		tstep.expectedResult == newres
	}

	def "should move step 2 to position #3 in a list of 3 test steps"(){
		given :
		def step1 = new ActionTestStep("first step", "first result")
		def step2 = new ActionTestStep("second step", "second result")
		def step3 = new ActionTestStep("third step", "third result")

		when :
		service.addActionTestStep(testCaseId, step1)
		def tstep2 =service.addActionTestStep(testCaseId, step2)
		def tstep3 = service.addActionTestStep(testCaseId, step3)

		service.changeTestStepPosition(testCaseId, tstep2.id, 2)

		def list = service.findStepsByTestCaseId(testCaseId)



		then :
		list[1].id == tstep3.id
		list[2].id == tstep2.id
	}

	def "should move step 3 to position #2 in a list of 3 test steps"(){
		given :
		def step1 = new ActionTestStep("first step", "first result")
		def step2 = new ActionTestStep("second step", "second result")
		def step3 = new ActionTestStep("third step", "third result")

		when :
		service.addActionTestStep(testCaseId, step1)
		def tstep2 =service.addActionTestStep(testCaseId, step2)
		def tstep3 = service.addActionTestStep(testCaseId, step3)

		service.changeTestStepPosition(testCaseId, tstep3.id, 1)

		def list = service.findStepsByTestCaseId(testCaseId)



		then :
		list[1].id == tstep3.id
		list[2].id == tstep2.id
	}
	
	def "should move a couple of steps to position #2"(){
		
		given :
			def step1 = new ActionTestStep("first step", "first result")
			def step2 = new ActionTestStep("second step", "second result")
			def step3 = new ActionTestStep("third step", "third result")
			def step4 = new ActionTestStep("fourth step", "fourth result")
			def step5 = new ActionTestStep("fifth step", "fifth result")
			def step6 = new ActionTestStep("sixth step", "sixth result")
		
		and :
			service.addActionTestStep(testCaseId, step1)
			service.addActionTestStep(testCaseId, step2)
			service.addActionTestStep(testCaseId, step3)
			service.addActionTestStep(testCaseId, step4)
			service.addActionTestStep(testCaseId, step5)
			service.addActionTestStep(testCaseId, step6)
		when :
		

			service.changeTestStepsPosition(testCaseId, 1, [step4, step5].collect{it.id})
			def reTc = service.findTestCaseWithSteps (testCaseId)
			
		then :
			reTc.getSteps().collect{it.id} == [step1, step4, step5, step2, step3, step6].collect{it.id}
			
		
	}
	
	def "should move the three first steps at last position"(){
		given :
			def step1 = new ActionTestStep("first step", "first result")
			def step2 = new ActionTestStep("second step", "second result")
			def step3 = new ActionTestStep("third step", "third result")
			def step4 = new ActionTestStep("fourth step", "fourth result")
			def step5 = new ActionTestStep("fifth step", "fifth result")
			def step6 = new ActionTestStep("sixth step", "sixth result")

		and :
			service.addActionTestStep(testCaseId, step1)
			service.addActionTestStep(testCaseId, step2)
			service.addActionTestStep(testCaseId, step3)
			service.addActionTestStep(testCaseId, step4)
			service.addActionTestStep(testCaseId, step5)
			service.addActionTestStep(testCaseId, step6)
			
		when :
			service.changeTestStepsPosition(testCaseId, 3, [step1, step2, step3].collect{it.id})
			def reTc = service.findTestCaseWithSteps (testCaseId)
		
		then :
			reTc.getSteps().collect{it.id} == [step4, step5, step6, step1, step2, step3].collect{it.id}
	}



	def "should remove step 2 in a list of three steps"(){
		given :
		def step1 = new ActionTestStep("first step", "first result")
		def step2 = new ActionTestStep("second step", "second result")
		def step3 = new ActionTestStep("third step", "third result")

		when :
		def tstep1 = service.addActionTestStep(testCaseId, step1)
		def tstep2 =service.addActionTestStep(testCaseId, step2)
		def tstep3 = service.addActionTestStep(testCaseId, step3)

		service.removeStepFromTestCase(testCaseId, tstep2.id)

		def list = service.findStepsByTestCaseId(testCaseId)

		then :
		list.size() == 2
		list[0].id == tstep1.id
		list[1].id == tstep3.id
	}



	def "should allow to create a second test case having the same name than a previously removed test case"(){

		given :
		def tc = service.findTestCaseById(testCaseId);

		def tc2 = new TestCase();
		tc2.name = tc.name;

		navService.deleteNodes([Long.valueOf(testCaseId)])

		when :
		navService.addTestCaseToFolder(folderId, tc2);

		then :
		notThrown(DuplicateNameException)
	}




	def "should initialize a test case with his test steps"(){

		given :
		def tc = new TestCase(name:"rich-tc")
		def ts1 = new ActionTestStep(action:"action1", expectedResult:"ex1")
		def ts2 = new ActionTestStep(action:"action2", expectedResult:"ex2")



		navService.addTestCaseToFolder(folderId, tc)
		service.addActionTestStep tc.id, ts1
		service.addActionTestStep tc.id, ts2


		when :

		def obj = service.findTestCaseWithSteps (tc.id)




		then :
		obj.steps.size()==2
		obj.steps.collect {it.action } == ["action1", "action2"]
	}


	def "should remove a list of steps"(){
		given :
		def tc = new TestCase(name:"stepdeletion-tc")
		def ts1 = new ActionTestStep(action:"action1", expectedResult:"ex1")
		def ts2 = new ActionTestStep(action:"action2", expectedResult:"ex2")
		def ts3 = new ActionTestStep(action:"action3", expectedResult:"ex3")
		def ts4 = new ActionTestStep(action:"action4", expectedResult:"ex4")
		def ts5 = new ActionTestStep(action:"action5", expectedResult:"ex5")


		navService.addTestCaseToFolder(folderId, tc)
		service.addActionTestStep tc.id, ts1
		service.addActionTestStep tc.id, ts2
		service.addActionTestStep tc.id, ts3
		service.addActionTestStep tc.id, ts4
		service.addActionTestStep tc.id, ts5


		when :

		def toRemove = [ts1.id, ts3.id, ts5.id]

		service.removeListOfSteps(tc.id, toRemove);

		def obj = service.findStepsByTestCaseId(tc.id)


		then :
		obj.size()==2
		obj.collect {it.action } == ["action2", "action4"]
	}
}
