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
package squashtm.testautomation.jenkins.beans;

public class Item {

	private Action[] actions;
	
	private Task task;
	
	int id;


	public Action[] getActions() {
		return actions;
	}

	public void setActions(Action[] actions) {
		this.actions = actions;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	
	public boolean representsProjectWithExtId(String projectName, String externalId){
		return (	
				projectName.equals(getProjectName())
			&&	hasExternalId(externalId)
		);
	}
	
	public boolean representsProjectWithId(String projectName, int id){
		return (	
				projectName.equals(getProjectName())
			&&	(getId() == id)
		);		
	}
	
	private boolean hasExternalId(String externalId){
		Parameter extIdParam = Parameter.newExtIdParameter(externalId);
		for (Action action : actions){
			if (action.hasParameter(extIdParam)){
				return true;
			}
		}
		return false;
	}
	
	public String getProjectName(){
		return task.getName();
	}
	
}
