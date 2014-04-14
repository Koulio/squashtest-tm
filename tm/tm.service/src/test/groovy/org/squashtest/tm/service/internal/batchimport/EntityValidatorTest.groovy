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
package org.squashtest.tm.service.internal.batchimport

import static org.squashtest.tm.service.importer.ImportStatus.*
import org.squashtest.tm.domain.testcase.ActionTestStep
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestStep
import org.squashtest.tm.service.importer.ImportStatus
import static org.squashtest.tm.service.internal.batchimport.Model.Existence.*
import org.squashtest.tm.service.internal.batchimport.Model.TargetStatus

import spock.lang.Specification
import spock.lang.Unroll

class EntityValidatorTest extends Specification {

	EntityValidator validator;
	Model model;
	
	static def longstring 		=	"IMAHMODERATELYLONGSTRINGOVERFIFTYCHARACTERSSOIFAILTESTSFORTESTCASEREFERENCES"
	
	static def toolongstring 	= 	"BOUHAHAHAHAHAHAAHAHAIAMAVERYLONGSTRINGOVERTWOHUNDREDANDFIFTYFIVECHARACTERSSOIFAILMANYTESTSOFSIZELIMITINLUCINGNAMESANDCUFSANDALLYOUNAMEIT"+
								  	"BOUHAHAHAHAHAHAAHAHAIAMAVERYLONGSTRINGOVERTWOHUNDREDANDFIFTYFIVECHARACTERSSOIFAILMANYTESTSOFSIZELIMITINLUCINGNAMESANDCUFSANDALLYOUNAMEIT"
	
	def setup(){
		validator = new EntityValidator()
		model = Mock()
		validator.model = model
	}
	
	
	
	
	def "should say that a test case is good for the service"(){
		
		given :
			TestCaseTarget target = new TestCaseTarget("/project/test-case")
			TestCase testCase = new TestCase(name:"test-case")
		
		and :
			model.getProjectStatus("project") >> new TargetStatus(EXISTS, 10l)
		
		when :
			LogTrain train = validator.basicTestCaseChecks(target, testCase)
		
		
		then :
			train.hasCriticalErrors() == false
			train.entries == []
	}
	
	
	
	
	@Unroll("should say nay to a test case because #humanmsg")
	def "should say nay for testcase for various reasons"(){
		
		given :
			model.getProjectStatus(_) >> { 
				return (it[0] == "project") ? 
					new TargetStatus(EXISTS, 10l) : 
					new TargetStatus(NOT_EXISTS, null) 
			}
		
		when :
			LogTrain train = validator.basicTestCaseChecks(target, testCase)
		
		
		then :
		
			train.entries.size() == 1
			
			def pb = train.entries[0]
			pb.status == status
			pb.i18nError == msg
			
		where :
			testCase										|	target							|	 status	|	msg								|	humanmsg
			tc(name:"test-case")							| 	tar("project/test-case")		|	FAILURE	|	Messages.ERROR_MALFORMED_PATH	|	"malformed path"
			tc(name:"")										|	tar("/project/whatever")		|	FAILURE	|	Messages.ERROR_FIELD_MANDATORY	|	"name is empty"
			tc(name:"test-case")							|	tar("/unknown/test-case")		|	FAILURE	|	Messages.ERROR_PROJECT_NOT_EXIST|	"project doesn't exists"
			tc(name:toolongstring)							|	tar("/project/"+toolongstring)	|	WARNING	|	Messages.ERROR_MAX_SIZE			|	"name is too long"
			tc(name:"test-case", reference:longstring)		|	tar("/project/test-case")		|	WARNING	|	Messages.ERROR_MAX_SIZE			|	"ref is too long"
			
	}
	
	
	def "should say that this test step is good for the service"(){
		
		given :
			TestStepTarget target  = new TestStepTarget(new TestCaseTarget("/project/test-case"), 2)
			TestStep astep = new ActionTestStep(action:"ready for action", expectedResult : "expected") 
		
		and :
			model.getProjectStatus("project") >> new TargetStatus(EXISTS, 10l)
			
		and :
			model.getStatus(_) >> new TargetStatus(EXISTS, 10l)
		
		when :
			LogTrain train = validator.basicTestStepChecks(target, astep)
		
		
		then :
			train.hasCriticalErrors() == false
			train.entries == []
		
	}
	
	@Unroll("should say nay to an action step because #humanmsg")
	def "should say nay for action step for various reasons"(){
		
		given :
			model.getProjectStatus(_) >> {
				return (it[0] == "project") ?
					new TargetStatus(EXISTS, 10l) :
					new TargetStatus(NOT_EXISTS, null)
			}
			
		and :
			model.getStatus(_) >> {
				return (it[0].path ==~ /.*test-case$/) ?
					new TargetStatus(EXISTS, 10l) :
					new TargetStatus(NOT_EXISTS, null)
			}
		when :
			LogTrain train = validator.basicTestStepChecks(target, astep)
		
		
		then :
			
			train.entries.size() == 1
			
			def pb = train.entries[0]
			pb.status == status
			pb.i18nError == msg
			
		where :
			astep					|	target									|	 status	|	msg								|	humanmsg
			ast(action:"action")	| 	steptarget("project/test-case", null)	|	FAILURE	|	Messages.ERROR_MALFORMED_PATH	|	"malformed path"
			ast(action:"action")	|	steptarget("/project/whatever", null)	|	FAILURE	|	Messages.ERROR_TC_NOT_FOUND		|	"test case doesn't exist"
			ast(action:"action")	|	steptarget("/unknown/test-case", null)	|	FAILURE	|	Messages.ERROR_PROJECT_NOT_EXIST|	"project doesn't exists"

	}
	

	def "should say that call step is fine"(){
		
		given :
			TestStepTarget target  = new TestStepTarget(new TestCaseTarget("/project/test-case"), 2)
			TestCaseTarget called = new TestCaseTarget("/project/called")
			TestStep cstep = new CallTestStep() 
		
		and :
			model.getStatus(_) >> new TargetStatus(EXISTS, 10l)
			model.wouldCreateCycle(_) >> false 
		
		when :
			LogTrain train = validator.validateCallStep(target, cstep, called)
		
		
		then :
			train.hasCriticalErrors() == false
			train.entries == []

	}
	
	
	@Unroll("should say nay to call step because #humanmsg")
	def "should say nay to call step for various reasons"(){
		
		given :
			model.getStatus(_) >> calledstatus
			model.wouldCreateCycle(_, _) >> calledcycle
			
		and : 
			def target = steptarget("/project/test-case", 5)
			def called = tar("/project/autre")
			def cstep = cst()
			
		when :
			LogTrain train = validator.validateCallStep(target, cstep, called)
		
		then :
			
			train.entries.size() == 1
			
			def pb = train.entries[0]
			pb.status == status
			pb.i18nError == msg
			
		where :
			calledstatus 				|	calledcycle		|	status	|	msg									|	humanmsg
			status(NOT_EXISTS,null)		|	false			|	FAILURE	|	Messages.ERROR_CALLED_TC_NOT_FOUND	|	"called test doesn't exist"
			status(EXISTS, 12l)			|	true			|	FAILURE	|	Messages.ERROR_CYCLIC_STEP_CALLS	|	"such calls would induce cycles"
	
	}
	
	
	def "should say that the parameter is good to go"(){
		
		given :
			ParameterTarget target = ptarget("/project/test-case", "param")
			
		and :
			model.getStatus(_) >> status(EXISTS, 10l)
			model.getProjectStatus("project") >> status(EXISTS, 15l)  
		
		when :
			LogTrain train = validator.basicParameterChecks(target)
		
		then :
			train.hasCriticalErrors() == false
			train.entries == []
		
	}
	
	
	@Unroll("should say nay to a parameter because #humanmsg")
	def "should reject the parameter for various reasons"(){
		
		given :
			model.getStatus(_) >> {
				return (it[0].path ==~ /.*test-case$/) ?
					new TargetStatus(EXISTS, 10l) :
					new TargetStatus(NOT_EXISTS, null)
			}
			
			model.getProjectStatus(_) >> {
				return (it[0] == "project") ?
					new TargetStatus(EXISTS, 10l) :
					new TargetStatus(NOT_EXISTS, null)
			}

			
		when :
			LogTrain train = validator.basicParameterChecks(target)
		
		then :
			System.out.println(humanmsg)
			train.entries.each { println it.i18nError }	
		
			train.entries.size() == 1
			
			def pb = train.entries[0]
			pb.status == status
			pb.i18nError == msg
			
		where :
			target 										|	status	|	msg									|	humanmsg
			ptarget("project/test-case", "param")		|	FAILURE	|	Messages.ERROR_MALFORMED_PATH		|	"malformed path"
			ptarget("/project/unknown", "param")		|	FAILURE	|	Messages.ERROR_TC_NOT_FOUND			|	"test case doesn't exists"
			ptarget("/unknown/test-case", "param")		|	FAILURE	|	Messages.ERROR_PROJECT_NOT_EXIST	|	"project doesn't exists"
			ptarget("/project/test-case", "")			|	FAILURE	|	Messages.ERROR_FIELD_MANDATORY		|	"param has no name"
			ptarget("/project/test-case", toolongstring)|	WARNING	|	Messages.ERROR_MAX_SIZE				|	"param name is too long"
			
		
	}
	
	
	// ******************** utils *****************************
	
	def status(ex, id){
		return new TargetStatus(ex, id)
	}
	
	def tc(args){
		return new TestCase(args)
	}
	
	def cst(){
		return new CallTestStep()
	}
	
	def ast(args){
		return new ActionTestStep(args)
	}
	
	def tar(arg){
		return new TestCaseTarget(arg)
	}
	
	def steptarget(path,idx){
		return new TestStepTarget(tar(path), idx)
	}
	
	def ptarget(path, name){
		return new ParameterTarget(tar(path), name)
	}

	
}
