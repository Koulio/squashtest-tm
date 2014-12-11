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
package org.squashtest.tm.web.internal.model.rest;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.squashtest.tm.domain.execution.Execution;

@XmlRootElement(name="execution")
public class RestExecution {

	@XmlElement(name="id")
	public Long id;
	
	@XmlElement(name="campaign")
	public RestCampaignStub restCampaignStub;
	
	@XmlElement(name="iteration")
	public RestIterationStub restIterationStub;
	
	@XmlElement(name="testcase")
	public RestTestCaseStub restTestCaseStub;
	
	@XmlElement(name="status")
	public String status;
	
	@XmlElement(name="execution-date")
	public Date executionDate; 
	
	public RestExecution(){
		super();
	}
	
	public RestExecution(Execution execution) {
		this.id = execution.getId();
		this.restCampaignStub = new RestCampaignStub(execution.getCampaign());
		this.restIterationStub = new RestIterationStub(execution.getIteration());
		this.restTestCaseStub = new RestTestCaseStub(execution.getReferencedTestCase());
		this.status = execution.getStatus().name();
		this.executionDate = execution.getLastExecutedOn();
	}


}
