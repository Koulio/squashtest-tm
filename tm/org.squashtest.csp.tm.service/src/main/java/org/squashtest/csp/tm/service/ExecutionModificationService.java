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
package org.squashtest.csp.tm.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.infrastructure.filter.CollectionFilter;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.service.deletion.SuppressionPreviewReport;

@Transactional(readOnly = false)
public interface ExecutionModificationService extends ExecutionFinder {

	Execution findAndInitExecution(Long executionId);

	void setExecutionDescription(Long executionId, String description);

	/*********************************** Steps methods *****************************************/

	FilteredCollectionHolder<List<ExecutionStep>> findExecutionSteps(long executionId, CollectionFilter filter);

	void setExecutionStepComment(Long executionStepId, String comment);

	/**
	 * that method should investigate the consequences of the deletion of the given executions, and return a report
	 * about what will happen.
	 * 
	 * @param targetIds
	 * @return
	 */
	List<SuppressionPreviewReport> simulateExecutionDeletion(Long execId);

	/**
	 * that method should delete the execution. It still takes care of non deletable executions so the implementation
	 * should abort if the execution can't be deleted.
	 * 
	 * 
	 * @param targetIds
	 * @throws RuntimeException
	 *             if the execution should not be deleted.
	 */
	void deleteExecution(Execution execution);

}
