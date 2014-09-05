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
package org.squashtest.tm.service.internal.repository;

import java.util.List;

import org.squashtest.tm.core.dynamicmanager.annotation.DynamicDao;
import org.squashtest.tm.core.dynamicmanager.annotation.QueryParam;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;

/**
 * @author Gregory Fouquet
 * 
 */
@DynamicDao(entity = GenericProject.class)
public interface GenericProjectDao extends CustomGenericProjectDao {
	long countGenericProjects();


	List<GenericProject> findAll(PagingAndSorting pagingAndSorting);

	List<GenericProject> findProjectsFiltered(PagingAndSorting pagingAndSorting, @QueryParam("filter") String filter);


	GenericProject findById(long projectId);

	// ************************* test automation section **********************

	List<TestAutomationProject> findBoundTestAutomationProjects(@QueryParam("projectId") long id);

	List<String> findBoundTestAutomationProjectJobNames(@QueryParam("projectId") long id);

	List<String> findBoundTestAutomationProjectLabels(@QueryParam("projectId") long projectId);

	/**
	 * @param idList
	 * @return
	 */
	List<GenericProject> findAllByIds(List<Long> ids);

	List<GenericProject> findAllByIds(List<Long> idList, Sorting defaultSorting);

	/**
	 * Simply remove entity
	 * @param project : the {@link GenericProject} to remove
	 */
	void remove(GenericProject project);

	long countByName(String name);

	TestAutomationServer findTestAutomationServer(@QueryParam("projectId") long projectId);


}
