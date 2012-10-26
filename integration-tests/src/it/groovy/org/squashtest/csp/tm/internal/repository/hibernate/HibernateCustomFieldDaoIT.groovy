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
package org.squashtest.csp.tm.internal.repository.hibernate

import javax.inject.Inject

import org.squashtest.csp.tm.domain.customfield.CustomField
import org.squashtest.csp.tm.domain.customfield.InputType
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting
import org.squashtest.csp.tm.internal.repository.CustomFieldDao
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
class HibernateCustomFieldDaoIT extends DbunitDaoSpecification {
	@Inject
	CustomFieldDao customFieldDao

	@DataSet("HibernateCustomFieldDaoIT.should return list of cuf ordered by name.xml")
	def "should return list of cuf ordered by name" () {
		when:
		List<CustomField> list = customFieldDao.finAllOrderedByName()

		then:
		list.size() == 3
		list.get(0).name == "abc"
		list.get(1).name == "cde"
		list.get(2).name == "fde"
	}
	
	@DataSet("HibernateCustomFieldDaoIT.should return sorted list of cuf.xml")
	def "should return sorted list of cuf"(){
		when:
		List<CustomField> list = customFieldDao.findSortedCustomFields(new InputTypeCollectionFilter())
		
		then: 
		list.size() == 2
		list.get(0).inputType == InputType.DROPDOWN_LIST
		list.get(1).inputType == InputType.PLAIN_TEXT

	}
	
	private class InputTypeCollectionFilter implements CollectionSorting	{
			@Override
			String getSortedAttribute(){
				return "inputType"
			}
			@Override
			String getSortingOrder(){
				return "asc"
			}
			@Override
			public int getFirstItemIndex() {
				return 1;
			}
			@Override
			public int getPageSize() {
				return 2;
			}
	}
	
}
