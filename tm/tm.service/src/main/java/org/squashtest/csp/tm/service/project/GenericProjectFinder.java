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

package org.squashtest.csp.tm.service.project;

import java.util.List;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.project.AdministrableProject;
import org.squashtest.csp.tm.domain.project.GenericProject;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;

/**
 * Finder service for Generic Projects ie both Projects and ProjectTemplates
 * @author Gregory Fouquet
 *
 */
@Transactional(readOnly = true)
public interface GenericProjectFinder {
	@PostFilter("hasPermission(filterObject, 'READ') or  hasRole('ROLE_ADMIN')")
	List<GenericProject> findAllOrderedByName(Paging paging);
	/**
	 * Will find all Projects and Templates to which the user has management access to and return them ordered according to the given params.
	 * 
	 * @param filter the {@link CollectionSorting} that holds order and paging params
	 * @return a {@link FilteredCollectionHolder} containing all projects the user has management access to, ordered according to the given params.
	 */
	@PreAuthorize("hasRole('ROLE_TM_PROJECT_MANAGER') or hasRole('ROLE_ADMIN')")
	PagedCollectionHolder<List<GenericProject>> findSortedProjects(PagingAndSorting pagingAndSorting);
	
	GenericProject findById(long projectId);
	
	/**
	 * @see {@link CustomGenericProjectManager#findAdministrableProjectById(long)}
	 */
	AdministrableProject findAdministrableProjectById(long projectId);
	
	/**
	 * @see  {@link CustomGenericProjectManager#getLastBoundServerOrDefault(long)}
	 */
	TestAutomationServer getLastBoundServerOrDefault(Long id);
	/**
	 * @see {@link CustomGenericProjectManager#findBoundTestAutomationProjects(long)}
	 */
	List<TestAutomationProject> findBoundTestAutomationProjects(long projectId);
	
	

}
