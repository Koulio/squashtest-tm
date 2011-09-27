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
package org.squashtest.csp.tm.domain.report.common.hibernate;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.squashtest.csp.tm.domain.report.query.QueryOperator;
import org.squashtest.csp.tm.domain.report.query.hibernate.ReportCriterion;

/*
 * this ReportCriterion is exactly wysiwyg
 * 
 */
public class AboveDateCriterion extends ReportCriterion {
	
	public AboveDateCriterion(){

		setOperator(QueryOperator.COMPARATOR_GT);
		setParamClass(Date.class);		
	}
	
	public AboveDateCriterion(String criterionName,String attributePath){
		this();
		setCriterionName(criterionName);
		setAttributePath(attributePath);

	}
	

	private Date makeDate(){
		Object[] values = getParameters();
		if (values.length!=1) throw new IllegalArgumentException("Criterion of type "+this.getClass().getSimpleName()+" cannot have more than one argument");
		String strDate = values[0].toString();
		Long lDate = Long.parseLong(strDate);
		
		Date date = new Date(lDate);
		
		Calendar calendar = GregorianCalendar.getInstance();
		
		calendar.setTime(date);
		
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		
		return calendar.getTime();
		
		
	}

	@Override
	public Criterion makeCriterion() {
		try{
			Criterion result = null;
			
			Date arg = makeDate();
				
			result= Restrictions.gt(getAttributePath(), arg );
			
			return result;
		}catch(Exception e){
			return null;
		}
	}

}
