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
package org.squashtest.tm.service.internal.repository;

import java.util.Collection;
import java.util.List;

import org.squashtest.tm.domain.customfield.CustomFieldBinding;

public interface CustomCustomFieldBindingDao {

	void removeCustomFieldBindings(List<Long> bindingIds);

	/**
	 * returns the bindings grouped by project and entity, sorted by position
	 * @param ids
	 * @return
	 */
	List<CustomFieldBinding> findAllByIds(Collection<Long> ids);

	
	/**
	 * returns the bindings grouped by project and entity, sorted by position
	 * @param ids
	 * @return
	 */
	List<CustomFieldBinding> findAllByIds(List<Long> ids);
	

}
