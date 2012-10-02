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
package org.squashtest.csp.tm.domain.event;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.squashtest.csp.tm.domain.requirement.ExportRequirementData;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.domain.requirement.RequirementSearchCriteria;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.internal.repository.RequirementDao;

/**
 * @author Gregory Fouquet
 * 
 */
public class StubRequirementDao extends StubEntityDao<Requirement> implements RequirementDao {

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#findAllByIds(java.util.Collection)
	 */
	@Override
	public List<Requirement> findAllByIds(Collection<Long> requirementsIds) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#findNamesInFolderStartingWith(long,
	 *      java.lang.String)
	 */
	@Override
	public List<String> findNamesInFolderStartingWith(long folderId, String nameStart) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#findNamesInLibraryStartingWith(long,
	 *      java.lang.String)
	 */
	@Override
	public List<String> findNamesInLibraryStartingWith(long libraryId, String nameStart) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#findAllBySearchCriteria(org.squashtest.csp.tm.domain.requirement.RequirementSearchCriteria)
	 */
	@Override
	public List<RequirementLibraryNode> findAllBySearchCriteria(RequirementSearchCriteria criteria) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#findAllBySearchCriteriaOrderByProject(org.squashtest.csp.tm.domain.requirement.RequirementSearchCriteria)
	 */
	@Override
	public List<RequirementLibraryNode> findAllBySearchCriteriaOrderByProject(RequirementSearchCriteria criteria) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#findRequirementToExportFromFolder(java.util.List)
	 */
	@Override
	public List<ExportRequirementData> findRequirementToExportFromNodes(List<Long> folderIds) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#findRequirementToExportFromProject(java.util.List)
	 */
	@Override
	public List<ExportRequirementData> findRequirementToExportFromProject(List<Long> folderIds) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#findDistinctRequirementsCriticalitiesVerifiedByTestCases(java.util.List)
	 */
	@Override
	public List<RequirementCriticality> findDistinctRequirementsCriticalitiesVerifiedByTestCases(Set<Long> testCasesIds) {
		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#findDistinctRequirementsCriticalities(java.util.List)
	 */
	@Override
	public List<RequirementCriticality> findDistinctRequirementsCriticalities(List<Long> requirementsIds) {

		return null;
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.RequirementDao#findVersions(Long)
	 */
	@Override
	public List<RequirementVersion> findVersions(Long requirementId) {
		return null;
	}

	@Override
	public List<RequirementVersion> findVersionsForAll(List<Long> requirementIds) {
		return null;
	}

	@Override
	public List<Requirement> findAll() {
		return null;
	}

	@Override
	public List<Requirement> findAllByIdListOrderedByName(List<Long> requirementsIds) {
		return null;
	}

}
