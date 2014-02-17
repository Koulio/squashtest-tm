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
package org.squashtest.tm.service.internal.testcase;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.squashtest.tm.service.internal.repository.TestCaseDao;

/**
 * 
 * @author Gregory Fouquet
 *
 */
@Component
public class TestCaseCallTreeFinder {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseCallTreeFinder.class);
	@Inject
	private TestCaseDao testCaseDao;
	
	/**
	 *  given the Id of a test case, will compute the subsequent test case call tree.
	 * 
	 * @param rootTcId. Null is not legal and unchecked.
	 * @return a set containing the ids of the called test cases, that will not include the calling test case id. Not null, possibly empty. 
	 */
	public Set<Long> getTestCaseCallTree(Long rootTcId) {

		Set<Long> calleesIds = new HashSet<Long>();
		List<Long> prevCalleesIds = testCaseDao.findAllDistinctTestCasesIdsCalledByTestCase(rootTcId);

		LOGGER.trace("TestCase #{} directly calls {}", rootTcId, prevCalleesIds);

		prevCalleesIds.remove(rootTcId);// added to prevent infinite cycle in case of inconsistent data

		while (!prevCalleesIds.isEmpty()) {
			// FIXME a tester avant correction : boucle infinie quand il y a un cycle dans les appels de cas de test
			calleesIds.addAll(prevCalleesIds);
			prevCalleesIds = testCaseDao.findAllTestCasesIdsCalledByTestCases(prevCalleesIds);

			LOGGER.trace("TestCase #{} indirectly calls {}", rootTcId, prevCalleesIds);
			prevCalleesIds.remove(rootTcId);// added to prevent infinite cycle in case of inconsistent data
		}

		return calleesIds;
	}
	
	/**
	 * same as {@link #getTestCaseCallTree(Long)}, but for multiple test cases
	 * 
	 * @param tcIds
	 * @return
	 */
	public Set<Long> getTestCaseCallTree(Collection<Long> tcIds){
		
		Set<Long> result = new HashSet<Long>();
		
		Collection<Long> process = tcIds;
		List<Long> next;
		
		while(! process.isEmpty()){
			next = testCaseDao.findAllTestCasesIdsCalledByTestCases(process);
			next.removeAll(result);	// remove results that were already processed
			
			result.addAll(next);
			process = next;
			
		}
		
		return result;
	}
	
	
	public Set<Long> getTestCaseCallers(Long rootTcId) {
		
		Set<Long> callerIds = new HashSet<Long>();
		List<Long> prevCallerIds = testCaseDao.findAllDistinctTestCasesIdsCallingTestCase(rootTcId);
	
		LOGGER.trace("TestCase #{} directly calls {}", prevCallerIds, rootTcId);

		prevCallerIds.remove(rootTcId);// added to prevent infinite cycle in case of inconsistent data

		while (!prevCallerIds.isEmpty()) {
			// FIXME a tester avant correction : boucle infinie quand il y a un cycle dans les appels de cas de test
			callerIds.addAll(prevCallerIds);
			prevCallerIds = testCaseDao.findAllTestCasesIdsCallingTestCases(prevCallerIds);

			LOGGER.trace("TestCase #{} indirectly calls {}", prevCallerIds, rootTcId);
			prevCallerIds.remove(rootTcId);// added to prevent infinite cycle in case of inconsistent data
		}

		return callerIds;
	}

}
