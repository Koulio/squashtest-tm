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
package org.squashtest.tm.web.internal.wizard;

import java.util.Collection;

import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.api.workspace.WorkspaceType;

/**
 * @author Gregory Fouquet
 * 
 */
public interface WorkspaceWizardManager {
	
	
	WorkspaceWizard findById(String wizardId);
	
	
	Collection<WorkspaceWizard> findAll();
	
	
	Collection<WorkspaceWizard> findAllByWorkspace(WorkspaceType workspace);
	
	
	/**
	 * returns all the wizards enabled for that project, regardless of the workspace type.
	 * 
	 * @param projectId
	 * @return
	 */
	Collection<WorkspaceWizard> findEnabledWizards(long projectId);	
	
	/**
	 * returns all the wizards enabled for that project, restricted to those of the corresponding workspace type.
	 * 
	 * @param projectId
	 * @return
	 */
	Collection<WorkspaceWizard> findEnabledWizards(long projectId, WorkspaceType workspace);
	
	/**
	 * returns all the wizards enabled for that project, restricted to those of the corresponding workspace types.
	 * 
	 * @param projectId
	 * @return
	 */
	Collection<WorkspaceWizard> findEnabledWizards(long projectId, WorkspaceType... workspaces);
	

	
	/**
	 * returns all the wizards disabled for that project, regardless of the workspace type.
	 * 
	 * @param projectId
	 * @return
	 */
	Collection<WorkspaceWizard> findDisabledWizards(long projectId);
	
	/**
	 * returns all the wizards disabled for that project, restricted to those of the corresponding workspace type.
	 * 
	 * @param projectId
	 * @return
	 */
	Collection<WorkspaceWizard> findDisabledWizards(long projectId, WorkspaceType workspace);
	
	
	/**
	 * returns all the wizards disabled for that project, restricted to those of the corresponding workspace types.
	 * 
	 * @param projectId
	 * @return
	 */
	Collection<WorkspaceWizard> findDisabledWizards(long projectId, WorkspaceType... workspaces);
	
	

	
}


