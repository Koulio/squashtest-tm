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
package squashtm.testautomation.jenkins.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;

import squashtm.testautomation.domain.TestAutomationProject;
import squashtm.testautomation.jenkins.beans.BuildList;
import squashtm.testautomation.jenkins.beans.ItemList;
import squashtm.testautomation.jenkins.beans.Job;
import squashtm.testautomation.jenkins.beans.JobList;
import squashtm.testautomation.spi.exceptions.TestAutomationException;
import squashtm.testautomation.spi.exceptions.UnreadableResponseException;



@Component
public class JsonParser {
	
	private static final String DISABLED_COLOR_STRING = "disabled";

	private ObjectMapper objMapper = new ObjectMapper();
	
	
	
	public Collection<TestAutomationProject> readJobListFromJson(String json){
		
		try {
			
			JobList list = objMapper.readValue(json, JobList.class);
			
			JobList filteredList = filterDisabledJobs(list);
			
			Collection<TestAutomationProject> projectsList = toProjectList(filteredList);
			
			return projectsList;
			
		} 
		catch (JsonParseException e) {
			throw new UnreadableResponseException(e);
		} 
		catch (JsonMappingException e) {
			throw new UnreadableResponseException(e);
		} 
		catch (IOException e) {
			throw new UnreadableResponseException(e);
		} 
		
	}
	
	
	public ItemList getQueuedListFromJson(String json){
		return safeReadValue(json, ItemList.class);
	}
	
	public BuildList getRunningBuildsFromJson(String json){
		return safeReadValue(json, BuildList.class);
	}
	
	
	public String toJson(Object object){
		try {
			return objMapper.writeValueAsString(object);
		} 
		catch (JsonGenerationException e) {
			throw new TestAutomationException("TestAutomationConnector : internal error, could not generate json", e);
		} 
		catch (JsonMappingException e) {
			throw new TestAutomationException("TestAutomationConnector : internal error, could not generate json", e);
		} 
		catch (IOException e) {
			throw new TestAutomationException("TestAutomationConnector : internal error, could not generate json", e);
		}
	}
	
	protected <R> R safeReadValue(String json, Class<R> clazz){
	
		try {
			return objMapper.readValue(json, clazz);
		} 
		catch (JsonParseException e) {
			throw new UnreadableResponseException("TestAutomationConnector : the response from the server couldn't be treated", e);
		} 
		catch (JsonMappingException e) {
			throw new UnreadableResponseException("TestAutomationConnector : the response from the server couldn't be treated", e);
		} 
		catch (IOException e) {
			throw new UnreadableResponseException("TestAutomationConnector : internal error :", e);
		}

	}
	
	
	protected JobList filterDisabledJobs(JobList fullList){
		JobList newJobList = new JobList();
		for (Job job : fullList.getJobs()){
			if (! job.getColor().equals(DISABLED_COLOR_STRING)){
				newJobList.getJobs().add(job);
			}
		}
		return newJobList;
	}
	
	
	protected Collection<TestAutomationProject> toProjectList(JobList jobList){
		
		Collection<TestAutomationProject> projects = new ArrayList<TestAutomationProject>();
		
		for (Job job : jobList.getJobs()){
			projects.add(new TestAutomationProject(job.getName(), null));
		}
		
		return projects;
		
		
	}

	
}
