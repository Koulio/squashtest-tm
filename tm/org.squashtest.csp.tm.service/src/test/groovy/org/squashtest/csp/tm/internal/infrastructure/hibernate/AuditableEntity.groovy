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
package org.squashtest.csp.tm.internal.infrastructure.hibernate;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.squashtest.csp.tm.domain.audit.Auditable;
import org.squashtest.csp.tm.domain.audit.AuditableSupport;

@Entity
@Auditable
class AuditableEntity {
	@Id @GeneratedValue
	Long id
	String dummy
	
	// aspectj not weaving correctly this class
	AuditableSupport audit = new AuditableSupport()

	public String getCreatedBy() {
		return audit.getCreatedBy();
	}

	public Date getCreatedOn() {
		return audit.getCreatedOn();
	}

	public String getLastModifiedBy() {
		return audit.getLastModifiedBy();
	}

	public Date getLastModifiedOn() {
		return audit.getLastModifiedOn();
	}
	
}
