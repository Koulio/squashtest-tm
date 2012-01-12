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
package org.squashtest.csp.tm.domain.requirement;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.validation.constraints.NotNull;

import org.squashtest.csp.tm.domain.attachment.AttachmentHolder;
import org.squashtest.csp.tm.domain.attachment.AttachmentList;

/**
 * Entity requirement
 * 
 * Note that much of its setters will throw an IllegalRequirementModificationException if a modification is attempted
 * while the status does not allow it.
 * 
 * @author bsiri
 * 
 */

@Entity
@PrimaryKeyJoinColumn(name = "RLN_ID")
public class Requirement extends RequirementLibraryNode<RequirementVersion> implements AttachmentHolder {
	/**
	 * The resource of this requirement is the latest version of the requirement.
	 */
	@OneToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "LATEST_VERSION_ID")
	private RequirementVersion resource;
	
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "requirement")
	@OrderBy("versionNumber DESC")
	private List<RequirementVersion> versions = new ArrayList<RequirementVersion>();
	

	protected Requirement() {
		super();
	}

	/**
	 * Creates a new requirement which "latest version" is the given {@link RequirementVersion}
	 * 
	 * @param version
	 */
	public Requirement(@NotNull RequirementVersion version) {
		resource = version;
		addVersion(version);
	}

	private void addVersion(RequirementVersion version) {
		versions.add(version);
		resource.setRequirement(this);
	}

	@Override
	public void setName(String name) {
		resource.setName(name);
	}

	@Override
	public void setDescription(String description) {
		resource.setDescription(description);
	}

	@Override
	public void accept(RequirementLibraryNodeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public AttachmentList getAttachmentList() {
		return resource.getAttachmentList();
	}

	/***
	 * @return the reference of the requirement
	 */
	public String getReference() {
		return resource.getReference();
	}

	/***
	 * Set the requirement reference
	 * 
	 * @param reference
	 */
	public void setReference(String reference) {
		resource.setReference(reference);
	}

	/**
	 * Creates a copy usable in a copy / paste operation. The copy is associated to no version, it should be done by the
	 * caller (the latest version might not be eligible for copy / paste).
	 */
	@Override
	public Requirement createPastableCopy() {
		RequirementVersion latestVersionCopy = getLatestVersion().createPastableCopy();
		Requirement copy = new Requirement(latestVersionCopy);
		
		for (RequirementVersion sourceVersion : this.versions) {
			if (isNotLatestVersion(sourceVersion) && sourceVersion.isNotObsolete()) {
				RequirementVersion copyVersion = sourceVersion.createPastableCopy();
				copy.addVersion(copyVersion);
			}
		}

		copy.notifyAssociatedWithProject(this.getProject());
		return copy;
	}

	private boolean isNotLatestVersion(RequirementVersion sourceVersion) {
		return !getLatestVersion().equals(sourceVersion);
	}

	/***
	 * @return the requirement criticality
	 */
	public RequirementCriticality getCriticality() {
		return resource.getCriticality();
	}

	/***
	 * Set the requirement criticality
	 * 
	 * @param criticality
	 */
	public void setCriticality(RequirementCriticality criticality) {
		resource.setCriticality(criticality);
	}

	public void setStatus(RequirementStatus status) {
		resource.setStatus(status);
	}

	public RequirementStatus getStatus() {
		return resource.getStatus();
	}

	/**
	 * 
	 * @return <code>true</code> if this requirement can be (un)linked by new verifying testcases
	 */
	public boolean isLinkable() {
		return getStatus().isRequirementLinkable();
	}

	/**
	 * Tells if this requirement's "intrinsic" properties can be modified. The following are not considered as
	 * "intrinsic" properties" : {@link #verifyingTestCases} are governed by the {@link #isLinkable()} state,
	 * {@link #status} is governed by itself.
	 * 
	 * @return <code>true</code> if this requirement's properties can be modified.
	 */
	public boolean isModifiable() {
		return getStatus().isRequirementModifiable();
	}

	@Override
	public String getName() {
		return resource.getName();
	}

	@Override
	public String getDescription() {
		return resource.getDescription();
	}

	public RequirementVersion getLatestVersion() {
		return resource;
	}

	@Override
	public RequirementVersion getResource() {
		return resource;
	}
}
