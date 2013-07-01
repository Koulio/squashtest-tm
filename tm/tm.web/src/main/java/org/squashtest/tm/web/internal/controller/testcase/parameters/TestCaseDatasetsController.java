/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.testcase.parameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.SinglePageCollectionHolder;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.DatasetParamValue;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.DomainException;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testcase.DatasetModificationService;
import org.squashtest.tm.service.testcase.ParameterFinder;
import org.squashtest.tm.service.testcase.TestCaseFinder;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.testcase.parameters.TestCaseParametersController.ParameterNameComparator;
import org.squashtest.tm.web.internal.controller.widget.AoColumnDef;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

/**
 * Controller to handle requests for datasets of a given test case.
 * 
 * @author mpagnon
 * 
 */
@RequestMapping("/test-cases/{testCaseId}/datasets")
@Controller
public class TestCaseDatasetsController {
	@Inject
	private TestCaseFinder testCaseFinder;
	@Inject
	private DatasetModificationService datasetModificationService;
	@Inject
	private ParameterFinder parameterFinder;
	@Inject
	private PermissionEvaluationService permissionEvaluationService;
	@Inject
	private MessageSource messageSource;
	

	private DatatableMapper<String> datasetsTableMapper = new NameBasedMapper(3)
															.mapAttribute(Dataset.class, "id",	String.class, DataTableModelHelper.DEFAULT_ENTITY_ID_KEY)
															.mapAttribute(Dataset.class, "name", String.class,DataTableModelHelper.NAME_KEY);


	/**
	 * Return the datas to fill the datasets table in the test case view
	 * 
	 * @param testCaseId
	 *            : the id of the viewed test case
	 * @param params
	 *            : DataTable draw parameters
	 * @param locale
	 *            : the browser's locale
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getDatasetsTableDatas(@PathVariable long testCaseId, final DataTableDrawParameters params,
			final Locale locale) {
		List<Dataset> datasetsList = getSortedDatasets(testCaseId, params);
		PagedCollectionHolder<List<Dataset>> holder = new SinglePageCollectionHolder<List<Dataset>>(datasetsList);
		return new DatasetsDataTableModelHelper().buildDataModel(holder, params.getsEcho());
	}

	/**
	 * @see DatasetsTableColumnDefHelper#getAoColumnDefs(List, boolean);
	 * @param testCaseId
	 *            : the id of the viewed test case
	 * @param locale
	 *            : the browser's locale
	 * @return
	 */
	@RequestMapping(value = "/table/aoColumnDef", method = RequestMethod.GET)
	@ResponseBody
	public List<AoColumnDef> getDatasetsTableColumnDefs(@PathVariable long testCaseId, final Locale locale) {
		TestCase testCase = testCaseFinder.findById(testCaseId);
		List<Parameter> directAndCalledParameters = getSortedDirectAndCalledParameters(testCaseId);
		List<Long> paramIds = IdentifiedUtil.extractIds(directAndCalledParameters);
		boolean editable = permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", testCase);
		return new DatasetsTableColumnDefHelper().getAoColumnDefs(paramIds, editable);

	}

	/**
	 * Return the list of parameters headers for the dataset table in the test case view.
	 * 
	 * @param testCaseId
	 *            : the viewed test case
	 * @param locale
	 * @return
	 */
	@RequestMapping(value = "/table/param-headers", method = RequestMethod.GET)
	@ResponseBody
	public List<String> getDatasetsTableParametersHeaders(@PathVariable long testCaseId, final Locale locale) {
		List<Parameter> directAndCalledParameters = getSortedDirectAndCalledParameters(testCaseId);
		return findDatasetParamHeaders(testCaseId, locale, directAndCalledParameters, messageSource);

	}

	private List<Parameter> getSortedDirectAndCalledParameters(long testCaseId) {
		List<Parameter> directAndCalledParameters = parameterFinder.findAllforTestCase(testCaseId);
		Collections.sort(directAndCalledParameters, new TestCaseParametersController.ParameterNameComparator(SortOrder.ASCENDING));
		return directAndCalledParameters;
	}
	
	/**
	 * Returns the list of column headers names for parameters in the Datasets table orderd by parameter name.
	 * 
	 * 
	 * @param testCaseId : the concerned test case id
	 * @param locale : the browser's locale
	 * @param directAndCalledParameters : the list of parameters directly associated or associated through call steps
	 * @param messageSource : the message source to internationalize suffix 
	 * @return
	 */
	public static List<String> findDatasetParamHeaders(long testCaseId, final Locale locale,
		List<Parameter> directAndCalledParameters, MessageSource messageSource) {
		Collections.sort(directAndCalledParameters, new ParameterNameComparator(SortOrder.ASCENDING));
		List<String> result = new ArrayList<String>(directAndCalledParameters.size());
		for(Parameter param : directAndCalledParameters){
			result.add(ParametersDataTableModelHelper.buildParameterName(param, testCaseId, messageSource, locale));
		}
		return result;
	}
	
	/**
	 * Returns the list of column headers names for parameters in the Datasets table mapped with the parameter id.
	 * @param testCaseId : the concerned test case id
	 * @param locale : the browser's locale
	 * @param directAndCalledParameters : the list of parameters directly associated or associated through call steps
	 * @param messageSource : the message source to internationalize suffix 
	 * @return
	 */
	public static Map<String, String> findDatasetParamHeadersByParamId(long testCaseId, final Locale locale,
			List<Parameter> directAndCalledParameters, MessageSource messageSource) {
	
		Map<String, String> result = new HashMap<String, String>(directAndCalledParameters.size());
			for(Parameter param : directAndCalledParameters){
				result.put(param.getId().toString(), ParametersDataTableModelHelper.buildParameterName(param, testCaseId, messageSource, locale));
			}
			return result;
		}
	private List<Dataset> getSortedDatasets(long testCaseId, final DataTableDrawParameters params) {
		final TestCase testCase = testCaseFinder.findById(testCaseId);
		Sorting sorting = new DataTableMapperPagingAndSortingAdapter(params, datasetsTableMapper);
		Set<Dataset> datasets = testCase.getDatasets();
		List<Dataset> datasetsList = new ArrayList<Dataset>(datasets);
		if (sorting.getSortedAttribute() != null && sorting.getSortedAttribute().equals("Parameter.name")) {
			Collections.sort(datasetsList, new DatasetNameComparator(sorting.getSortOrder()));
		} else {
			Collections.sort(datasetsList, new DatasetNameComparator(SortOrder.ASCENDING));
		}
		return datasetsList;
	}

	/**
	 * Will compare {@link Dataset} on their name in the given {@link SortOrder}
	 * 
	 * @author mpagnon
	 * 
	 */
	@SuppressWarnings("serial")
	private static final class DatasetNameComparator implements Comparator<Dataset>, Serializable {

		private SortOrder sortOrder;

		private DatasetNameComparator(SortOrder sortOrder) {
			this.sortOrder = sortOrder;
		}

		@Override
		public int compare(Dataset o1, Dataset o2) {
			int ascResult = o1.getName().compareTo(o2.getName());
			if (this.sortOrder.equals(SortOrder.ASCENDING)) {
				return ascResult;
			} else {
				return -ascResult;
			}
		}
	}

	/**
	 * Helps create the datas (for the jQuery DataTable) for the datasets table in the test case view.
	 * 
	 * @author mpagnon
	 * 
	 */
	public final static class DatasetsDataTableModelHelper extends DataTableModelHelper<Dataset> {

		public DatasetsDataTableModelHelper() {
			super();
		}

		@Override
		public Map<String, Object> buildItemData(Dataset item) {
			Map<String, Object> res = new HashMap<String, Object>();
			res.put(DataTableModelHelper.DEFAULT_ENTITY_ID_KEY, item.getId());
			res.put(DataTableModelHelper.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
			res.put(DataTableModelHelper.NAME_KEY, item.getName());
			for (DatasetParamValue parameterValue : item.getParameterValues()) {
				res.put("parameter-" + parameterValue.getParameter().getId(), "id="+parameterValue.getId()+", value="+parameterValue.getParamValue());
			}
			res.put(DataTableModelHelper.DEFAULT_EMPTY_DELETE_HOLDER_KEY, "");
			return res;
		}

		public List<Map<?,?>> buildAllData(Collection<Dataset> source){
			List<Map<?,?>> result = new ArrayList<Map<?,?>>(source.size());
			for (Dataset item : source){
				incrementIndex();
				Map<?,?> itemData = (Map<?, ?>) buildItemData(item);
				result.add(itemData);
			}
			return result;
		}
		
	}

	/**
	 * Will add a new dataset to the test case
	 * 
	 * @param testCaseId
	 *            : the id of the test case that will hold the new dataset
	 * @param dataset
	 *            : the dataset to add with it's set name
	 */
	@RequestMapping(value = "/new", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public void newDataset(@PathVariable long testCaseId, @Valid @RequestBody NewDataset dataset) {
		TestCase testCase = testCaseFinder.findById(testCaseId);
		try{
			datasetModificationService.persist(dataset.createTransientEntity(testCase, parameterFinder), testCaseId);
			}catch (DomainException e) {
				e.setObjectName("add-dataset");
				throw e;
			}
	}
}
