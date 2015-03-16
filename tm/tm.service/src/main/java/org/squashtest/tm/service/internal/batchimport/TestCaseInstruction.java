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
package org.squashtest.tm.service.internal.batchimport;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.squashtest.tm.domain.testcase.TestCase;

public class TestCaseInstruction extends Instruction<TestCaseTarget> implements CustomFieldHolder {
	private final TestCase testCase;
	private final Map<String, String> customFields = new HashMap<>();
	private final Queue<String> milestones = new ArrayDeque<>();

	public TestCaseInstruction(TestCaseTarget target, TestCase testCase) {
		super(target);
		this.testCase = testCase;
	}

	protected LogTrain executeUpdate(Facility facility) {
		LogTrain execLogTrain;
		execLogTrain = facility.updateTestCase(this);
		return execLogTrain;
	}

	protected LogTrain executeDelete(Facility facility) {
		LogTrain execLogTrain;
		execLogTrain = facility.deleteTestCase(getTarget());
		return execLogTrain;
	}

	protected LogTrain executeCreate(Facility facility) {
		LogTrain execLogTrain;
		execLogTrain = facility.createTestCase(this);
		return execLogTrain;
	}

	public TestCase getTestCase() {
		return testCase;
	}

	public Map<String, String> getCustomFields() {
		return customFields;
	}

	public void addCustomField(String code, String value) {
		customFields.put(code, value);
	}

	public Collection<String> getMilestones() {
		return milestones;
	}

	public void addMilestoneName(String name) {
		milestones.add(name);
	}
}
