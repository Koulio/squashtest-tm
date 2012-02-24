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
package org.squashtest.csp.tm.service;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;

@Transactional
public interface TestSuiteExecutionProcessingService {
	/**
	 * <p>
	 * will find the first unexecuted step of the execution or return -1<br>
	 * returns -1 if all steps have a status, or, if there is no steps
	 * </p>
	 * 
	 * @param executionId
	 * @return the first unexecuted {@linkplain ExecutionStep} or -1
	 */
	int findIndexOfFirstUnexecuted(long executionId);

	/**
	 * <p>
	 * will delete all existing executions and returns the execution step were to restart the test suite execution<br>
	 * or null if there is no execution step
	 * </p>
	 * 
	 * @param testSuiteId
	 * @return
	 */
	ExecutionStep restartExecution(long testSuiteId);

	/**
	 * <p>
	 * Should start a new execution for the next executable test plan item of the given test suite. suite's test plan.
	 * </p>
	 * 
	 * @param testSuiteId
	 * @param testPlanItemId
	 * @return
	 */
	Execution startNextExecution(long testSuiteId, long testPlanItemId);

	/**
	 * <p>
	 * returns the execution were to resume the test suite<br>
	 * or null if no execution is to be resumed because :
	 * <ul>
	 * <li>all terminated, or</li>
	 * <li>no execution-step on executions, or</li>
	 * <li>no execution and test-case deleted</li>
	 * </ul>
	 * <p>
	 * if there is no execution should start a new execution for the given test suite, ie create an execution for the
	 * first test case of this suite's test plan
	 * </p>
	 * 
	 * @param testSuiteId
	 * @return the {@linkplain Execution} where to resume or null
	 */
	Execution startResume(long testSuiteId);

	/**
	 * <p>
	 * tells if a test suite has at least one executable item in its test plan after the given item.
	 * </p>
	 * 
	 * @param testSuiteId
	 * @param testPlanItemId
	 * @return
	 */
	boolean hasMoreExecutableItems(long testSuiteId, long testPlanItemId);
}
