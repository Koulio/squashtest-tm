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
import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.service.internal.batchexport.ExportModel.TestCaseModel;
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
		
		
		while (idx < testCaseIds.size()){
			
			ids = testCaseIds.subList(idx, max);
			
			ExportModel model = exportDao.findModel(ids); 
			addPaths(testCaseIds, model);
			
			exporter.appendToWorkbook(model);
			
			idx = idx+20;
			max = Math.min(idx+20, testCaseIds.size());
		}
		
		return exporter.print();
		
	}
	
	
	private void addPaths(List<Long> ids, ExportModel models){
		
		List<String> paths = nodeDao.getPathsAsString(ids);
		
		// add the path to the test cases 
		for (TestCaseModel model : models.getTestCases()){
			Long id = model.getId();
			int index = ids.indexOf(id);
			String path = paths.get(index);
			model.setPath(path);
		}
		
	}
	

}
