/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.testcase;

import java.util.ArrayList;
import java.util.List;

import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.TestCase;

public class RequirementVerifierView {
	private TestCase verifier;
	private ActionTestStep verifyingStep;
	private String type;

	public RequirementVerifierView(TestCase requirementVerifier) {
		this.verifier = requirementVerifier;
		type = "test-cases";
	}
	
	public RequirementVerifierView(ActionTestStep verifyingStep) {
		this.verifier = verifyingStep.getTestCase();
		this.verifyingStep = verifyingStep;
		type = "test-step";
	}
	
	public TestCase getVerifier() {
		return verifier;
	}

	public void setVerifier(TestCase verifier) {
		this.verifier = verifier;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ActionTestStep getVerifyingStep() {
		return verifyingStep;
	}

	public void setVerifyingStep(ActionTestStep verifyingStep) {
		this.verifyingStep = verifyingStep;
	}
	
	public List<RequirementVersionCoverageView> getCoverages(){
		List<RequirementVersionCoverageView> coverages = new ArrayList<RequirementVersionCoverageView>(0);
		for(RequirementVersionCoverage rc : verifier.getRequirementVersionCoverages()){
			RequirementVersionCoverageView coverage = new RequirementVersionCoverageView(rc, verifyingStep);
			coverages.add(coverage);
		}
		return coverages;
	}
	
	public static final class RequirementVersionCoverageView{
		private RequirementVersion version;
		private boolean verifiedByStep = false;

		public RequirementVersionCoverageView(RequirementVersionCoverage rc, ActionTestStep step) {
			this.version = rc.getVerifiedRequirementVersion();
			if(step != null){
				for(ActionTestStep verifyingStep : rc.getVerifyingSteps()){
					if(step.getId().equals(verifyingStep.getId())){
						verifiedByStep = true;
						break;
					}
				}
			}
		}

		public RequirementVersion getVersion() {
			return version;
		}

		public boolean isVerifiedByStep() {
			return verifiedByStep;
		}
		
		
	}
	
	
}
