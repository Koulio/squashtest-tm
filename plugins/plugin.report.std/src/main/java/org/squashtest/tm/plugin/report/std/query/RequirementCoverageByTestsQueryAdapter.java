/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.plugin.report.std.query;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.internal.domain.report.common.hibernate.HibernateRequirementCoverageByTestsQuery;
import org.squashtest.tm.internal.domain.report.query.hibernate.HibernateReportQuery;

/**
 * @author Gregory Fouquet
 * 
 */
public class RequirementCoverageByTestsQueryAdapter extends LegacyQueryAdapter<HibernateRequirementCoverageByTestsQuery> {

	@Inject
	private Provider<HibernateRequirementCoverageByTestsQuery> legacyQueryProvider;

	/**
	 * 
	 */
	private static final String LEGACY_PROJECT_IDS = "projectIds[]";

	/**
	 * @see org.squashtest.tm.plugin.report.std.query.LegacyQueryAdapter#processNonStandardCriteria(java.util.Map,
	 *      org.squashtest.tm.internal.domain.report.query.hibernate.HibernateReportQuery)
	 */
	@Override
	protected void processNonStandardCriteria(Map<String, Criteria> criteria, HibernateReportQuery legacyQuery) {
		
			Criteria idsCrit = criteria.get("projectIds");
			legacyQuery.setCriterion(LEGACY_PROJECT_IDS, ((Collection<?>) idsCrit.getValue()).toArray());
	}

	/**
	 * @see org.squashtest.tm.plugin.report.std.query.LegacyQueryAdapter#isStandardCriteria(java.lang.String)
	 */
	@Override
	protected boolean isStandardCriteria(String criterionName) {
		return "mode".equals(criterionName);
	}

	/**
	 * @return the legacyQueryProvider
	 */
	public Provider<HibernateRequirementCoverageByTestsQuery> getLegacyQueryProvider() {
		return legacyQueryProvider;
	}
}
