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

import com.querydsl.jpa.hibernate.HibernateQuery;

/*
 * TODO : about the subqueries
 * 
 * Lets see simple examples that illustrate the mechanism, and how we could optimize this
 * 
 * ===========================================================
 * 
 * -------------------------
 * Current mechanism
 * -------------------------
 * 
 *  1/ select context :
 * 
 * 	Our example is "select requirement id and  their version count for all requirements covered by test case 1"
 * 
 *  Produced implementation is (correlated subquery) :
 * 
 * select requirement.id, (select count(distinct version2) from Requirement r2 join r2.versions versions2 where r2.id = requirement.id)
 * from TestCase testCase join testCase.requirementVersionCoverages cov
 * join cov.verifiedRequirementVersion version
 * join version.requirement requirement
 * group by testCase.id
 * 
 * 
 * ----------------------
 * 
 * 2/ where context
 * 
 *  Our example is "select count requirement covered by test case 1 that have at least 2 versions"
 * 
 * Produced implementation is (using subquery) :
 * 
 * select count(distinct requirement.id)
 * from TestCase testCase join testCase.requirementVersionCoverages cov
 * join cov.verifiedRequirementVersion version
 * join version.requirement requirement
 * where testCase.id =1
 * and requirement.id in (select req2.id from Requirement req2 join req2.versions version2 group by req2.id having count(distinct version2) > 1 )
 * group by testCase.id
 * 
 * 
 */
class QueryBuilder {

	enum QueryProfile{
		MAIN_QUERY,			// for the main query, tuples returns the full axis + measures data
		SUBSELECT_QUERY,	// generate correlated subqueries, returning the measure only, correlated on axes supplied by the outer query
		SUBWHERE_QUERY;		// generate uncorrelated subqueries, returning the axes only, measures must match predicates supplied by the outer query
	}

	protected QuerydslToolbox utils = new QuerydslToolbox();

	protected DetailedChartQuery queryDefinition;

	// the SubQueryBuilder would use a different strategy.
	// for the QueryBuilder, it is set to MAIN_QUERY.
	protected QueryProfile profile = QueryProfile.MAIN_QUERY;

	protected HibernateQuery<?> detachedQuery;

	QueryBuilder(DetailedChartQuery queryDefinition){
		super();
		this.queryDefinition = queryDefinition;
	}




	// **************** actual building ***************************

	HibernateQuery<?> createQuery(){


		QueryPlanner mainPlanner = new QueryPlanner(queryDefinition, utils);
		detachedQuery = mainPlanner.createQuery();


		ProjectionPlanner projectionPlanner = new ProjectionPlanner(queryDefinition, detachedQuery, utils);
		projectionPlanner.setProfile(profile);
		projectionPlanner.modifyQuery();


		FilterPlanner filterPlanner = new FilterPlanner(queryDefinition, detachedQuery, utils);
		filterPlanner.modifyQuery();

		return detachedQuery;
	}



}
