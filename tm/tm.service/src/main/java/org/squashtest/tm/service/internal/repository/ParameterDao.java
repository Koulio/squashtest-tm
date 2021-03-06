/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.service.internal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.service.annotation.EmptyCollectionGuard;


public interface ParameterDao extends Repository<Parameter, Long>, CustomParameterDao {

	@NativeMethodFromJpaRepository
	void save(Parameter parameter);

	@NativeMethodFromJpaRepository
	void delete(Parameter parameter);

	@UsesTheSpringJpaDsl
	Parameter findById(Long id);

	@UsesANamedQueryInPackageInfoOrElsewhere
	@Modifying
	@EmptyCollectionGuard
	void removeAllByTestCaseIds(@Param("testCaseIds") List<Long> removeAllByTestCaseIds);

	@UsesANamedQueryInPackageInfoOrElsewhere
	@Modifying
	@EmptyCollectionGuard
	void removeAllValuesByTestCaseIds(@Param("testCaseIds") List<Long> testCaseIds);

	/**
	 * Given a test case ID, returns the list of parameters that directly belong to that test case
	 * (inherited parameters are ignored).
	 *
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	// corresponds to JPA dsl : findByTestCaseIdOrderByNameAndTestCaseNameAsc, but this would be less expressive
	List<Parameter> findOwnParametersByTestCase(@Param("testCaseId") Long testcaseId);

	/**
	 * For a given test case, finds the parameter bearing the given name. Note that the test case must
	 * own the parameter, ie the query wont search for delegated parameters.
	 *
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	// corresponds to JPA dsl : findByNameAndTestCaseId, but this would be less expressive
	Parameter findOwnParameterByNameAndTestCase(@Param("name") String name, @Param("testCaseId") Long testcaseId);


}
