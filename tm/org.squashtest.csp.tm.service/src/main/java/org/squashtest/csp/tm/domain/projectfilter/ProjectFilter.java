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
package org.squashtest.csp.tm.domain.projectfilter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.squashtest.csp.tm.domain.project.Project;

@Entity
public class ProjectFilter {
	@Id
	@GeneratedValue
	@Column(name = "PROJECT_FILTER_ID")
	private Long id;

	private String userLogin;

	private Boolean activated;

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "PROJECT_FILTER_ENTRY", joinColumns = @JoinColumn(name = "FILTER_ID"), inverseJoinColumns = @JoinColumn(name = "PROJECT_ID"))
	private final List<Project> projects = new ArrayList<Project>();

	public void setProjects(List<Project> newProjectList) {
		this.projects.clear();
		this.projects.addAll(newProjectList);
	}

	public List<Project> getProjects() {
		return projects;
	}

	public Long getId() {
		return id;
	}

	public String getUserLogin() {
		return userLogin;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	public Boolean getActivated() {
		return activated;
	}

	public void setActivated(Boolean activated) {
		this.activated = activated;
	}

	/**
	 * returns true if the project is listed, false otherwhise.
	 * 
	 * @param project
	 * @return
	 */
	public boolean isProjectSelected(Project project) {
		boolean result = false;
		for (Project localProject : projects) {
			if (project.getId().equals(localProject.getId())) {
				result = true;
			}
		}
		return result;
	}

	public void removeProject(Project project) {
		Iterator<Project> projectIterator = this.projects.iterator();
		while (projectIterator.hasNext()) {
			Project projectItem = projectIterator.next();
			if (projectItem.getId() == project.getId()) {
				projectIterator.remove();
			}
		}
	}

}
