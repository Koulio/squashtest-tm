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
package org.squashtest.csp.tm.domain.bugtracker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;

/**
 * The purpose of this entity is to store informations about A Project's connection to a BugTracker. <br>
 * 
 * @author mpagnon
 *
 */
@Entity
@Table(name = "BUGTRACKER_PROJECT")
public class BugTrackerProject {
	@Id
	@GeneratedValue
	@Column(name = "BUGTRACKER_PROJECT_ID")
	private Long id;
	
	@Column(name = "PROJECT_NAME")
	@NotBlank
	@Size(min = 0, max = 255)
	private String projectName;
	
	@OneToOne(optional = false)
	@ForeignKey(name="FK_BugtrackerProject_Bugtracker")
	@JoinColumn(name="BUGTRACKER_ID")
	private BugTrackerEntity bugtrackerEntity;
	
	public BugTrackerProject(){
		
	}
	
	public BugTrackerProject(String projectName, BugTrackerEntity bugtrackerEntity) {
		super();
		this.projectName = projectName;
		this.bugtrackerEntity = bugtrackerEntity;
	}


	/**
	 * 
	 * @return the name of a project in the bugtracker ({@link BugTrackerProject#getBugtrackerEntity()})
	 */
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public BugTrackerEntity getBugtrackerEntity() {
		return bugtrackerEntity;
	}
	public BugTracker getBugtracker(){
		return new BugTracker(bugtrackerEntity.getUrl(), bugtrackerEntity.getKind(), bugtrackerEntity.getName(), bugtrackerEntity.isIframeFriendly());
	}
	public void setBugtrackerEntity(BugTrackerEntity bugtrackerEntity) {
		this.bugtrackerEntity = bugtrackerEntity;
	}

	public Long getId() {
		return id;
	}
	
	
	
}
