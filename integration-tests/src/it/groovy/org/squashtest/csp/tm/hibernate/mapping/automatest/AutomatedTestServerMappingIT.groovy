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
package org.squashtest.csp.tm.hibernate.mapping.automatest


import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.squashtest.csp.tm.domain.automatest.AutomatedTestServer;
import org.squashtest.csp.tm.hibernate.mapping.HibernateMappingSpecification


class AutomatedTestServerMappingIT extends HibernateMappingSpecification {

	
	
	def "should persist a new AutomatedTestServer"(){
		
		given :
			URL baseUrl = new URL("http://www.squashtest.org/")
		
		and :
			AutomatedTestServer server = new AutomatedTestServer()
			server.setBaseURL(baseUrl)
			
		when :
			persistFixture server
			def server2 = doInTransaction({it.get(AutomatedTestServer.class, server.id)})
		
		then :
			server2.baseURL.equals(server.baseURL)
			
		cleanup :
			deleteFixture server
		
	}
	
}
