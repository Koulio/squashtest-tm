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
package org.squashtest.tm.domain.testutils

import org.squashtest.tm.domain.infolist.InfoList
import org.squashtest.tm.domain.infolist.InfoListItem
import org.squashtest.tm.domain.infolist.TransientListItem
import org.squashtest.tm.domain.project.Project

import spock.lang.Specification;

class MockFactory extends Specification {

	def mockProject(){

		Project p = Mock();

		InfoList natures = new InfoList()
		InfoListItem nat = new 	TransientListItem()
		nat.setDefault(true)
		natures.items << nat

		InfoList types = new InfoList()
		InfoListItem typ = new 	TransientListItem()
		typ.setDefault(true)
		types.items << typ

		InfoList categories = new InfoList()
		InfoListItem cat = new TransientListItem();
		cat.setDefault(true)
		categories.items << cat

		p.getTestCaseNatures() >> natures
		p.getTestCaseTypes() >> types
		p.getRequirementCategories() >> categories


		p

	}

}
