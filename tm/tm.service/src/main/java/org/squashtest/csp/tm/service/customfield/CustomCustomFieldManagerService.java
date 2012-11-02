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
package org.squashtest.csp.tm.service.customfield;

import javax.validation.constraints.NotNull;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.customfield.CustomField;

/**
 * Custom-Field manager services which cannot be dynamically generated.
 * 
 * @author mpagnon
 * 
 */
@Transactional
public interface CustomCustomFieldManagerService extends CustomFieldFinderService {
	/**
	 * Will delete the custom-field entity
	 * 
	 * @param customFieldId
	 *            : the id of the custom field to delete
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	void deleteCustomField(long customFieldId);

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	void persist(@NotNull CustomField newCustomField);

}
