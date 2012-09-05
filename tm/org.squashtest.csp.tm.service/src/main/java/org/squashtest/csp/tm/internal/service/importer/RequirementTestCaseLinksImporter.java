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
package org.squashtest.csp.tm.internal.service.importer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.squashtest.csp.tm.domain.ColumnHeaderNotFoundException;
import org.squashtest.csp.tm.domain.SheetCorruptedException;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.service.VerifyingTestCaseManagerService;
import org.squashtest.csp.tm.service.importer.ImportRequirementTestCaseLinksSummary;


@Component
public class RequirementTestCaseLinksImporter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementTestCaseLinksImporter.class);
	
	@Inject 
	private VerifyingTestCaseManagerService verifyingTestCaseManagerService;
	@Inject
	private RequirementTestCaseLinkParser parser ;
	
	/**
	 * @throws ColumnHeaderNotFoundException if one mandatory column header is not found
	 * @param excelStream
	 * @return
	 */
	public ImportRequirementTestCaseLinksSummary importLinksExcel(InputStream excelStream){
		
		ImportRequirementTestCaseLinksSummaryImpl summary = new ImportRequirementTestCaseLinksSummaryImpl();
		
		try {
			Workbook workbook = WorkbookFactory.create(excelStream);
			parseFile(workbook, summary);
			excelStream.close();
						
		} catch (InvalidFormatException e) {
			LOGGER.warn(e.getMessage());
			throw new SheetCorruptedException(e);
		} catch (IOException e) {
			LOGGER.warn(e.getMessage());
			throw new SheetCorruptedException(e);
		}
		return summary;

	}
	
	private void parseFile(Workbook workbook, ImportRequirementTestCaseLinksSummaryImpl summary) {
		Sheet sheet = workbook.getSheetAt(0);
		//process column headers
		Map<String, Integer> columnsMapping = ExcelRowReaderUtils.mapColumns(sheet);
		parser.checkColumnsMapping(columnsMapping);
		// change ids into Squash Entities and fill the summary
		Map<RequirementVersion, List<TestCase>> testCaseListByRequirementVersion = new HashMap<RequirementVersion, List<TestCase>>();
		for (int r = 1; r < sheet.getLastRowNum(); r++) {
			Row row = sheet.getRow(r);
			parser.parseRow( row, summary, columnsMapping, testCaseListByRequirementVersion);
		}
		//persist links
		verifyingTestCaseManagerService.addVerifyingTestCasesToRequirementVersions(testCaseListByRequirementVersion);
	}


	
	
}
