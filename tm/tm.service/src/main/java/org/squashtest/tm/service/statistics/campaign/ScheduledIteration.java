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
package org.squashtest.tm.service.statistics.campaign;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;


public final class ScheduledIteration{
	
	public static final String	SCHED_ITER_MISSING_DATES_I18N = "dashboard.campaigns.progression.errors.nulldates";
	public static final String SCHED_ITER_OVERLAP_DATES_I18N = "dashboard.campaigns.progression.errors.overlap";
	
	private long id;
	private String name;
	private long testplanCount;
	private Date scheduledStart;
	private Date scheduledEnd;
	
	// an entry = { Date, int }
	private Collection<Object[]> cumulativeTestsByDate = new LinkedList<Object[]>();
	
	public ScheduledIteration(){
		super();
	}
	
	
	public ScheduledIteration(long id, String name, long testplanCount,
			Date scheduledStart, Date scheduledEnd) {
		super();
		this.id = id;
		this.name = name;
		this.testplanCount = testplanCount;
		this.scheduledStart = scheduledStart;
		this.scheduledEnd = scheduledEnd;
	}



	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	
	public long getTestplanCount() {
		return testplanCount;
	}

	public void setTestplanCount(long testplanCount) {
		this.testplanCount = testplanCount;
	}

	public Date getScheduledStart() {
		return scheduledStart;
	}
	
	public void setScheduledStart(Date scheduledStart) {
		this.scheduledStart = scheduledStart;
	}
	
	public Date getScheduledEnd() {
		return scheduledEnd;
	}
	
	public void setScheduledEnd(Date scheduledEnd) {
		this.scheduledEnd = scheduledEnd;
	}

	public Collection<Object[]> getCumulativeTestsByDate() {
		return cumulativeTestsByDate;
	}
	
	public void addCumulativeTestByDate(Object[] testByDate) {
		cumulativeTestsByDate.add(testByDate);
	}

	
	public static final void checkIterationsDatesIntegrity(Collection<ScheduledIteration> iterations){
		
		ScheduledDatesIterator datesIterator = new ScheduledDatesIterator(iterations);
		
		Date prevDate;
		Date currDate;
		
		if (! datesIterator.hasNext()){
			return;
		}
		
		currDate = datesIterator.next();
		if (currDate == null){
			throw new IllegalArgumentException(SCHED_ITER_MISSING_DATES_I18N);
		}
		
		while (datesIterator.hasNext()){
			
			prevDate = currDate;
			currDate = datesIterator.next();
			
			if (currDate == null){
				throw new IllegalArgumentException(SCHED_ITER_MISSING_DATES_I18N);
			}
			
			if (currDate.before(prevDate)){
				throw new IllegalArgumentException(SCHED_ITER_OVERLAP_DATES_I18N);
			}
		}
		
	}
	
	
	private static final class ScheduledDatesIterator implements Iterator<Date>{
		
		private Iterator<ScheduledIteration> iterator;
		
		private boolean isCurrentdateAStartdate = false;
		private ScheduledIteration currentIteration;
		
		ScheduledDatesIterator(Collection<ScheduledIteration> iterations){
			iterator = iterations.iterator();
		}
		
		@Override
		public boolean hasNext() {
			return (isCurrentdateAStartdate || iterator.hasNext());
		}
		
		@Override
		public Date next() {
			Date result;
			if (isCurrentdateAStartdate){
				result = currentIteration.scheduledEnd;
				isCurrentdateAStartdate = false;
			}
			else{
				currentIteration = iterator.next();
				result = currentIteration.scheduledStart;
				isCurrentdateAStartdate = true;
			}
			return result;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
}