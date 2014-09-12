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

import java.util.List;

import org.squashtest.tm.core.dynamicmanager.annotation.DynamicDao;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.domain.event.RequirementAuditEvent;

@DynamicDao(entity = RequirementAuditEvent.class, hasCustomImplementation = false)
public interface RequirementAuditEventDao {
	void persist(RequirementAuditEvent event);

	List<RequirementAuditEvent> findAllByRequirementVersionIdOrderedByDate(long requirementVersionId, Paging paging);

	long countByRequirementVersionId(long requirementVersionId);

}
