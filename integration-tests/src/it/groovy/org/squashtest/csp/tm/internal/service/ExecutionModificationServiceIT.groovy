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

import org.spockframework.util.NotThreadSafe;
import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.domain.execution.ExecutionStatusReport;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.service.CampaignLibrariesCrudService;
import org.squashtest.csp.tm.service.CampaignLibraryNavigationService;
import org.squashtest.csp.tm.service.CampaignModificationService;
import org.squashtest.csp.tm.service.ExecutionModificationService;
import org.squashtest.csp.tm.service.ExecutionProcessingService;
import org.squashtest.csp.tm.service.IterationModificationService;
import org.squashtest.csp.tm.service.IterationTestPlanManagerService 
import org.squashtest.csp.tm.service.TestCaseLibrariesCrudService;
import org.squashtest.csp.tm.service.TestCaseLibraryNavigationService;
import org.squashtest.csp.tm.service.TestCaseModificationService;


@NotThreadSafe
class ExecutionModificationServiceIT extends HibernateServiceSpecification {

	@Inject
	private CampaignModificationService campaignModService

	@Inject
	private CampaignLibraryNavigationService campaignNavService

	@Inject
	private CampaignLibrariesCrudService campaignLibCrud



	@Inject
	private IterationModificationService iterService
	
	@Inject 
	private IterationTestPlanManagerService tpManagerService

	@Inject
	private ExecutionModificationService execService;

	@Inject
	private ExecutionProcessingService procservice;



	@Inject
	private TestCaseModificationService tcModservice

	@Inject
	private TestCaseLibraryNavigationService tcNavService

	@Inject
	private TestCaseLibrariesCrudService tcLibCrud



	private long iterationId
	private long testCaseId
	private long libtcId;
	private long testPlanId;


	def setup(){

		/** make the iteration environnement **/	
		campaignLibCrud.addLibrary();
		def libList= campaignLibCrud.findAllLibraries()
		def camplib = libList.get(libList.size()-1);

		Campaign campaign = new Campaign(name:"execcampaign")
		campaignNavService.addCampaignToCampaignLibrary(camplib.id, campaign)

		Iteration iteration = new Iteration(name:"exec iteration");
		campaignNavService.addIterationToCampaign(iteration,campaign.id)

		iterationId=iteration.id

		/** make the test case environment **/

		tcLibCrud.addLibrary()
		libList = tcLibCrud.findAllLibraries();
		def tcLib = libList.get(libList.size()-1)
		libtcId = tcLib.id

		TestCase testCase = new TestCase(name:"exec IT test case")

		tcNavService.addTestCaseToLibrary (tcLib.id, testCase);

		ActionTestStep ts1 = new ActionTestStep(action:"action 1")
		ActionTestStep ts2 = new ActionTestStep(action:"action 2")
		ActionTestStep ts3 = new ActionTestStep(action:"action 3")
		ActionTestStep ts4 = new ActionTestStep(action:"action 4")
		ActionTestStep ts5 = new ActionTestStep(action:"action 5")

		tcModservice.addActionTestStep(testCase.id, ts1)
		tcModservice.addActionTestStep(testCase.id, ts2)
		tcModservice.addActionTestStep(testCase.id, ts3)
		tcModservice.addActionTestStep(testCase.id, ts4)
		tcModservice.addActionTestStep(testCase.id, ts5)

		testCaseId=testCase.id
		
		tpManagerService.addTestCasesToIteration([testCaseId], iterationId);
		
		IterationTestPlanItem tp = tpManagerService.findTestPlanItemByTestCaseId(iterationId, testCaseId);
		
		testPlanId = tp.getId();
		
		
	}


	def "should create a new execution for the test case in the iteration"(){
		given :

		iterService.addExecution(iterationId, testPlanId)

		when :
		def execList = iterService.findAllExecutions(iterationId);
		Execution execution = execList.get(execList.size()-1)
		then :
		execution.getName()== "exec IT test case"
	}


	def "should create three executions and fetch them in the correct order"(){

		given :
		TestCase tc1= new TestCase(name:"exec tc1")
		TestCase tc2= new TestCase(name:"exec tc2")
		TestCase tc3= new TestCase(name:"exec tc3")

		tcNavService.addTestCaseToLibrary(libtcId, tc1)
		tcNavService.addTestCaseToLibrary(libtcId, tc2)
		tcNavService.addTestCaseToLibrary(libtcId, tc3)
		
		and :
		
		tpManagerService.addTestCasesToIteration([tc1.id, tc2.id, tc3.id], iterationId)
		
		def tp1 = tpManagerService.findTestPlanItemByTestCaseId(iterationId, tc1.id);
		def tp2 = tpManagerService.findTestPlanItemByTestCaseId(iterationId, tc2.id);
		def tp3 = tpManagerService.findTestPlanItemByTestCaseId(iterationId, tc3.id);
		

		iterService.addExecution(iterationId, tp1.id)
		iterService.addExecution(iterationId, tp2.id)
		iterService.addExecution(iterationId, tp3.id)


		when :
		List<Execution> executions = iterService.findAllExecutions(iterationId)

		then :
		executions.collect { it.name} ==[
			"exec tc1",
			"exec tc2",
			"exec tc3"
		]
	}

	def "should iterate over the 5 steps of the referenced test case"(){

		given :
		iterService.addExecution(iterationId, testPlanId)
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)

		when :
		def executionSteps = []
		executionSteps << procservice.getStepAt(execution.id,0)
		executionSteps << procservice.getStepAt(execution.id,1)
		executionSteps << procservice.getStepAt(execution.id,2)
		executionSteps << procservice.getStepAt(execution.id,3)
		executionSteps << procservice.getStepAt(execution.id,4)


		then :

		executionSteps.size()==5
		executionSteps.collect {it.action} == [
			"action 1",
			"action 2",
			"action 3",
			"action 4",
			"action 5"
		]
		executionSteps.collect {it.executionStepOrder} == [0, 1, 2, 3, 4]
	}

	def "should raise an out of bound exception"(){

		given :
		iterService.addExecution(iterationId, testPlanId);
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)
		when :
		def fails = procservice.getStepAt(execution.id, 10)

		then :
		thrown(IndexOutOfBoundsException)
	}
	/*
	 * 
	 * unimplemented for now 	
	 def "should return execution status READY"(){
	 given :
	 iterService.addExecution(iterationId, testPlanId);
	 def execList = iterService.findAllExecutions(iterationId)
	 def execution = execList.get(execList.size()-1)
	 when :
	 def executionSteps = []
	 executionSteps << procservice.nextExecutionStep(execution.id)
	 executionSteps << procservice.nextExecutionStep(execution.id)
	 executionSteps << procservice.nextExecutionStep(execution.id)
	 executionSteps << procservice.nextExecutionStep(execution.id)
	 executionSteps << procservice.nextExecutionStep(execution.id)
	 Execution execution2 = execService.findAndInitExecution(execution.id)
	 def status = execution2.getExecutionStatus();
	 then :
	 status = ExecutionStatus.READY;
	 }
	 def "should return execution status BLOQUED"(){
	 given :
	 iterService.addExecution(iterationId, testPlanId);
	 def execList = iterService.findAllExecutions(iterationId)
	 def execution = execList.get(execList.size()-1)
	 when :
	 def executionSteps = []
	 executionSteps << procservice.nextExecutionStep(execution.id)
	 executionSteps << procservice.nextExecutionStep(execution.id)
	 executionSteps << procservice.nextExecutionStep(execution.id)
	 executionSteps << procservice.nextExecutionStep(execution.id)
	 executionSteps << procservice.nextExecutionStep(execution.id)
	 procservice.setExecutionStepStatus(executionSteps.get(2).getId(), ExecutionStatus.BLOQUED)
	 Execution execution2 = execService.findAndInitExecution(execution.id)
	 def status = execution2.getExecutionStatus();
	 then :
	 status = ExecutionStatus.READY;
	 }
	 */	

	def "should update execution dscription"(){

		given :
		iterService.addExecution(iterationId, testPlanId)
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)

		when :
		execService.setExecutionDescription(execution.id, "wooohooo I just updated the description here !")

		execution=execService.findExecution(execution.id)


		then :
		execution.getDescription()=="wooohooo I just updated the description here !"
	}

	def "should update execution step comment"(){

		given :
		iterService.addExecution(iterationId, testPlanId)
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)
		when :
		ExecutionStep executionStep = procservice.getStepAt(execution.id,0)

		procservice.setExecutionStepComment(executionStep.id, "Wooooohooo I did that here too !")

		executionStep = procservice.findExecutionStep(executionStep.id);

		then :
		executionStep.getComment()=="Wooooohooo I did that here too !"
	}



	def "should get me the first and third step"(){
		given :
		iterService.addExecution(iterationId, testPlanId);
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)

		when :

		def exec1 = procservice.getStepAt(execution.id, 0)
		def exec3 = procservice.getStepAt(execution.id, 2)



		then :
		exec1.getAction()=="action 1"
		exec3.getAction()=="action 3"
	}


	def "should return the current step of the execution"(){
		given :
		iterService.addExecution(iterationId, testPlanId);
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)

		ExecutionStep estep = procservice.getStepAt(execution.id, 0);
		procservice.setExecutionStepStatus(estep.id, ExecutionStatus.SUCCESS);

		estep = procservice.getStepAt(execution.id, 1);
		procservice.setExecutionStepStatus(estep.id, ExecutionStatus.FAILURE);


		when :

		def lastOne = procservice.findRunningExecutionStep (execution.id)


		then :
		lastOne.action=="action 3"
	}

	def "should tell that the requested execution is the second one of the set"(){

		given :

		iterService.addExecution(iterationId, testPlanId);
		iterService.addExecution(iterationId, testPlanId);
		iterService.addExecution(iterationId, testPlanId);

		List<Execution> execList = iterService.findExecutionsByTestPlan(iterationId,testPlanId);


		when :
		Execution execution = execList.get(1);

		def rank = execService.findExecutionRank(execution.id);


		then :
		rank==1;
	}

	def "should bring an execution report"(){
		given :
		iterService.addExecution(iterationId, testPlanId);
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)
		when :
		def report = procservice.getExecutionStatusReport(execution.id);

		then :

		report.ready==5;
	}

	def "should set an execution status for an execution to BLOQUED"(){
		given :
		iterService.addExecution(iterationId, testPlanId);
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)

		when :
		ExecutionStatusReport report = new ExecutionStatusReport(1, 0, 4, 0, 0);

		procservice.setExecutionStatus(execution.id, report);

		def reExec = execService.findAndInitExecution(execution.id);


		then :
		reExec.executionStatus == ExecutionStatus.BLOQUED;
	}

	def "should set an execution status for an execution to FAILURE"(){
		given :
		iterService.addExecution(iterationId, testPlanId);
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)

		when :
		ExecutionStatusReport report = new ExecutionStatusReport(0,1, 4, 0, 0);

		procservice.setExecutionStatus(execution.id, report);

		def reExec = execService.findAndInitExecution(execution.id);


		then :
		reExec.executionStatus == ExecutionStatus.FAILURE;
	}

	def "should set an execution status for an execution to SUCCESS"(){
		given :
		iterService.addExecution(iterationId, testPlanId);
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)

		when :
		ExecutionStatusReport report = new ExecutionStatusReport(0, 0, 5, 0, 0);

		procservice.setExecutionStatus(execution.id, report);

		def reExec = execService.findAndInitExecution(execution.id);


		then :
		reExec.executionStatus == ExecutionStatus.SUCCESS;
	}

	def "should set an execution status for an execution to RUNNING"(){
		given :
		iterService.addExecution(iterationId, testPlanId);
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)

		when :
		ExecutionStatusReport report = new ExecutionStatusReport(0, 0, 1,3, 1);

		procservice.setExecutionStatus(execution.id, report);

		def reExec = execService.findAndInitExecution(execution.id);


		then :
		reExec.executionStatus == ExecutionStatus.RUNNING;
	}

	def "should set an execution status for an execution to READY"(){
		given :
		iterService.addExecution(iterationId, testPlanId);
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)

		when :
		ExecutionStatusReport report = new ExecutionStatusReport(0, 0, 0, 0, 5);

		procservice.setExecutionStatus(execution.id, report);

		def reExec = execService.findAndInitExecution(execution.id);


		then :
		reExec.executionStatus == ExecutionStatus.READY;
	}


	def "should update executionStep status and accordingly update the status of its parent execution to BLOQUED"(){

		given :
		iterService.addExecution(iterationId, testPlanId)
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)



		and :
		List<ExecutionStep> listSteps = execService.getExecutionSteps(execution.id);



		when :
		ExecutionStep toBlock = listSteps.get(2);
		procservice.setExecutionStepStatus(toBlock.getId(), ExecutionStatus.BLOQUED)

		Execution exec = execService.findExecution(execution.id)

		then :
		exec.getExecutionStatus() == ExecutionStatus.BLOQUED;
	}

	def "should update executionStep status and accordingly update the status of its parent execution to FAILURE"(){

		given :
		iterService.addExecution(iterationId, testPlanId)
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)



		and :
		List<ExecutionStep> listSteps = execService.getExecutionSteps(execution.id);



		when :
		ExecutionStep toBlock = listSteps.get(2);
		procservice.setExecutionStepStatus(toBlock.getId(), ExecutionStatus.FAILURE)

		Execution exec = execService.findExecution(execution.id)

		then :
		exec.getExecutionStatus() == ExecutionStatus.FAILURE;
	}

	def "should update executionStep status and accordingly update the status of its parent execution to SUCCESS"(){

		given :
		iterService.addExecution(iterationId, testPlanId)
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)



		and :
		List<ExecutionStep> listSteps = execService.getExecutionSteps(execution.id);



		when :
		for (ExecutionStep estep : listSteps){
			procservice.setExecutionStepStatus(estep.getId(), ExecutionStatus.SUCCESS)
		}

		Execution exec = execService.findExecution(execution.id)

		then :
		exec.getExecutionStatus() == ExecutionStatus.SUCCESS;
	}

	def "should update executionStep status and accordingly update the status of its parent execution to RUNNING"(){

		given :
		iterService.addExecution(iterationId, testPlanId)
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)



		and :
		List<ExecutionStep> listSteps = execService.getExecutionSteps(execution.id);



		when :
		ExecutionStep toBlock = listSteps.get(2);
		procservice.setExecutionStepStatus(toBlock.getId(), ExecutionStatus.SUCCESS)

		Execution exec = execService.findExecution(execution.id)

		then :
		exec.getExecutionStatus() == ExecutionStatus.RUNNING;
	}

	def "should update executionStep status and accordingly update the status of its parent execution to READY"(){

		given :
		iterService.addExecution(iterationId, testPlanId)
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)



		and :
		List<ExecutionStep> listSteps = execService.getExecutionSteps(execution.id);



		when :
		ExecutionStep toBlock = listSteps.get(2);
		procservice.setExecutionStepStatus(toBlock.getId(), ExecutionStatus.READY)

		Execution exec = execService.findExecution(execution.id)

		then :
		exec.getExecutionStatus() == ExecutionStatus.READY;
	}


	def "after step update, execution status should swap from BLOQUED to RUNNING"(){

		given :

		iterService.addExecution(iterationId, testPlanId)
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)

		and :
		List<ExecutionStep> listSteps = execService.getExecutionSteps(execution.id);

		when :
		def blocked = listSteps.get(1)
		procservice.setExecutionStepStatus(blocked.id, ExecutionStatus.BLOQUED);

		def blockedExec = execService.findExecution(execution.id)

		procservice.setExecutionStepStatus(blocked.id, ExecutionStatus.SUCCESS)

		def runningExec = execService.findExecution(execution.id);


		then :
		blockedExec.executionStatus == ExecutionStatus.BLOQUED
		runningExec.executionStatus == ExecutionStatus.RUNNING;
	}

	def "after step update, execution status should swap from BLOQUED to READY"(){

		given :

		iterService.addExecution(iterationId, testPlanId)
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)

		and :
		List<ExecutionStep> listSteps = execService.getExecutionSteps(execution.id);

		when :
		def blocked = listSteps.get(1)
		procservice.setExecutionStepStatus(blocked.id, ExecutionStatus.BLOQUED);

		def blockedExec = execService.findExecution(execution.id)

		procservice.setExecutionStepStatus(blocked.id, ExecutionStatus.READY)

		def readyExec = execService.findExecution(execution.id);


		then :
		blockedExec.executionStatus == ExecutionStatus.BLOQUED
		readyExec.executionStatus == ExecutionStatus.READY;
	}

	def "after step update, execution status should stay BLOQUED"(){

		given :

		iterService.addExecution(iterationId, testPlanId)
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)

		and :
		List<ExecutionStep> listSteps = execService.getExecutionSteps(execution.id);

		when :
		def blocked = listSteps.get(1)
		procservice.setExecutionStepStatus(blocked.id, ExecutionStatus.BLOQUED);

		def blocked2 = listSteps.get(2)
		procservice.setExecutionStepStatus(blocked2.id, ExecutionStatus.BLOQUED);

		def blockedExec = execService.findExecution(execution.id)

		procservice.setExecutionStepStatus(blocked.id, ExecutionStatus.SUCCESS)

		def stillBlockedExec = execService.findExecution(execution.id);


		then :
		blockedExec.executionStatus == ExecutionStatus.BLOQUED
		stillBlockedExec.executionStatus == ExecutionStatus.BLOQUED;
	}


	def "after step update, execution status should swap from BLOQUED to FAILURE"(){

		given :

		iterService.addExecution(iterationId, testPlanId)
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)

		and :
		List<ExecutionStep> listSteps = execService.getExecutionSteps(execution.id);

		when :
		def blocked = listSteps.get(1)
		procservice.setExecutionStepStatus(blocked.id, ExecutionStatus.BLOQUED);

		def blocked2 = listSteps.get(2)
		procservice.setExecutionStepStatus(blocked2.id, ExecutionStatus.FAILURE);

		def blockedExec = execService.findExecution(execution.id)

		procservice.setExecutionStepStatus(blocked.id, ExecutionStatus.SUCCESS)

		def failureExec = execService.findExecution(execution.id);


		then :
		blockedExec.executionStatus == ExecutionStatus.BLOQUED
		failureExec.executionStatus == ExecutionStatus.FAILURE;
	}

	def "after step update, execution status should swap from BLOQUED to SUCCESS"(){
		given :

		iterService.addExecution(iterationId, testPlanId)
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)

		and :
		List<ExecutionStep> listSteps = execService.getExecutionSteps(execution.id);

		when :
		for (ExecutionStep estep : listSteps){
			procservice.setExecutionStepStatus(estep.id,ExecutionStatus.SUCCESS);
		}


		def blocked = listSteps.get(1)
		procservice.setExecutionStepStatus(blocked.id, ExecutionStatus.BLOQUED);

		def blockedExec = execService.findExecution(execution.id)

		procservice.setExecutionStepStatus(blocked.id, ExecutionStatus.SUCCESS)

		def successExec = execService.findExecution(execution.id);


		then :
		blockedExec.executionStatus == ExecutionStatus.BLOQUED
		successExec.executionStatus == ExecutionStatus.SUCCESS;
	}

	def "after step update, execution status should swap from SUCCESS to RUNNING"(){
		given :

		iterService.addExecution(iterationId, testPlanId)
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)

		and :
		List<ExecutionStep> listSteps = execService.getExecutionSteps(execution.id);

		when :
		for (ExecutionStep estep : listSteps){
			procservice.setExecutionStepStatus(estep.id,ExecutionStatus.SUCCESS);
		}

		def successExec = execService.findExecution(execution.id)


		def blocked = listSteps.get(1)

		procservice.setExecutionStepStatus(blocked.id, ExecutionStatus.READY)

		def runningExec = execService.findExecution(execution.id);


		then :
		successExec.executionStatus == ExecutionStatus.SUCCESS
		runningExec.executionStatus == ExecutionStatus.RUNNING;
	}


	def "after step update, execution status should swap from SUCCESS to READY"(){
		given :

		iterService.addExecution(iterationId, testPlanId)
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)

		and :
		List<ExecutionStep> listSteps = execService.getExecutionSteps(execution.id);

		when :
		for (ExecutionStep estep : listSteps){
			procservice.setExecutionStepStatus(estep.id,ExecutionStatus.SUCCESS);
		}

		def successExec = execService.findExecution(execution.id)

		for (ExecutionStep estep : listSteps){
			procservice.setExecutionStepStatus(estep.id,ExecutionStatus.RUNNING);
		}

		def readyExec = execService.findExecution(execution.id);


		then :
		successExec.executionStatus == ExecutionStatus.SUCCESS
		readyExec.executionStatus == ExecutionStatus.READY;
	}


	def "after step update, execution status should swap from FAILURE to SUCCESS"(){
		given :

		iterService.addExecution(iterationId, testPlanId)
		def execList = iterService.findAllExecutions(iterationId)
		def execution = execList.get(execList.size()-1)

		and :
		List<ExecutionStep> listSteps = execService.getExecutionSteps(execution.id);

		when :
		for (ExecutionStep estep : listSteps){
			procservice.setExecutionStepStatus(estep.id,ExecutionStatus.FAILURE);
		}

		def failedExec = execService.findExecution(execution.id)

		def execStatusList = [];

		for (ExecutionStep estep : listSteps){
			procservice.setExecutionStepStatus(estep.id,ExecutionStatus.SUCCESS);
			execStatusList << execService.findExecution(execution.id).getExecutionStatus();
		}



		then :
		failedExec.executionStatus == ExecutionStatus.FAILURE
		execStatusList == [
			ExecutionStatus.FAILURE,
			ExecutionStatus.FAILURE,
			ExecutionStatus.FAILURE,
			ExecutionStatus.FAILURE,
			ExecutionStatus.SUCCESS
		]
	}




	def "should correctly update an item test plan status when the status of an execution is updated "(){
		given :
		TestCase tc1 = new TestCase(name:"tc1");
		tcNavService.addTestCaseToLibrary(libtcId, tc1)
		
		and :
		
		tpManagerService.addTestCasesToIteration([tc1.id], iterationId)
		def tp = tpManagerService.findTestPlanItemByTestCaseId(iterationId, tc1.id);

		iterService.addExecution(iterationId, tp.id)
		iterService.addExecution(iterationId, tp.id)
		iterService.addExecution(iterationId, tp.id)

		when :

		List<Execution> execList = iterService.findExecutionsByTestPlan(iterationId, tp.id);

		def exec1 = execList.get(0)
		def exec2 = execList.get(1)
		def exec3 = execList.get(2)

		def testPlan = exec1.getTestPlan()

		def status0 = testPlan.getExecutionStatus();

		exec1.setExecutionStatus(ExecutionStatus.BLOQUED)
		def status1 = testPlan.getExecutionStatus();

		//the last execution will impose it's status to the itemTestPlan
		//we're performing it now, not in last position, to check
		//the next execution being updated wont affect it
		exec3.setExecutionStatus(ExecutionStatus.SUCCESS)
		def status2 = testPlan.getExecutionStatus();


		exec2.setExecutionStatus(ExecutionStatus.BLOQUED)
		def status3 = testPlan.getExecutionStatus();

		then :
		status0 == ExecutionStatus.READY
		status1 == ExecutionStatus.READY
		status2 == ExecutionStatus.SUCCESS
		status3 == ExecutionStatus.SUCCESS


	}
}
