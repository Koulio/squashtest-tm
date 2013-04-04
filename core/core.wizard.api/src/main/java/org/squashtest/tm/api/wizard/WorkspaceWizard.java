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

package org.squashtest.tm.api.wizard;

import org.squashtest.tm.api.plugin.EntityReference;
import org.squashtest.tm.api.widget.MenuItem;
import org.squashtest.tm.api.workspace.WorkspaceType;

/**
 * 
 * @author Gregory Fouquet
 * 
 */
public interface WorkspaceWizard extends WizardPlugin {
	/**
	 * This wizard is available for display in the workspace returned by this method.
	 * 
	 * @return the {@link WorkspaceType} where this wizard can be started / displayed. SHould not return
	 *         <code>null</code>
	 */
	WorkspaceType getDisplayWorkspace();

	/**
	 * {@link MenuItem} which should be used to generate a menu in the workspace's wizard menu.
	 * 
	 * @return
	 */
	MenuItem getWizardMenu();
	

	/**
	 * Asks the plugin to validate against an EntityReference. Must either succeed, or throw a {@link WizardValidationException}.
	 * 
	 * @param reference to a given object
	 */
	void validate(EntityReference reference) throws WizardValidationException;
	
}
