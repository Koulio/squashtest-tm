/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.domain.tree;

import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.customreport.TreeEntityVisitor;
import org.squashtest.tm.domain.project.Project;

/**
 * Interface that every ENTITY represented in a tree by a {@link TreeLibraryNode} must implements
 * The contract assure that the entity is identified, named and can produce a reference to the {@link TreeLibraryNode}
 * The goal is to have a bidirectional association between a {@link TreeLibraryNode} and it's entity.
 * @author jthebault
 *
 */
public interface TreeEntity extends Identified{
	
	final int MAX_NAME_SIZE = 255;
	
	/**
	 * @return Name of this node.
	 */
	String getName();

	/**
	 *
	 * @param name
	 *            The name of this node. Should not be blank or null.
	 */
	void setName(String name);
	
	void accept(TreeEntityVisitor visitor);
	
	Project getProject();
	
	void setProject(Project project);
}
