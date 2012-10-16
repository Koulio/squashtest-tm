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

import java.util.List;

import org.squashtest.csp.tm.domain.customfield.BindableEntity;
import org.squashtest.csp.tm.domain.customfield.CustomField;
import org.squashtest.csp.tm.domain.customfield.CustomFieldBinding;


/**
 * An interface for services around {@link CustomField}. This is a 'finder' service : those methods are meant to find data, not to modify them. 
 *
 *The methods in this service need not to be secured.
 * 
 *  
 * @author bsiri
 *
 */
public interface CustomFieldBindingFinderService {

	
	/**
	 * returns all the existing custom fields.
	 * 
	 * @return what I just said.
	 */
	List<CustomField> findAvailableCustomFields();
	
	
	/**
	 * returns all the custom field bindings associated to a project. 
	 * 
	 * 
	 * @param projectId
	 * @return
	 */
	List<CustomFieldBinding> findCustomFieldsForProject(long projectId);
	
	
	/**
	 * 
	 * returns all the custom field bindings associated to a project for a given entity type
	 * 
	 * @param projectId
	 * @return
	 */
	List<CustomFieldBinding> findCustomFieldsForProjectAndEntity(long projectId, BindableEntity entity);
	
	
}
