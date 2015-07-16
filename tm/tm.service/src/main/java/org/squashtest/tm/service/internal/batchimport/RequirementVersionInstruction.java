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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.squashtest.tm.domain.requirement.RequirementVersion;

public class RequirementVersionInstruction extends Instruction<RequirementVersionTarget> implements CustomFieldHolder {

	private final RequirementVersion requirementVersion;
	private final Map<String, String> customFields = new HashMap<>();
	private final String[] milestones = {};

	public RequirementVersionInstruction(RequirementVersionTarget target, RequirementVersion requirementVersion) {
		super(target);
		this.requirementVersion = requirementVersion;
	}

	@Override
	protected LogTrain executeUpdate(Facility facility) {
		LogTrain logTrain = facility.updateRequirementVersion(this);
		return logTrain;
	}

	@Override
	protected LogTrain executeDelete(Facility facility) {
		LogTrain logTrain = facility.deleteRequirementVersion(this);
		return logTrain;
	}

	@Override
	protected LogTrain executeCreate(Facility facility) {
		LogTrain logTrain = facility.createRequirementVersion(this);
		return logTrain;
	}

	@Override
	public void addCustomField(String code, String value) {
		customFields.put(code, value);
	}

	public RequirementVersion getRequirementVersion() {
		return requirementVersion;
	}

	public Map<String, String> getCustomFields() {
		return customFields;
	}

	public List<String> getMilestones() {
		return Arrays.asList(milestones);
	}


}
