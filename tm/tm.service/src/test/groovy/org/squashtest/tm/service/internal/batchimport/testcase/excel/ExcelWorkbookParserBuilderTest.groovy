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

package org.squashtest.tm.service.internal.batchimport.testcase.excel;


import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import spock.lang.Specification;

import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateWorksheet.*;

/**
 * @author Gregory Fouquet
 *
 */
class ExcelWorkbookParserBuilderTest extends Specification {

	def "should create metadata"() {
		given:
		def builder = new ExcelWorkbookParserBuilder()

		and:
		Resource xls = new ClassPathResource("batchimport/testcase/import-2269.xlsx")
		println xls.file.absolutePath
		InputStream is = new BufferedInputStream(new FileInputStream(xls.file))
		println is

		when:
		def wb = builder.openWorkbook(is);
		WorkbookMetaData wmd = builder.buildMetaData(wb)
		
		then:
		wmd.worksheetDefs[TEST_CASES_SHEET]
		wmd.worksheetDefs[TEST_CASES_SHEET].stdColumnDefs.values()*.type as Set == TestCaseSheetColumn.values() as Set
		wmd.worksheetDefs[TEST_CASES_SHEET].customFieldDefs*.code as Set == ["<CODE1>", "<CODE2>"] as Set
		wmd.worksheetDefs[STEPS_SHEET]
		wmd.worksheetDefs[STEPS_SHEET].stdColumnDefs.values()*.type as Set == StepSheetColumn.values() as Set
		wmd.worksheetDefs[STEPS_SHEET].customFieldDefs*.code == ["<CODE>"]
		wmd.worksheetDefs[PARAMETERS_SHEET]
		wmd.worksheetDefs[PARAMETERS_SHEET].stdColumnDefs.values()*.type as Set == ParameterSheetColumn.values() as Set
		wmd.worksheetDefs[DATASETS_SHEET]
		wmd.worksheetDefs[DATASETS_SHEET].stdColumnDefs.values()*.type as Set == DatasetSheetColumn.values() as Set

		cleanup:
		IOUtils.closeQuietly(is);
	}
}
