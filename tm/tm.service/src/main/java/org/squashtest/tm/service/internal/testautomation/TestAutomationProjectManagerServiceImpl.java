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
package org.squashtest.tm.service.internal.testautomation;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.internal.repository.TestAutomationProjectDao;
import org.squashtest.tm.service.internal.repository.TestAutomationServerDao;
import org.squashtest.tm.service.testautomation.TestAutomationProjectFinderService;
import org.squashtest.tm.service.testautomation.TestAutomationProjectManagerService;
import org.squashtest.tm.service.testautomation.spi.TestAutomationConnector;
import org.squashtest.tm.service.testautomation.spi.TestAutomationException;

@Transactional
@Service("squashtest.tm.service.TestAutomationProjectManagementService")
public class TestAutomationProjectManagerServiceImpl implements TestAutomationProjectManagerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestAutomationConnector.class);

	@Inject
	private TestAutomationProjectDao projectDao;

	@Inject
	private TestAutomationConnectorRegistry connectorRegistry;

	@Inject
	private TestAutomationServerDao serverDao;

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TM_PROJECT_MANAGER')")
	public void persist(TestAutomationProject newProject) {
		projectDao.persist(newProject);
	}

	@Override
	public TestAutomationProject findProjectById(long projectId) {
		return projectDao.findById(projectId);
	}

	@Override
	public void deleteProject(long projectId) {
		projectDao.deleteProjectsByIds(Arrays.asList(projectId));
	}

	@Override
	public void deleteAllForTMProject(long tmProjectId) {
		Collection<Long> allprojects = projectDao.findAllByTMProject(tmProjectId);
		projectDao.deleteProjectsByIds(allprojects);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TM_PROJECT_MANAGER')")
	public void changeLabel(long projectId, String label) {
		TestAutomationProject project = projectDao.findById(projectId);
		project.setLabel(label);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TM_PROJECT_MANAGER')")
	public void changeJobName(long projectId, String jobName) {
		TestAutomationProject project = projectDao.findById(projectId);
		project.setJobName(jobName);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TM_PROJECT_MANAGER')")
	public void changeSlaves(long projectId, String slaveList) {
		TestAutomationProject project = projectDao.findById(projectId);
		project.setSlaves(slaveList);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TM_PROJECT_MANAGER')")
	public Collection<TestAutomationProject> listProjectsOnServer(String serverName) {

		TestAutomationServer server = serverDao.findByName(serverName);

		return listProjectsOnServer(server);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TM_PROJECT_MANAGER')")
	public Collection<TestAutomationProject> listProjectsOnServer(Long serverId) {
		TestAutomationServer server = serverDao.findById(serverId);

		return listProjectsOnServer(server);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TM_PROJECT_MANAGER')")
	public Collection<TestAutomationProject> listProjectsOnServer(TestAutomationServer server) {

		TestAutomationConnector connector = connectorRegistry.getConnectorForKind(server.getKind());

		connector.checkCredentials(server);
		try {
			return connector.listProjectsOnServer(server);
		} catch (TestAutomationException ex) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Test Automation : failed to list projects on server : ", ex);
			}
			throw ex;
		}
	}

	/**
	 * @see TestAutomationProjectFinderService#findProjectUrls(List)
	 */
	@Override
	public Map<String, URL> findProjectUrls(Collection<TestAutomationProject> taProjects) {
		Map<String, URL> result = new HashMap<String, URL>(taProjects.size());
		for (TestAutomationProject testAutomationProject : taProjects) {
			URL url = findProjectURL(testAutomationProject);
			result.put(testAutomationProject.getJobName(), url);
		}
		return result;
	}

	private URL findProjectURL(TestAutomationProject testAutomationProject) {
		TestAutomationServer server = testAutomationProject.getServer();
		TestAutomationConnector connector = connectorRegistry.getConnectorForKind(server.getKind());
		return connector.findTestAutomationProjectURL(testAutomationProject);
	}

	/**
	 * @see TestAutomationProjectFinderService#hasExecutedTests(long)
	 */
	@Override
	public boolean hasExecutedTests(long projectId) {
		return projectDao.haveExecutedTestsByIds(Arrays.asList(projectId));
	}
}
