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
package org.squashtest.tm.service.internal.repository.hibernate

import javax.inject.Inject

import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.service.internal.repository.TestStepDao
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
class HibernateTestStepDaoIT extends DbunitDaoSpecification {
	
	@Inject TestStepDao stepDao
	

	@DataSet("HibernateTestCaseDaoIT.should find filtered steps by test case id.xml")
	def "should load a step with its test case"() {
		when :
			ActionTestStep st = stepDao.findById(200l)
			
		then :
			st.testCase.id == 10l
	}
	
	@DataSet("HibernateTestCaseDaoIT.should find filtered steps by test case id.xml")
	def "should find the index of a step"(){
		
		when :
			def index = stepDao.findPositionOfStep(300l)
			
		then :
			index == 2
		
		
	}
	
}
