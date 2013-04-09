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
package org.squashtest.csp.core.bugtracker.service;

import java.util.List;

import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.core.ProjectNotFoundException;
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnector;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.bugtracker.definition.RemoteProject;


/**
 * Could also have been called LegacyBugtrackerConnectorAdapter
 * 
 * @author bsiri
 *
 */

public class SimpleBugtrackerConnectorAdapter implements
		InternalBugtrackerConnector {
	
	private BugTrackerConnector connector;
	
	public SimpleBugtrackerConnectorAdapter(){
		super();
	}
	
	public SimpleBugtrackerConnectorAdapter(BugTrackerConnector connector){
		super();
		this.connector = connector;
	}
	
	public void setConnector(BugTrackerConnector connector){
		this.connector = connector;
	}

	@Override
	public void authenticate(AuthenticationCredentials credentials) {
		connector.authenticate(credentials);
	}

	@Override
	public void checkCredentials(AuthenticationCredentials credentials)
			throws BugTrackerNoCredentialsException, BugTrackerRemoteException {
		connector.checkCredentials(credentials);
	}

	@Override
	public String makeViewIssueUrlSuffix(String issueId) {
		return connector.makeViewIssueUrlSuffix(issueId);
	}

	@Override
	public RemoteProject findProject(String projectName)
			throws ProjectNotFoundException, BugTrackerRemoteException {
		return connector.findProject(projectName);
	}

	@Override
	public RemoteProject findProjectById(String projectId)
			throws ProjectNotFoundException, BugTrackerRemoteException {
		return connector.findProject(projectId);
	}

	@Override
	public RemoteIssue createIssue(RemoteIssue issue)
			throws BugTrackerRemoteException {
		return connector.createIssue((BTIssue)issue);
	}

	@Override
	public BugTrackerInterfaceDescriptor getInterfaceDescriptor() {
		return connector.getInterfaceDescriptor();
	}

	@Override
	public RemoteIssue findIssue(String key) {
		return connector.findIssue(key);
	}

	@Override
	public List<RemoteIssue> findIssues(List<String> issueKeyList) {
		return (List)connector.findIssues(issueKeyList);
	}

}
