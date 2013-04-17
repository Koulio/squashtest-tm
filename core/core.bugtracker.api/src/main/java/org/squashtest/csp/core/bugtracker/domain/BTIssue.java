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
package org.squashtest.csp.core.bugtracker.domain;

import java.util.Date;

import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.bugtracker.definition.RemoteProject;

/**
 * Bug-tracker-agnostic representation of an issue / ticket
 *
 * @author Gregory Fouquet
 *
 */

public class BTIssue implements RemoteIssue {
	
	private String id;
	
	private String summary;
	
	private BTProject project;
	
	private Priority priority;
	
	private Version version;
	
	private User reporter;
	
	private Category category;
	
	private User assignee;
		
	private String description;
	
	private String comment;
		
	private Date createdOn;
	
	private Status status;
	
	private String bugtracker;

	public BTIssue(){
		
	}

	public BTIssue(String id, String summary){
		this.id=id;
		this.summary=summary;
	}
	

	public String getId(){
		return id;
	}
	
	public void setId(String id){
		this.id=id;
	}
	

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}


	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public BTProject getProject() {
		return project;
	}

	public void setProject(BTProject project) {
		this.project = project;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public Version getVersion() {
		return version;
	}

	public void setVersion(Version version) {
		this.version = version;
	}

	public User getReporter() {
		return reporter;
	}

	public void setReporter(User reporter) {
		this.reporter = reporter;
	}

	public User getAssignee() {
		return assignee;
	}

	public void setAssignee(User assignee) {
		this.assignee = assignee;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}


	public Date getCreatedOn() {
		return createdOn;
	}


	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	
	public Status getStatus(){
		return status;
	}
	
	public void setStatus(Status status){
		this.status=status;
	}
	
	/**
	 * sets the name of the instance of the bugtracker (not its kind, url or else)
	 * 
	 * @param btName
	 */
	public void setBugtracker(String btName){
		this.bugtracker = btName;
	}
	
	public String getBugtracker(){
		return bugtracker;
	}
	

	
	/** exists for the purpose of being java-bean compliant */
	public void setDummy(Boolean dummy){
		
	}

	@Override
	public boolean hasBlankId(){
		return (
			(id==null) ||
			(id.length()==0) ||
			(id.matches("^\\s*$"))
		);
	}
}
