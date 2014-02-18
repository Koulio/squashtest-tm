/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.internal.batchexport;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.service.internal.batchexport.ExportModel.TestCaseModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.TestStepModel;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;

@Service
public class TestCaseExcelExporterService {

	@Inject
	private ExportDao exportDao;
	
	
	@Inject
	@Qualifier("squashtest.tm.repository.TestCaseLibraryNodeDao")
	private LibraryNodeDao<TestCaseLibraryNode> nodeDao;

	
	public File exportAsExcel(List<Long> testCaseIds){
		
		// let's chunk the job by batches of 20 test cases 
		List<Long> ids;
		int idx=0;
		int max = Math.min(idx+20, testCaseIds.size());
		ExcelExporter exporter = new ExcelExporter();
		
		Map<Long, String> pathById = new HashMap<Long, String>(testCaseIds.size());
		populatePathsCache(pathById, testCaseIds);
		
		while (idx < testCaseIds.size()){
			
			ids = testCaseIds.subList(idx, max);
			
			ExportModel model = exportDao.findModel(ids); 
			addPaths(pathById, model);
			sort(model);
			
			exporter.appendToWorkbook(model);
			
			idx = idx+20;
			max = Math.min(idx+20, testCaseIds.size());
		}
		
		return exporter.print();
		
	}
	
	
	private void populatePathsCache(Map<Long, String> pathById, List<Long> ids){
		
		List<String> paths = nodeDao.getPathsAsString(ids);
		
		for (int i=0; i< ids.size(); i++){
			pathById.put(ids.get(i), paths.get(i));
		}
		
	}
	
	private void addPaths(Map<Long, String> pathById, ExportModel models){

		addPathsForTestCase(pathById, models);
		addPathsForTestSteps(pathById, models);

	}
	
	private void addPathsForTestCase(Map<Long, String> pathById, ExportModel models){
		
		for (TestCaseModel model : models.getTestCases()){
			Long id = model.getId();
			String path = pathById.get(id);
			model.setPath(path);
		}
		
	}
	
	private void addPathsForTestSteps(Map<Long, String> pathById, ExportModel models){
		
		
		List<TestStepModel> callsteps = new LinkedList<TestStepModel>();
		List<Long> calledTC = new LinkedList<Long>();
		
		for (TestStepModel model : models.getTestSteps()){
			
			// add the path to the owner id
			Long id = model.getTcOwnerId();
			String path = pathById.get(id);
			model.setTcOwnerPath(path);
			
			// if it is a call step, treat the path of the called test case or save the reference for a second round
			if (model.getIsCallStep()>0){
				Long callid = Long.valueOf(model.getAction());
				if (pathById.containsKey(callid)){
					String callaction = "CALL "+pathById.get(callid);
					model.setAction(callaction);
				}
				else{
					callsteps.add(model);
					calledTC.add(callid);
				}
			}
		}
		
		// if some call steps were left unresolved, let's do them.
		if (! calledTC.isEmpty()){
			populatePathsCache(pathById, calledTC);
			for (TestStepModel model : callsteps){
				Long callid = Long.valueOf(model.getAction());
				String callaction = "CALL "+pathById.get(callid);
				model.setAction(callaction);
			}			
		}
	}


	private void sort(ExportModel models){
		Collections.sort(models.getTestCases());
		Collections.sort(models.getTestSteps());
	}
	


}
