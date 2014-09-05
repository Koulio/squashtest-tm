/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.JsonParser;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.RequestExecutor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.BuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.BuildStep;


public class StartBuild extends BuildStep<StartBuild> implements HttpBasedStep{

	private RequestExecutor requestExecutor = RequestExecutor.getInstance();
	
	private HttpClient client;
	
	private HttpMethod method;
	
	

	public StartBuild(BuildProcessor processor) {
		super(processor);
	}

	@Override
	public void setClient(HttpClient client) {
		this.client = client;
	}

	@Override
	public void setMethod(HttpMethod method) {
		this.method = method;
	}
	
	@Override
	public void setParser(JsonParser parser) {
		//nothing, no parser needed
	}
	
	
	@Override
	public void setBuildAbsoluteId(BuildAbsoluteId absoluteId) {
		//not needed here
	}
	
	
	//**************** code ********************

	@Override
	public boolean needsRescheduling() {
		return false;
	}


	@Override
	public void perform() throws Exception {
		requestExecutor.execute(client, method);
	}

	@Override
	public void reset() {
	
	}

	@Override
	public Integer suggestedReschedulingInterval() {
		return null;
	}
	

}
