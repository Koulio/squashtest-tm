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
package org.squashtest.tm.service.internal.chart.engine;

import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.domain.testcase.QTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.hibernate.HibernateQuery;

/**
 * <p></p>
 * 
 * <p>
 * 	This class will generate the main query, that is a query that joins together the sequence of tables required for the given chart.
 * 	Whenever possible the natural joins will be used; however we are dependent on the way the entities were mapped : when no natural join
 * 	is available a where clause will be used.
 * </p>
 * 
 * <p>See javadoc on {@link ChartDataFinder}</p>
 * 
 * 
 * @author bsiri
 *
 */

class MainQueryPlanner {


	private DetailedChartDefinition definition;


	MainQueryPlanner(DetailedChartDefinition definition){
		this.definition = definition;
	}


	HibernateQuery<?> createMainQuery(){

		// get the query plan : the orderly set of joins this
		// class must now put together
		QueryPlan plan = DomainGraph.getQueryPlan(definition);

		// now get the query done
		//TraversedEntity rootNode = plan.getR
		return null;

	}


	private void test(){

		String tcAlias = "tc";
		String stepAlias = "st";


		EntityPathBase testcase = new QTestCase(tcAlias);
		EntityPathBase tcsteps = new QTestStep(stepAlias);


		PathBuilder stepjoin = new PathBuilder<>(TestCase.class, tcAlias)
				.get("steps", TestStep.class);

		PathBuilder stepid = new PathBuilder(TestStep.class, stepAlias).get("id");

		PathBuilder tcid = new PathBuilder(TestCase.class, tcAlias).get("id");

		HibernateQuery q = new HibernateQuery();

		q.from(testcase);

		q.join(stepjoin, tcsteps);
		q.select(stepid);
		q.where(tcid.eq(-1l));

	}



}
