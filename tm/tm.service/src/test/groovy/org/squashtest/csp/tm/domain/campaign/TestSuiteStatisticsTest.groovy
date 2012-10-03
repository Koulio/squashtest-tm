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
package org.squashtest.csp.tm.domain.campaign

import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;

import spock.lang.Specification;

class TestSuiteStatisticsTest extends Specification {

	
	def "should return the good values and status RUNNING"(){
		
		when :
			def stats = new TestSuiteStatistics(10L, 0, 2, 1, 4, 2, 1)
			
		then :
			stats.nbTestCases == 10L
			stats.nbUntestable == 0
			stats.nbBloqued == 2
			stats.nbFailure == 1
			stats.nbSuccess == 4
			stats.nbRunning == 2
			stats.nbReady == 1
			stats.nbDone == 7
			stats.progression == 70
			stats.status == ExecutionStatus.RUNNING
	}
	
	def "should return the good values and status READY"(){
		
		when :
			def stats = new TestSuiteStatistics(10L, 0, 0, 0, 0, 0, 10)
			
		then :
			stats.nbTestCases == 10L
			stats.nbUntestable == 0
			stats.nbBloqued == 0
			stats.nbFailure == 0
			stats.nbSuccess == 0
			stats.nbRunning == 0
			stats.nbReady == 10
			stats.nbDone == 0
			stats.progression == 0
			stats.status == ExecutionStatus.READY
	}
	
	def "should return the good values and status SUCCESS"(){
		
		when :
			def stats = new TestSuiteStatistics(10L, 0, 3, 2, 5, 0, 0)
			
		then :
			stats.nbTestCases == 10L
			stats.nbUntestable == 0
			stats.nbBloqued == 3
			stats.nbFailure == 2
			stats.nbSuccess == 5
			stats.nbRunning == 0
			stats.nbReady == 0
			stats.nbDone == 10
			stats.progression == 100
			stats.status == ExecutionStatus.SUCCESS
	}
	
}
