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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.audit.AuditableSupport;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.DatasetParamValue;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseNature;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.domain.testcase.TestCaseType;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.importer.ImportStatus;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.repository.CustomFieldDao;
import org.squashtest.tm.service.internal.repository.DatasetDao;
import org.squashtest.tm.service.internal.repository.DatasetParamValueDao;
import org.squashtest.tm.service.internal.repository.ParameterDao;
import org.squashtest.tm.service.testcase.CallStepManagerService;
import org.squashtest.tm.service.testcase.DatasetModificationService;
import org.squashtest.tm.service.testcase.ParameterModificationService;
import org.squashtest.tm.service.testcase.TestCaseLibraryFinderService;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;
import org.squashtest.tm.service.testcase.TestCaseModificationService;
import org.squashtest.tm.service.testcase.TestStepModificationService;

@Component
@Scope("prototype")
public class FacilityImpl implements Facility {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FacilityImpl.class); 

	@Inject
	private TestCaseLibraryFinderService finderService;
	
	@Inject
	private TestCaseLibraryNavigationService navigationService;
	
	@Inject
	private TestCaseModificationService testcaseModificationService;
	
	@Inject
	private TestStepModificationService stepModificationService;
	
	
	@Inject
	private PrivateCustomFieldValueService cufvalueService;
	
	@Inject
	private CallStepManagerService callstepService;
	
	@Inject
	private ParameterModificationService parameterService;
	
	@Inject
	private DatasetModificationService datasetService;
	
	@Inject
	private DatasetDao datasetDao;
	
	@Inject
	private DatasetParamValueDao paramvalueDao;
	
	
	@Inject
	private ParameterDao paramDao;
	
	@Inject
	private CustomFieldDao cufDao;
	
	
	
	private SimulationFacility simulator;
	
	private Model model;
	
	private Map<String, Long> cufIdByCode = new HashMap<String, Long>();

	
	public SimulationFacility getSimulator() {
		return simulator;
	}

	public void setSimulator(SimulationFacility simulator) {
		this.simulator = simulator;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}
	
	// ************************ public (and nice looking) code **************************************

	@Override
	public LogTrain createTestCase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues) {

		LogTrain train = simulator.createTestCase(target, testCase, cufValues);
		
		if (! train.hasCriticalErrors()){
			try{
				truncate(testCase, cufValues);
				doCreateTestcase(target, testCase, cufValues);
				model.setExists(target, testCase.getId());
			}
			catch(Exception ex){
				train.addEntry( new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR, new Object[]{ex.getClass().getName()}) );
				model.setNotExists(target);
				LOGGER.error("Excel import : unexpected error while importing "+target+" : ", ex);
			}
		}
		
		return train;
		
	}

	@Override
	public LogTrain updateTestCase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues) {
		
		LogTrain train = simulator.updateTestCase(target, testCase, cufValues);
		
		if (! train.hasCriticalErrors()){
			try{
				truncate(testCase, cufValues);
				doUpdateTestcase(target, testCase, cufValues);
			}
			catch(Exception ex){
				train.addEntry( new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR, new Object[]{ex.getClass().getName()}) );
				LOGGER.error("Excel import : unexpected error while updating "+target+" : ", ex);
			}
		}
		
		return train;
	}

	@Override
	public LogTrain deleteTestCase(TestCaseTarget target) {

		LogTrain train = simulator.deleteTestCase(target);
		
		if (! train.hasCriticalErrors()){
			try{
				doDeleteTestCase(target);
				model.setDeleted(target);
			}
			catch(Exception ex){
				train.addEntry( new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR, new Object[]{ex.getClass().getName()}) );
				LOGGER.error("Excel import : unexpected error while deleting "+target+" : ", ex);				
			}
		}
		
		return train;
	}

	@Override
	public LogTrain addActionStep(TestStepTarget target, ActionTestStep testStep,
			Map<String, String> cufValues) {
		
		LogTrain train = simulator.addActionStep(target, testStep, cufValues);
		
		if (! train.hasCriticalErrors()){
			try{
				truncate(testStep, cufValues);
				doAddActionStep(target, testStep, cufValues);
				model.addActionStep(target);
			}
			catch(Exception ex){
				train.addEntry( new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR, new Object[]{ex.getClass().getName()}) );
				LOGGER.error("Excel import : unexpected error while creating step "+target+" : ", ex);				
			}
		}
		
		return train;
	}

	@Override
	public LogTrain addCallStep(TestStepTarget target, CallTestStep testStep,	TestCaseTarget calledTestCase) {
		
		LogTrain train = simulator.addCallStep(target, testStep, calledTestCase);
		
		if (! train.hasCriticalErrors()){
			try{
				doAddCallStep(target, testStep, calledTestCase);
				model.addCallStep(target, calledTestCase);
			}
			catch(Exception ex){
				train.addEntry( new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR, new Object[]{ex.getClass().getName()}) );
				LOGGER.error("Excel import : unexpected error while creating step "+target+" : ", ex);				
			}
		}
		
		return train;
	}

	@Override
	public LogTrain updateActionStep(TestStepTarget target, ActionTestStep testStep,
			Map<String, String> cufValues) {
		
		LogTrain train = simulator.updateActionStep(target, testStep, cufValues);
		
		if (! train.hasCriticalErrors()){
			try{
				truncate(testStep, cufValues);
				doUpdateActionStep(target, testStep, cufValues);
			}
			catch(Exception ex){
				train.addEntry( new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR, new Object[]{ex.getClass().getName()}) );
				LOGGER.error("Excel import : unexpected error while updating step "+target+" : ", ex);				
			}
		}
		
		return train;
	}

	@Override
	public LogTrain updateCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase) {
		
		LogTrain train = simulator.updateCallStep(target, testStep, calledTestCase);
		
		if (! train.hasCriticalErrors()){
			try{
				doUpdateCallStep(target, testStep, calledTestCase);
			}
			catch(Exception ex){
				train.addEntry( new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR, new Object[]{ex.getClass().getName()}) );
				LOGGER.error("Excel import : unexpected error while updating step "+target+" : ", ex);				
			}
		}
		
		return train;
	}

	@Override
	public LogTrain deleteTestStep(TestStepTarget target) {
		
		LogTrain train = simulator.deleteTestStep(target);
		
		if (! train.hasCriticalErrors()){
			try{
				doDeleteTestStep(target);
				model.remove(target);
			}
			catch(Exception ex){
				train.addEntry( new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR, new Object[]{ex.getClass().getName()}) );
				LOGGER.error("Excel import : unexpected error while deleting step "+target+" : ", ex);				
			}
		}
		
		return train;
	}
	
	
	@Override
	public LogTrain createParameter(ParameterTarget target, Parameter param) {
		
		LogTrain train = simulator.createParameter(target, param);
		
		if (! train.hasCriticalErrors()){
			try{
				doCreateParameter(target, param);
			}
			catch(Exception ex){
				train.addEntry( new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR, new Object[]{ex.getClass().getName()}) );
				model.removeParameter(target);
				LOGGER.error("Excel import : unexpected error while adding parameter "+target+" : ", ex);			
			}
		}
		
		return train;
	}

	
	@Override
	public LogTrain updateParameter(ParameterTarget target, Parameter param) {
		
		LogTrain train = simulator.updateParameter(target, param);
		
		if (! train.hasCriticalErrors()){
			try{
				doUpdateParameter(target, param);
			}
			catch(Exception ex){
				train.addEntry( new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR, new Object[]{ex.getClass().getName()}) );				
				LOGGER.error("Excel import : unexpected error while updating parameter "+target+" : ", ex);			
			}
		}
		
		return train;
	}

	@Override
	public LogTrain deleteParameter(ParameterTarget target) {
		
		LogTrain train = simulator.deleteParameter(target);
		
		if (! train.hasCriticalErrors()){
			try{
				doDeleteParameter(target);
			}
			catch(Exception ex){
				train.addEntry( new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR, new Object[]{ex.getClass().getName()}) );
				model.addParameter(target);	// we must readd it because simulation facility (if all went well at the simulation level) would have removed it. Design flaw apparently.
				LOGGER.error("Excel import : unexpected error while deleting parameter "+target+" : ", ex);			
			}
		}
		
		return train;
	}

	@Override
	public LogTrain failsafeUpdateParameterValue(DatasetTarget dataset,	ParameterTarget param, String value) {
		
		LogTrain train = simulator.failsafeUpdateParameterValue(dataset, param, value);
		
		if (! train.hasCriticalErrors()){
			try{
				doFailsafeUpdateParameterValue(dataset, param, value);
			}
			catch(Exception ex){
				train.addEntry( new LogEntry(dataset, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR, new Object[]{ex.getClass().getName()}) );
				LOGGER.error("Excel import : unexpected error while setting parameter "+param+" in dataset "+dataset+" : ", ex);						
			}
		}
		
		return train;
	}

	@Override
	public LogTrain deleteDataset(DatasetTarget dataset) {
		
		LogTrain train = simulator.deleteDataset(dataset);
		
		if (! train.hasCriticalErrors()){
			try{
				doDeleteDataset(dataset);
			}
			catch(Exception ex){
				train.addEntry( new LogEntry(dataset, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR, new Object[]{ex.getClass().getName()}) );
				LOGGER.error("Excel import : unexpected error while deleting dataset "+dataset+" in dataset "+dataset+" : ", ex);						
			}
		}
		
		return train;	
	}
	

	
	// ************************* private (and hairy) code *********************************

	
	
	// because this time we're not toying around man, this is the real thing
	private void doCreateTestcase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues) throws Exception{
		
		Map<Long, String> acceptableCufs = toAcceptableCufs(cufValues);
		
		// backup the audit log 
		AuditableSupport metadata = saveAuditMetadata((AuditableMixin)testCase);
	
		// case 1 : this test case lies at the root of the project
		if (target.isRootTestCase()){
			Long libraryId = model.getProjectStatus(target.getProject()).getId();	// never null because the checks ensured that the project exists
			navigationService.addTestCaseToLibrary(libraryId, testCase, acceptableCufs);
		}
		// case 2 : this test case exists within a folder
		else{
			Long folderId = navigationService.mkdirs(target.getFolder());
			navigationService.addTestCaseToFolder(folderId, testCase, acceptableCufs);
		}
		
		// restore the audit log 
		restoreMetadata((AuditableMixin)testCase, metadata);
	}

	
	private void doUpdateTestcase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues) throws Exception{
		
		TestCase orig = model.get(target);
		Long origId = orig.getId();
		
		// backup the audit log 
		AuditableSupport metadata = saveAuditMetadata((AuditableMixin)testCase);
	
		
		// update the test case core attributes
		
		String newName = testCase.getName();
		if (! StringUtils.isBlank(newName) && ! orig.getName().equals(newName)){
			testcaseModificationService.rename(origId, newName);
		}
		
		String newRef = testCase.getReference();
		if (! StringUtils.isBlank(newRef) && ! orig.getReference().equals(newRef)){
			testcaseModificationService.changeReference(origId, newRef);
		}
		
		String newPrereq = testCase.getPrerequisite();
		if (! StringUtils.isBlank(newPrereq) && ! orig.getPrerequisite().equals(newPrereq)){
			testcaseModificationService.changePrerequisite(origId, newPrereq);
		}
		
		TestCaseImportance newImp = testCase.getImportance();
		if (newImp != null && ! orig.getImportance().equals(newImp)){
			testcaseModificationService.changeImportance(origId, newImp);
		}
		
		TestCaseNature newNat = testCase.getNature();
		if (newNat != null && ! orig.getNature().equals(newNat)){
			testcaseModificationService.changeNature(origId, newNat);
		}
		
		TestCaseType newType = testCase.getType();
		if (newType != null && ! orig.getType().equals(newType)){
			testcaseModificationService.changeType(origId, newType);
		}
		
		TestCaseStatus newStatus = testCase.getStatus();
		if (newStatus != null && ! orig.getStatus().equals(newStatus)){
			testcaseModificationService.changeStatus(origId, newStatus);
		}
		
		boolean newImportanceAuto = testCase.isImportanceAuto();
		if (orig.isImportanceAuto() != newImportanceAuto){
			testcaseModificationService.changeImportanceAuto(origId, newImportanceAuto);
		}
		
		// the custom field values now
		
		List<CustomFieldValue> cufs = cufvalueService.findAllCustomFieldValues(testCase);
		for (CustomFieldValue v : cufs){
			String code = v.getCustomField().getCode();
			String newValue = cufValues.get(code);
			if (! StringUtils.isBlank(newValue)){
				v.setValue(newValue);
			}
		}
		
		// restore the audit log 
		restoreMetadata((AuditableMixin)testCase, metadata);
		
	}
	
	
	private void doDeleteTestCase(TestCaseTarget target) throws Exception{
		TestCase tc = model.get(target);
		navigationService.deleteNodes(Arrays.asList(tc.getId()));
	}
	
	
	private void doAddActionStep(TestStepTarget target, ActionTestStep testStep, Map<String, String> cufValues) throws Exception{
		
		Map<Long, String> acceptableCufs = toAcceptableCufs(cufValues);
		
		// backup the audit log 
		AuditableSupport metadata = saveAuditMetadata((AuditableMixin)testStep);
	
		// add the step
		TestCase tc = model.get(target.getTestCase());
		testcaseModificationService.addActionTestStep(tc.getId(), testStep, acceptableCufs);
		
		// move it if the index was specified
		Integer index = target.getIndex();
		if (index != null && index >=0 && index < tc.getSteps().size()){
			testcaseModificationService.changeTestStepsPosition(tc.getId(), index, Arrays.asList(testStep.getId()));
		}
		
		// restore the audit log 
		restoreMetadata((AuditableMixin)testStep, metadata);
	}
	
	
	
	private void doAddCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase){
		
		// backup the audit log 
		AuditableSupport metadata = saveAuditMetadata((AuditableMixin)testStep);
	
		// add the step
		TestCase tc = model.get(target.getTestCase());
		TestCase called = model.get(calledTestCase);
		
		callstepService.addCallTestStep(tc.getId(), called.getId());
		CallTestStep created = (CallTestStep)tc.getSteps().get(tc.getSteps().size()-1);
		
		// change position if possible and required
		Integer index = target.getIndex();
		if (index != null && index >=0 && index < tc.getSteps().size()){
			testcaseModificationService.changeTestStepsPosition(tc.getId(), index, Arrays.asList(created.getId()));
		}
		
		// restore the audit log 
		restoreMetadata((AuditableMixin)created, metadata);
	}
	
	
	private void doUpdateActionStep(TestStepTarget target, ActionTestStep testStep, Map<String, String> cufValues){
		
		Map<Long, String> acceptableCufs = toAcceptableCufs(cufValues);
		
		// backup the audit log 
		AuditableSupport metadata = saveAuditMetadata((AuditableMixin)testStep);
		
		// update the step
		TestStep actualStep = model.getStep(target);		
		stepModificationService.updateTestStep(actualStep.getId(), testStep.getAction(), testStep.getExpectedResult(), acceptableCufs);
		
		// restore the audit log 
		restoreMetadata((AuditableMixin)actualStep, metadata);
	}
	
	private void doUpdateCallStep(TestStepTarget target, CallTestStep testStep, TestCaseTarget calledTestCase){

		// backup the audit log 
		AuditableSupport metadata = saveAuditMetadata((AuditableMixin)testStep);
		
		// update the step
		TestStep actualStep = model.getStep(target);		
		TestCase newCalled = model.get(calledTestCase);
		callstepService.checkForCyclicStepCallBeforePaste(newCalled.getId(), Arrays.asList(actualStep.getId()));
		((CallTestStep)actualStep).setCalledTestCase(newCalled);
		
		// restore the audit log 
		restoreMetadata((AuditableMixin)actualStep, metadata);
	}
	
	private void doDeleteTestStep(TestStepTarget target){
		TestStep actual = model.getStep(target);
		testcaseModificationService.removeStepFromTestCase(actual.getTestCase().getId(), actual.getId());
	}
	
	private void doCreateParameter(ParameterTarget target, Parameter param){

		// according to the spec this is exactly the same thing
		doUpdateParameter(target, param);
	}
	
	private void doUpdateParameter(ParameterTarget target, Parameter param){
		
		if (! model.doesParameterExists(target)){
			Long testcaseId = model.getId(target.getOwner());
			truncate(param);
			parameterService.addNewParameterToTestCase(param, testcaseId);
		}
		else{
		
			findParameter(target).setDescription(param.getDescription());
		}
		
	}
	
	private void doDeleteParameter(ParameterTarget target){
		Long testcaseId = model.getId(target.getOwner());
		List<Parameter> allparams = parameterService.findAllforTestCase(testcaseId);
		
		Parameter param=null;
		for (Parameter p : allparams){
			if (p.getName().equals(target.getName())){
				param = p;
				break;				
			}
		}		
		
		parameterService.remove(param);
	}
	
	
	private void doFailsafeUpdateParameterValue(DatasetTarget dataset, ParameterTarget param, String value){
		DatasetParamValue dpv = findParamValue(dataset, param);
		String trValue = truncate(value, 255);
		dpv.setParamValue(trValue);
	}
	
	private void doDeleteDataset(DatasetTarget dataset){
		Dataset ds = findDataset(dataset);
		datasetService.remove(ds);
	}
	
	// ******************************** support methods ***********************
	
	
	private Parameter findParameter(ParameterTarget param){
		Long testcaseId = model.getId(param.getOwner());

		Parameter found = paramDao.findParameterByNameAndTestCase(param.getName(), testcaseId);
		
		if (found != null){
			return found;
		}
		else{
			throw new NoSuchElementException("parameter "+param+" could not be found");
		}
	}

	private Dataset findDataset(DatasetTarget dataset){
		Long tcid = model.getId(dataset.getTestCase());
		
		Dataset found = datasetDao.findDatasetByTestCaseAndByName(tcid, dataset.getName());
		
		if (found == null){
			return found;
		}
		else{
			Dataset newds = new Dataset();
			newds.setName(dataset.getName());
			truncate(newds);
			datasetService.persist(newds, tcid);
			return newds;
		}
	}

	private DatasetParamValue findParamValue(DatasetTarget dataset, ParameterTarget param){
		
		Dataset dbDs = findDataset(dataset);
		Parameter dsParam = findParameter(param);
		
		for (DatasetParamValue dpv : dbDs.getParameterValues()){
			if (dpv.getParameter().equals(dsParam)){
				return dpv;
			}
		}
		
		// else we have to create it. Note that the services do not provide any facility for that 
		// so we have to do it from scratch here. Tsss, lazy conception again.
		DatasetParamValue dpv = new DatasetParamValue(dsParam, dbDs);
		paramvalueDao.persist(dpv);
		dbDs.addParameterValue(dpv);
		
		return dpv;
	}
	
	
	// because the service identifies cufs by their id, not their code
	// also populates the cache (cufIdByCode)
	private Map<Long, String> toAcceptableCufs(Map<String, String> origCufs){
		
		Map<Long, String> result = new HashMap<Long, String>(origCufs.size());
		
		for (Entry<String, String> origCuf : origCufs.entrySet()){
			String cufCode = origCuf.getKey();
			
			if (! cufIdByCode.containsKey(cufCode)){
				
				CustomField customField = cufDao.findByCode(cufCode);

				// that bit of code checks that if the custom field doesn't exist, the hashmap entry contains
				// a dummy value for this code.
				Long id = null;
				if (customField != null){
					id = customField.getId();
				}
	
				cufIdByCode.put(cufCode, id);
			}
			
			// now add to our map the id of the custom field, except if null : the custom field 
			// does not exist and therefore wont be included.
			Long cufId = cufIdByCode.get(cufCode);
			if (cufId != null){
				result.put(cufId, origCuf.getValue());
			}
		}
		
		return result;
		
	}
	
	

	private AuditableSupport saveAuditMetadata(AuditableMixin mixin){
		AuditableSupport support = new AuditableSupport();
		support.setCreatedBy ( mixin.getCreatedBy() );
		support.setLastModifiedBy ( mixin.getLastModifiedBy() );
		support.setCreatedOn ( mixin. getCreatedOn() );
		support.setLastModifiedOn ( mixin.getLastModifiedOn() );
		return support;
	}
	
	private void truncate(TestCase testCase, Map<String, String> cufValues){
		String name = testCase.getName();
		if (name != null){
			testCase.setName( truncate(name, 255) );
		}
		String ref = testCase.getReference();
		if (ref != null){
			testCase.setReference( truncate(ref, 50) );
		}
		
		for (Entry<String, String> cuf : cufValues.entrySet()){
			String value = cuf.getValue();
			cuf.setValue( truncate(value, 255));
		}
	}
	
	private void truncate(ActionTestStep step, Map<String, String> cufValues){
		for (Entry<String, String> cuf : cufValues.entrySet()){
			String value = cuf.getValue();
			cuf.setValue( truncate(value, 255));
		}	
	}
	
	private void truncate(Parameter param){
		String name = param.getName();
		if (name != null){
			param.setName( truncate(name, 255));
		}
	}
	
	private void truncate(Dataset ds){
		String name = ds.getName();
		if (name != null){
			ds.setName ( truncate (name, 255));
		}
	}
	
	private String truncate(String str, int cap){
		return str.substring(0, Math.min(str.length(), cap));
	}
	
	private void restoreMetadata(AuditableMixin mixin, AuditableSupport saved){
		if (!StringUtils.isBlank(saved.getCreatedBy())){
			mixin.setCreatedBy(saved.getCreatedBy());
		}
		
		if (!StringUtils.isBlank(saved.getLastModifiedBy())){
			mixin.setLastModifiedBy(saved.getLastModifiedBy());
		}
		
		if (saved.getCreatedOn() != null){
			mixin.setCreatedOn(saved.getCreatedOn());
		}
		
		if (saved.getLastModifiedOn() != null){
			mixin.setLastModifiedOn(saved.getLastModifiedOn());
		}
	}


}
