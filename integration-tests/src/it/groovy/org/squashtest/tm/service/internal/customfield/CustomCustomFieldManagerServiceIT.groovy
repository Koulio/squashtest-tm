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
package org.squashtest.tm.service.internal.customfield

import javax.inject.Inject

import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.customfield.CustomFieldValue
import org.squashtest.tm.service.internal.repository.CustomFieldValueDao;
import org.squashtest.tm.service.DbunitServiceSpecification;
import org.squashtest.tm.service.customfield.CustomFieldManagerService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport;

@UnitilsSupport
@Transactional
@DataSet("CustomFieldVariousIT.sandbox.xml")
class CustomCustomFieldManagerServiceIT extends DbunitServiceSpecification {

	@Inject 
	CustomFieldManagerService service;
	
	@Inject 
	CustomFieldValueDao customFieldValueDao;
	
	@Inject
	SessionFactory sessionFactory;
	
	def "should add default value to custom fields without a value"(){
		
		when :
			service.changeOptional(1l,false);
			CustomFieldValue value1 = customFieldValueDao.findById(1111l);
			CustomFieldValue value2 = customFieldValueDao.findById(1112l);
			
		then :
			value1.getValue().equals("NOSEC");
			value2.getValue().equals("true");
	}
}
