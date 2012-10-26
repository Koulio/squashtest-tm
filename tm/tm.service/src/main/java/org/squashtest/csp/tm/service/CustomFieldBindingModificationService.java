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
package org.squashtest.csp.tm.service;

import org.squashtest.csp.tm.domain.customfield.BindableEntity;
import org.squashtest.csp.tm.domain.customfield.CustomField;
import org.squashtest.csp.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.csp.tm.domain.project.Project;

/**
 * An interface for services around {@link CustomField}. 
 * The user calling the following methods must have a role 'admin' or 'project manager'.
 * 
 * 
 * @author bsiri
 *
 */
public interface CustomFieldBindingModificationService extends CustomFieldBindingFinderService{

	
	/**
	 * Will attach a {@link CustomField} to a {@link Project}. The details and conditions of that binding 
	 * is described in the {@link CustomFieldBinding} newBinding. The new binding will be inserted last.
	 * 
	 * @param projectId
	 * @param customFieldId
	 * @param entity
	 * @param newBinding
	 */
	void addNewCustomFieldBinding(long projectId, BindableEntity entity, long customFieldId, CustomFieldBinding newBinding);
	
	
	
}
