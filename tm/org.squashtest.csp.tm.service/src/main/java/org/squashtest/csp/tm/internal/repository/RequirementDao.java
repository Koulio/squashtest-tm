/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.internal.repository;

import java.util.List;

import org.squashtest.csp.tm.domain.requirement.ExportRequirementData;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.domain.requirement.RequirementSearchCriteria;

public interface RequirementDao extends EntityDao<Requirement> {
	/**
	 * returns all the requirements matching the given ids.
	 *
	 * @param requirementsIds
	 * @return
	 */
	List<Requirement> findAllByIdList(List<Long> requirementsIds);

	List<String> findNamesInFolderStartingWith(long folderId, String nameStart);

	List<String> findNamesInLibraryStartingWith(long libraryId, String nameStart);

	@SuppressWarnings("rawtypes")
	List<RequirementLibraryNode> findAllBySearchCriteria(RequirementSearchCriteria criteria);

	@SuppressWarnings("rawtypes")
	List<RequirementLibraryNode> findAllBySearchCriteriaOrderByProject(RequirementSearchCriteria criteria);

	List<ExportRequirementData> findRequirementToExportFromFolder(List<Long> folderIds);
	
	List<ExportRequirementData> findRequirementToExportFromLibrary(List<Long> folderIds);
	
}
