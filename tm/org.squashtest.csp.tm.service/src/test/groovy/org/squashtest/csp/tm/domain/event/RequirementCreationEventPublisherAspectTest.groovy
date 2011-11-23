/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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

package org.squashtest.csp.tm.domain.event;

import spock.lang.Specification;

import org.squashtest.csp.core.service.security.UserContextService;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.internal.service.CampaignTestPlanManagerServiceImplTest.MockTC;
import org.squashtest.csp.tm.internal.service.event.RequirementAuditor;
import org.squashtest.csp.tm.internal.repository.RequirementDao;
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory;

/**
 * @author Gregory Fouquet
 *
 */
class RequirementCreationEventPublisherAspectTest extends Specification {
	RequirementDao dao = new StubRequirementDao()
	RequirementAuditor auditor = Mock()
	UserContextService userContext = Mock()
	def event
	
	def setup() {
		use (ReflectionCategory) {
			def aspect = RequirementCreationEventPublisherAspect.aspectOf()
			AbstractRequirementEventPublisher.set field: "auditor", of: aspect, to: auditor 
			AbstractRequirementEventPublisher.set field: "userContext", of: aspect, to: userContext
		}
		
		userContext.getUsername() >> "bruce dickinson"
	}
	
	def "should raise event when requirement is persisted"() {
		given:
		Requirement requirement = new Requirement()
		
		when:
		dao.persist requirement
		
		then:
		1 * auditor.notify({event = it})
		event instanceof RequirementCreation
		event.requirement == requirement
		event.author == "bruce dickinson"
	}
	
	def "uninitialized auditor should not break dao usage"() {
		given:
		use (ReflectionCategory) {
			def aspect = RequirementCreationEventPublisherAspect.aspectOf()
			AbstractRequirementEventPublisher.set field: "auditor", of: aspect, to: null 
		}

		and:
		
		when:
		dao.persist new Requirement()
		
		then:
		notThrown(NullPointerException)
	}
	
	def "uninitialized user context should generate 'unknown' event author"() {
		given:
		use (ReflectionCategory) {
			def aspect = RequirementCreationEventPublisherAspect.aspectOf()
			AbstractRequirementEventPublisher.set field: "userContext", of: aspect, to: null
		}

		when:
		dao.persist new Requirement()
		
		then:
		notThrown(NullPointerException)
		1 * auditor.notify({ event = it })
		event.author == "unknown"
	}

}
