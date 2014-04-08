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

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseNature;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.domain.testcase.TestCaseType;
import org.squashtest.tm.service.importer.ImportMode;
import org.squashtest.tm.service.internal.batchimport.excel.CellValueCoercer;
import org.squashtest.tm.service.internal.batchimport.excel.OptionalBooleanCellCoercer;
import org.squashtest.tm.service.internal.batchimport.excel.OptionalDateCellCoercer;
import org.squashtest.tm.service.internal.batchimport.excel.OptionalEnumCellCoercer;
import org.squashtest.tm.service.internal.batchimport.excel.OptionalIntegerCellCoercer;
import org.squashtest.tm.service.internal.batchimport.excel.OptionalOneBasedIndexCellCoercer;
import org.squashtest.tm.service.internal.batchimport.excel.StringCellCoercer;

/**
 * Repository of {@link CellValueCoercer} for a given {@link TemplateColumn}s
 * 
 * @author Gregory Fouquet
 * 
 */
@Component
public final class CellValueCoercerRepository<COL extends Enum<COL> & TemplateColumn> {
	private static final Map<TemplateWorksheet, CellValueCoercerRepository<?>> coercerRepoByWorksheet = new HashMap<TemplateWorksheet, CellValueCoercerRepository<?>>(
			TemplateWorksheet.values().length);

	static {
		coercerRepoByWorksheet.put(TemplateWorksheet.TEST_CASES_SHEET, createTestCasesSheetRepo());
		coercerRepoByWorksheet.put(TemplateWorksheet.STEPS_SHEET, createStepsSheetRepo());
		coercerRepoByWorksheet.put(TemplateWorksheet.PARAMETERS_SHEET, createParamsSheetRepo());
	}

	/**
	 * Returns the repository suitable for the given worksheet.
	 * 
	 * @param worksheet
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <C extends Enum<C> & TemplateColumn> CellValueCoercerRepository<C> forWorksheet(
			@NotNull TemplateWorksheet worksheet) {
		return (CellValueCoercerRepository<C>) coercerRepoByWorksheet.get(worksheet);
	}

	/**
	 * @return
	 */
	private static CellValueCoercerRepository<ParameterSheetColumn> createParamsSheetRepo() {
		CellValueCoercerRepository<ParameterSheetColumn> repo = new CellValueCoercerRepository<ParameterSheetColumn>();

		repo.coercerByColumn.put(ParameterSheetColumn.ACTION, OptionalEnumCellCoercer.forEnum(ImportMode.class));

		return repo;
	}

	/**
	 * @return a {@link CellValueCoercerRepository} suitable for the steps worksheet.
	 */
	private static CellValueCoercerRepository<StepSheetColumn> createStepsSheetRepo() {
		CellValueCoercerRepository<StepSheetColumn> repo = new CellValueCoercerRepository<StepSheetColumn>();

		repo.coercerByColumn.put(StepSheetColumn.ACTION, OptionalEnumCellCoercer.forEnum(ImportMode.class));
		repo.coercerByColumn.put(StepSheetColumn.TC_STEP_NUM, OptionalOneBasedIndexCellCoercer.INSTANCE);
		repo.coercerByColumn.put(StepSheetColumn.TC_STEP_IS_CALL_STEP, OptionalBooleanCellCoercer.INSTANCE);

		return repo;
	}

	/**
	 * @return a {@link CellValueCoercerRepository} suitable for the test cases worksheet.
	 */
	private static CellValueCoercerRepository<TestCaseSheetColumn> createTestCasesSheetRepo() {
		CellValueCoercerRepository<TestCaseSheetColumn> repo = new CellValueCoercerRepository<TestCaseSheetColumn>();

		repo.coercerByColumn.put(TestCaseSheetColumn.TC_NUM, OptionalIntegerCellCoercer.INSTANCE);
		repo.coercerByColumn.put(TestCaseSheetColumn.TC_WEIGHT_AUTO, OptionalBooleanCellCoercer.INSTANCE);
		repo.coercerByColumn.put(TestCaseSheetColumn.TC_WEIGHT,
				OptionalEnumCellCoercer.forEnum(TestCaseImportance.class));
		repo.coercerByColumn.put(TestCaseSheetColumn.TC_NATURE, OptionalEnumCellCoercer.forEnum(TestCaseNature.class));
		repo.coercerByColumn.put(TestCaseSheetColumn.TC_TYPE, OptionalEnumCellCoercer.forEnum(TestCaseType.class));
		repo.coercerByColumn.put(TestCaseSheetColumn.TC_STATUS, OptionalEnumCellCoercer.forEnum(TestCaseStatus.class));
		repo.coercerByColumn.put(TestCaseSheetColumn.TC_CREATED_ON, OptionalDateCellCoercer.INSTANCE);

		repo.coercerByColumn.put(TestCaseSheetColumn.ACTION, OptionalEnumCellCoercer.forEnum(ImportMode.class));

		return repo;
	}

	/**
	 * The default coercer that shall be given when no other is defined.
	 */
	private static final CellValueCoercer<String> DEFAULT_COERCER = StringCellCoercer.INSTANCE;

	private Map<COL, CellValueCoercer<?>> coercerByColumn = new HashMap<COL, CellValueCoercer<?>>();

	private CellValueCoercerRepository() {
		super();
	}

	/**
	 * Finds a coercer for the given column. When no coercer is available, returns the default coercer
	 * 
	 * @param col
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <VAL> CellValueCoercer<VAL> findCoercer(COL col) {
		CellValueCoercer<?> coercer = (CellValueCoercer<VAL>) coercerByColumn.get(col);
		return (CellValueCoercer<VAL>) (coercer == null ? DEFAULT_COERCER : coercer);
	}

	/**
	 * @return the coercer suitable for custom field cells.
	 */
	public CellValueCoercer<String> findCustomFieldCoercer() {
		return StringCellCoercer.INSTANCE;
	}
}
