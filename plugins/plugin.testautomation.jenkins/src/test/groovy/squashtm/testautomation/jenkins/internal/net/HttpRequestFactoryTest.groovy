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
package squashtm.testautomation.jenkins.internal.net




import org.squashtest.csp.tm.domain.testautomation.AutomatedTest;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.csp.tm.testautomation.model.TestAutomationProjectContent;

import spock.lang.Specification
import squashtm.testautomation.jenkins.internal.net.HttpRequestFactory;

class HttpRequestFactoryTest extends Specification {
	
	private HttpRequestFactory factory
	
	def setup(){
		factory = new HttpRequestFactory()
	}
	
	def "should return a well formatted query"(){
		
		given :
			TestAutomationServer server = new TestAutomationServer(new URL("http://ci.jruby.org"), "", "")
			
		when :
			def method = factory.newGetJobsMethod(server)
			
		then :
			method.path == "http://ci.jruby.org/api/json"
			method.queryString == "tree=jobs%5Bname%2Ccolor%5D"
		
	}
	
	
	def "should create a suitable test suite parameter"(){
		
		given :
			
			def project = Mock(TestAutomationProject)
			project.getName() >> "the-test-project"
		
		and :
			def tests = []
			
			[   "tests/base-test.txt", 
				"tests/subfolder/folder-test.txt", 
				"tests/refolder/another-test.txt" 
			].each{
				tests << new AutomatedTest(it, project)				
			}
			
		and :
			def content = new TestAutomationProjectContent(project, tests)
			
		and : 
			def expected = "base-test.txt,subfolder/folder-test.txt,refolder/another-test.txt"
		
		when :
			def param = factory.makeTestListParameter(content)
		
		then :
			param == expected
				
	}
	
	

}

