/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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

package org.squashtest.csp.tm.internal.service.audit;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.squashtest.csp.core.infrastructure.collection.PagedCollection;
import org.squashtest.csp.core.infrastructure.collection.Paging;
import org.squashtest.csp.core.infrastructure.collection.PagingBackedPagedCollection;
import org.squashtest.csp.tm.domain.event.RequirementAuditEvent;
import org.squashtest.csp.tm.internal.repository.RequirementAuditEventDao;
import org.squashtest.csp.tm.service.audit.RequirementAuditTrailService;

/**
 * @author Gregory Fouquet
 * 
 */
@Service
public class RequirementAuditTrailServiceImpl implements RequirementAuditTrailService {
	@Inject
	private RequirementAuditEventDao auditEventDao;

	/**
	 * @see org.squashtest.csp.tm.service.audit.RequirementAuditTrailService#findAllByRequirementIdOrderedByDate(long,
	 *      org.squashtest.csp.core.infrastructure.collection.Paging)
	 */
	@Override
	public PagedCollection<List<RequirementAuditEvent>> findAllByRequirementIdOrderedByDate(long requirementId,
			Paging paging) {
		
		List<RequirementAuditEvent> pagedEvents = auditEventDao.findAllByRequirementIdOrderedByDate(requirementId, paging);
		long nbOfEvents = auditEventDao.countByRequirementId(requirementId);
		
		return new PagingBackedPagedCollection<List<RequirementAuditEvent>>(paging, nbOfEvents, pagedEvents);
	}

}
