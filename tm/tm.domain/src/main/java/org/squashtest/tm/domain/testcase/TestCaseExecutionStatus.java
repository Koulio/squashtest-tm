/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.domain.testcase;

import org.squashtest.tm.domain.execution.ExecutionStatus;

/**
 * Pojo used to link a test case with an execution status.
 * Used for 4433 and 4334
 * @author jthebault
 *
 */
public class TestCaseExecutionStatus {
	
	private ExecutionStatus status;
	private Long testCaseId;
	
	public TestCaseExecutionStatus(ExecutionStatus status, Long testCaseId) {
		super();
		this.status = status;
		this.testCaseId = testCaseId;
	}
	
	public ExecutionStatus getStatus() {
		return status;
	}
	public void setStatus(ExecutionStatus status) {
		this.status = status;
	}
	public Long getTestCaseId() {
		return testCaseId;
	}
	public void setTestCaseId(Long testCaseId) {
		this.testCaseId = testCaseId;
	}
}