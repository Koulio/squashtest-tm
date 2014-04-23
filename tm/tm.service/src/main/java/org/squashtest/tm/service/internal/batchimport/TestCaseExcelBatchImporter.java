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
package org.squashtest.tm.service.internal.batchimport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.stereotype.Component;
import org.squashtest.tm.service.importer.EntityType;
import org.squashtest.tm.service.importer.ImportLog;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.ExcelWorkbookParser;

@Component
public class TestCaseExcelBatchImporter {

	@Inject
	private Provider<SimulationFacility> simulatorProvider;

	@Inject
	private Provider<FacilityImpl> facilityImplProvider;

	@Inject
	private Provider<Model> modelProvider;

	public ImportLog simulateImport(File excelFile) {

		SimulationFacility simulator = simulatorProvider.get();

		Model model = modelProvider.get();
		simulator.setModel(model);

		ExcelWorkbookParser parser = ExcelWorkbookParser.createParser(excelFile);
		parser.parse().releaseResources();
		List<Instruction<?>> instructions = buildOrderedInstruction(parser);

		return run(instructions, simulator);
	}

	public ImportLog performImport(File excelFile) {

		SimulationFacility simulator = simulatorProvider.get();
		FacilityImpl impl = facilityImplProvider.get();

		Model model = modelProvider.get();
		simulator.setModel(model);
		impl.setModel(model);
		impl.setSimulator(simulator);

		ExcelWorkbookParser parser = ExcelWorkbookParser.createParser(excelFile);
		parser.parse().releaseResources();
		List<Instruction<?>> instructions = buildOrderedInstruction(parser);

		ImportLog importLog = run(instructions, impl);

		impl.postprocess();

		return importLog;

	}

	private List<Instruction<?>> buildOrderedInstruction(ExcelWorkbookParser parser) {
		List<Instruction<?>> instructions = new ArrayList<Instruction<?>>();
		for (EntityType entity :  Facility.ENTITIES_ORDERED_BY_INSTRUCTION_ORDER) {
			List<Instruction<?>> entityInstructions = findInstructionsByEntity(parser,entity);
			instructions.addAll(entityInstructions);
		}
		return instructions;
	}

	private List<Instruction<?>> findInstructionsByEntity(ExcelWorkbookParser parser, EntityType entityType) {
		List<Instruction<?>> instructions = new ArrayList<Instruction<?>>();
		;
		switch (entityType) {
		case TEST_CASE:
			instructions.addAll(parser.getTestCaseInstructions());
			break;
		case TEST_STEP:
			instructions.addAll(parser.getTestStepInstructions());
			break;
		case PARAMETER:
			instructions.addAll(parser.getParameterInstructions());
			break;
		case DATASET:
			instructions.addAll(parser.getDatasetInstructions());
			break;
		default:

		}
		return instructions;
	}

	private ImportLog run(List<Instruction<?>> instructions, Facility facility) {

		ImportLog importLog = new ImportLog();

		for (Instruction<?> instruction : instructions) {
			LogTrain logs = instruction.execute(facility);

			logs.setForAll(instruction.getMode());
			logs.setForAll(instruction.getLine());

			importLog.appendLogTrain(logs);
		}

		return importLog;
	}

}
