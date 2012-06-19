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
// CHECKSTYLE:OFF
package org.squashtest.csp.tm.domain.audit;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Transient;
import org.squashtest.csp.tm.domain.audit.Auditable;
import org.squashtest.csp.tm.domain.audit.AuditableSupport;
import  org.squashtest.csp.tm.domain.audit.AuditableMixin;
import javax.persistence.Embedded;

/**
 * This aspect adds the {@link AuditableMixin} mixin to entities annotated with @Audidable
 * 
 * @author Gregory Fouquet
 * 
 */
public aspect AuditableMixinAspect {
	declare parents : @Auditable  @Entity * implements AuditableMixin;

	/**
	 * Introduced field name is not predictible which means not queryable. Workaround is forcing hibernate to access the field through getter/setter (the @Access annotation on getter below). The field must then be annotated @Transient otherwise hibernate persists both the field and the "property". 
	 */
	@Transient private
	AuditableSupport AuditableMixin.audit = new AuditableSupport();

	public Date AuditableMixin.getCreatedOn() {
		return this.getAudit().getCreatedOn();
	}
	
	public String AuditableMixin.getCreatedBy() {
		return this.getAudit().getCreatedBy();
	}

	public Date AuditableMixin.getLastModifiedOn() {
		return this.getAudit().getLastModifiedOn();
	}

	public String AuditableMixin.getLastModifiedBy() {
		return this.getAudit().getLastModifiedBy();
	}
	
	public void AuditableMixin.setCreatedBy(String createdBy) {
		this.getAudit().setCreatedBy(createdBy);
	}

	public void AuditableMixin.setCreatedOn(Date createdOn) {
		this.getAudit().setCreatedOn(createdOn);
	}

	public void AuditableMixin.setLastModifiedBy(String lastModifiedBy) {
		this.getAudit().setLastModifiedBy(lastModifiedBy);
	}

	public void AuditableMixin.setLastModifiedOn(Date lastModifiedOn) {
		this.getAudit().setLastModifiedOn(lastModifiedOn);
	}	
	
	
	
	@Embedded @Access(AccessType.PROPERTY)
	public AuditableSupport AuditableMixin.getAudit() {
		return this.audit;
	}

	public void AuditableMixin.setAudit(AuditableSupport audit) {
		this.audit = audit;
	}
}
