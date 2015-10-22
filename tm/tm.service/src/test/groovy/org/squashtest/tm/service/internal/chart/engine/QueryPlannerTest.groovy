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
package org.squashtest.tm.service.internal.chart.engine

import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.chart.ChartQuery.NaturalJoinStyle;
import org.squashtest.tm.domain.testcase.TestCase;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.hibernate.HibernateQuery;

import spock.lang.Specification
import static org.squashtest.tm.service.internal.chart.engine.ChartEngineTestUtils.*;
import static org.squashtest.tm.service.internal.chart.engine.InternalEntityType.*;


class QueryPlannerTest extends Specification {


	def "should add a natural inner join, if the join doesn't exist yet"(){

		given :
		HibernateQuery hquery = Mock(HibernateQuery)

		DetailedChartQuery cquery = new DetailedChartQuery(
				rootEntity : REQUIREMENT,
				targetEntities : [REQUIREMENT, REQUIREMENT_VERSION],
				joinStyle : NaturalJoinStyle.INNER_JOIN
				)

		QuerydslToolbox tools = Mock(QuerydslToolbox)

		def aliases = ['requirement'] as Set

		and :
		QueryPlanner planner = new QueryPlanner(
				aliases : aliases,
				utils : tools,
				query : hquery,
				definition : cquery
				)

		when :
		planner.addNaturalJoin(r,v,"versions")

		then :
		1 * tools.makePath(r, v, "versions") >> Mock(PathBuilder)
		1 * hquery.innerJoin(_ as PathBuilder, v)

	}

	def "should add a natural left join, if the join doesn't exist yet"(){

		given :
		HibernateQuery hquery = Mock(HibernateQuery)

		DetailedChartQuery cquery = new DetailedChartQuery(
				rootEntity : REQUIREMENT,
				targetEntities : [REQUIREMENT, REQUIREMENT_VERSION],
				joinStyle : NaturalJoinStyle.LEFT_JOIN
				)

		QuerydslToolbox tools = Mock(QuerydslToolbox)

		def aliases = ['requirement'] as Set

		and :
		QueryPlanner planner = new QueryPlanner(
				aliases : aliases,
				utils : tools,
				query : hquery,
				definition : cquery
				)

		when :
		planner.addNaturalJoin(r,v,"versions")

		then :
		1 * tools.makePath(r, v, "versions") >> Mock(PathBuilder)
		1 * hquery.leftJoin(_ as PathBuilder, v)

	}

	def "should not add a natural join if exists already"(){

		given :
		HibernateQuery hquery = Mock(HibernateQuery)

		DetailedChartQuery cquery = new DetailedChartQuery(
				rootEntity : REQUIREMENT,
				targetEntities : [REQUIREMENT, REQUIREMENT_VERSION],
				joinStyle : NaturalJoinStyle.INNER_JOIN
				)

		QuerydslToolbox tools = Mock(QuerydslToolbox)

		def aliases = ['requirement', 'requirementVersion'] as Set

		and :
		QueryPlanner planner = new QueryPlanner(
				aliases : aliases,
				utils : tools,
				query : hquery,
				definition : cquery
				)

		when :
		planner.addNaturalJoin(r,v,"versions")

		then :
		0 * tools.makePath(r, v, "versions")
		0 * hquery.innerJoin(_ as PathBuilder, v)
	}


	def "should add a where join, if the join doesn't exist yet"(){

		given :
		HibernateQuery hquery = Mock(HibernateQuery)

		DetailedChartQuery cquery = new DetailedChartQuery(
				rootEntity : TEST_CASE,
				targetEntities : [TEST_CASE, ITEM_TEST_PLAN]
				)

		QuerydslToolbox tools = Mock(QuerydslToolbox)

		def aliases = ['testCase'] as Set

		and :
		QueryPlanner planner = new QueryPlanner(
				aliases : aliases,
				utils : tools,
				query : hquery,
				definition : cquery
				)

		when :
		planner.addWhereJoin(tc, itp,"referencedTestCase")

		then :
		1 * tools.makePath(itp, tc, "referencedTestCase") >> new PathBuilder(IterationTestPlanItem.class, "iterationTestPlanItem").get('referencedTestCase', TestCase.class)
		1 * hquery.where(_ as Predicate)

	}


	def "should not add a where join, if the join doesn't exist yet"(){

		given :
		HibernateQuery hquery = Mock(HibernateQuery)

		DetailedChartQuery cquery = new DetailedChartQuery(
				rootEntity : TEST_CASE,
				targetEntities : [TEST_CASE, ITEM_TEST_PLAN]
				)

		QuerydslToolbox tools = Mock(QuerydslToolbox)

		def aliases = ['testCase', 'iterationTestPlanItem'] as Set

		and :
		QueryPlanner planner = new QueryPlanner(
				aliases : aliases,
				utils : tools,
				query : hquery,
				definition : cquery
				)

		when :
		planner.addWhereJoin(tc, itp,"referencedTestCase")

		then :
		0 * tools.makePath(itp, tc, "referencedTestCase")
		0 * hquery.where(_ as Predicate)

	}


	def "should create a query from a main chart query"(){

		given :
		DetailedChartQuery cquery = new DetailedChartQuery(
				rootEntity : TEST_CASE,
				targetEntities : [REQUIREMENT, TEST_CASE]
				)

		QueryPlanner planner = new QueryPlanner(cquery)

		when :
		HibernateQuery res = planner.createQuery()
		res.select(r.id)

		then :
		res.toString() ==
				"""select requirement.id
from TestCase testCase
  inner join testCase.requirementVersionCoverages as requirementVersionCoverage
  inner join requirementVersionCoverage.verifiedRequirementVersion as requirementVersion
  inner join requirementVersion.requirement as requirement"""
	}

	def "should append a subquery to a main query"(){

		given :
		DetailedChartQuery cquery = new DetailedChartQuery(
				rootEntity : REQUIREMENT_VERSION,
				targetEntities : [REQUIREMENT_VERSION, REQUIREMENT_VERSION_CATEGORY]
				)

		HibernateQuery mainq =
				new HibernateQuery().from(r)
				.innerJoin(r.versions, v)
				.innerJoin(v.requirementVersionCoverages, cov)
				.innerJoin(cov.verifyingTestCase, tc)
				.select(r.id)

		and :

		QuerydslToolbox tools = new QuerydslToolbox("sub")

		QueryPlanner planner =
				new QueryPlanner(cquery, tools)
				.appendToQuery(mainq)
				.joinRootEntityOn(v)

		when :
		planner.modifyQuery()

		then :
		mainq.toString() ==
				"""select requirement.id
from Requirement requirement
  inner join requirement.versions as requirementVersion
  inner join requirementVersion.requirementVersionCoverages as requirementVersionCoverage
  inner join requirementVersionCoverage.verifyingTestCase as testCase
  inner join requirementVersion.category as reqversionCategory_sub"""
	}

	def "should not append twice a subquery to a main query"(){

		given :
		DetailedChartQuery cquery = new DetailedChartQuery(
				rootEntity : REQUIREMENT_VERSION,
				targetEntities : [REQUIREMENT_VERSION, REQUIREMENT_VERSION_CATEGORY]
				)

		HibernateQuery mainq =
				new HibernateQuery().from(r)
				.innerJoin(r.versions, v)
				.innerJoin(v.requirementVersionCoverages, cov)
				.innerJoin(cov.verifyingTestCase, tc)
				.select(r.id)

		and :

		QuerydslToolbox tools = new QuerydslToolbox("sub")

		QueryPlanner planner =
				new QueryPlanner(cquery, tools)
				.appendToQuery(mainq)
				.joinRootEntityOn(v)

		when :
		planner.modifyQuery()
		planner.modifyQuery()


		then :
		mainq.toString() ==
				"""select requirement.id
from Requirement requirement
  inner join requirement.versions as requirementVersion
  inner join requirementVersion.requirementVersionCoverages as requirementVersionCoverage
  inner join requirementVersionCoverage.verifyingTestCase as testCase
  inner join requirementVersion.category as reqversionCategory_sub"""
	}
}
