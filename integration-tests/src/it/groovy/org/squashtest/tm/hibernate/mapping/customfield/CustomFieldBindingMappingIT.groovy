/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.hibernate.mapping.customfield

import javax.inject.Inject

import org.apache.poi.hssf.record.formula.functions.T
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.service.internal.repository.hibernate.DbunitDaoSpecification
import org.squashtest.tm.domain.customfield.BindableEntity
import org.squashtest.tm.domain.customfield.CustomField
import org.squashtest.tm.domain.customfield.CustomFieldBinding
import org.squashtest.tm.domain.customfield.RenderingLocation
import org.squashtest.tm.domain.project.Project
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

/**
 * @author Gregory Fouquet
 *
 */
@UnitilsSupport
@Transactional
@DataSet
class CustomFieldBindingMappingIT extends DbunitDaoSpecification {
	@Inject SessionFactory sessionFactory

	Session getSession() {
		sessionFactory.currentSession
	}

	def "should persist and fetch a custom field binding"() {
		given:
		Project p = session.load(Project, -1L)
		CustomField cf = session.load(CustomField, -10L)

		when:
		CustomFieldBinding cfb = new CustomFieldBinding()
		cfb.boundEntity = BindableEntity.TEST_CASE
		cfb.boundProject = p
		cfb.customField = cf

		session.persist cfb
		session.flush()
		session.clear()

		and:
		CustomFieldBinding res = session.get(CustomFieldBinding, cfb.id)

		then:
		res.boundEntity == BindableEntity.TEST_CASE
		res.boundProject.id == -1L
		res.customField.id == -10L
		res.renderingLocations == Collections.emptySet()
	}

	def "should add rendering locations to a custom field binding"() {
		given:
		CustomFieldBinding cfb = session.load(CustomFieldBinding, -202L)

		when:
		cfb.renderingLocations.add(RenderingLocation.TEST_PLAN)

		session.flush()
		session.clear()

		and:
		CustomFieldBinding res = session.get(CustomFieldBinding, -202L)

		then:
		println res.renderingLocations
		res.renderingLocations == new HashSet([RenderingLocation.TEST_PLAN])
	}
}
