/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.service.requirement;

import java.util.Collection;
import java.util.List;

import org.squashtest.tm.core.foundation.lang.PathUtils;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.testcase.TestCase;

/**
 * @author Gregory Fouquet
 *
 */
public interface RequirementLibraryFinderService {

	/**
	 * Returns the collection of {@link RequirementLibrary} which Requirements can be linked by a {@link TestCase}
	 *
	 * @return
	 */
	List<RequirementLibrary> findLinkableRequirementLibraries();


	/**
	 * Returns the path of a RequirementLibraryNode given its id. The format is standard, beginning with /&lt;project-name&gt;
	 *
	 * @param entityId the id of the node.
	 * @return the path of that node.
	 */
	String getPathAsString(long entityId);

	/**
	 * Returns the id list, representing each node on the path.
	 * @param paths
	 * A list of paths. This list can be generated by {@link PathUtils#scanPath(String)}.
	 * This method assume that the path contains the project path, but it will NOT return the project Id as member of returned list
	 *
	 * @return
	 */
	List<Long> findNodeIdsByPath(List<String> paths);

	/**
	 * Return the id of the {@link RequirementLibraryNode} identified by the path given in argument
	 * @param path
	 * @return the id or null if no {@link RequirementLibraryNode} have the path
	 */
	Long findNodeIdByPath(String path);

	/**
	 * Return the id of a (synchronized) {@link Requirement}, identified by its remote key.
	 *
	 * @return the id or null if no {@link Requirement} have such remote key
	 *
	 * @param remoyeKey
	 * @return
	 */
	Long findNodeIdByRemoteKey(String remoteKey, String projectName);


	/**
	 * Return a list of synchronized Requirement, given their remote key. The ids will be returned in the same order than
	 * the input list. The resulting list might contain null entries if no synchronized Requirement was found for some remote keys.
	 *
	 * @param remoteKeys
	 * @return
	 */
	List<Long> findNodeIdsByRemoteKeys(List<String> remoteKeys, String projectName);


	/**
	 * Passing the ids of some selected RequirementLibrary and RequirementLibraryNodes (in separate collections), will return
	 * the ids of the Requirements encompassed by this selection.
	 *
	 * the requirement ids that cannot be accessed for security reason will be filtered out.
	 * @param libraryIds
	 * @param nodeIds
	 * @return
	 */
	Collection<Long> findRequirementIdsFromSelection(Collection<Long> libraryIds, Collection<Long> nodeIds);

}
