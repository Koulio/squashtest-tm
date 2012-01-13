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
package org.squashtest.csp.tm.internal.repository.hibernate;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.requirement.ExportRequirementData;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.domain.requirement.RequirementSearchCriteria;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.domain.requirement.VerificationCriterion;
import org.squashtest.csp.tm.domain.resource.Resource;
import org.squashtest.csp.tm.internal.repository.RequirementDao;

@Repository
public class HibernateRequirementDao extends HibernateEntityDao<Requirement> implements RequirementDao {
	private static final Map<VerificationCriterion, Object[]> HIBERNATE_RESTRICTION_BY_VERIFICATION_CRITERION = new HashMap<VerificationCriterion, Object[]>(
			VerificationCriterion.values().length);

	static {
		HIBERNATE_RESTRICTION_BY_VERIFICATION_CRITERION.put(VerificationCriterion.ANY, new Object[] { Resource.class,
				null }); // yeah, it's a null.
		HIBERNATE_RESTRICTION_BY_VERIFICATION_CRITERION.put(VerificationCriterion.SHOULD_BE_VERIFIED, new Object[] {
				RequirementVersion.class, Restrictions.isNotEmpty("verifyingTestCases") });
		HIBERNATE_RESTRICTION_BY_VERIFICATION_CRITERION.put(VerificationCriterion.SHOULD_NOT_BE_VERIFIED, new Object[] {
				RequirementVersion.class, Restrictions.isEmpty("verifyingTestCases") });

	}

	@Override
	public List<Requirement> findAllByIdList(final List<Long> requirementsIds) {
		if (!requirementsIds.isEmpty()) {
			SetQueryParametersCallback setParams = new SetQueryParametersCallback() {
				@Override
				public void setQueryParameters(Query query) {
					query.setParameterList("requirementsIds", requirementsIds);
				}
			};
			return executeListNamedQuery("requirement.findAllByIdList", setParams);

		} else {
			return Collections.emptyList();

		}

	}

	@Override
	public List<String> findNamesInFolderStartingWith(final long folderId, final String nameStart) {
		SetQueryParametersCallback newCallBack1 = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("containerId", folderId);
				query.setParameter("nameStart", nameStart + "%");
			}
		};
		return executeListNamedQuery("requirement.findNamesInFolderStartingWith", newCallBack1);
	}

	@Override
	public List<String> findNamesInLibraryStartingWith(final long libraryId, final String nameStart) {
		SetQueryParametersCallback callBack = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("containerId", libraryId);
				query.setParameter("nameStart", nameStart + "%");
			}
		};

		return executeListNamedQuery("requirement.findNamesInLibraryStartingWith", callBack);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<RequirementLibraryNode> findAllBySearchCriteria(RequirementSearchCriteria searchCriteria) {
		DetachedCriteria crit = createCriteria(searchCriteria);

		crit.addOrder(Order.asc("res.name"));

		return crit.getExecutableCriteria(currentSession()).list();
	}

	private DetachedCriteria createCriteria(RequirementSearchCriteria searchCriteria) {
		DetachedCriteria versionCriteria = DetachedCriteria.forClass(RequirementLibraryNode.class);
		versionCriteria.createCriteria("resource", "res");

		if (StringUtils.isNotBlank(searchCriteria.getName())) {
			versionCriteria.add(Restrictions.ilike("res.name", searchCriteria.getName(), MatchMode.ANYWHERE));
		}
		if (StringUtils.isNotBlank(searchCriteria.getReference())) {
			versionCriteria.add(Restrictions.ilike("res.reference", searchCriteria.getReference(), MatchMode.ANYWHERE));
		}
		if (!searchCriteria.getCriticalities().isEmpty()) {
			versionCriteria.add(Restrictions.in("res.criticality", searchCriteria.getCriticalities()));
		}

		addVerificationRestriction(searchCriteria, versionCriteria);

		return versionCriteria;
	}

	Class<?> getCriteriaClass(RequirementSearchCriteria searchCriteria) {
		return (Class<?>) HIBERNATE_RESTRICTION_BY_VERIFICATION_CRITERION
				.get(searchCriteria.getVerificationCriterion())[0];
	}

	private void addVerificationRestriction(RequirementSearchCriteria searchCriteria, DetachedCriteria criteria) {
		Criterion restriction = (Criterion) HIBERNATE_RESTRICTION_BY_VERIFICATION_CRITERION.get(searchCriteria
				.getVerificationCriterion())[1];

		if (restriction != null) {
			criteria.add(restriction);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<RequirementLibraryNode> findAllBySearchCriteriaOrderByProject(RequirementSearchCriteria searchCriteria) {
		DetachedCriteria crit = createCriteria(searchCriteria);

		crit.createAlias("project", "p").addOrder(Order.asc("p.name"));
		crit.addOrder(Order.asc("name"));

		return crit.getExecutableCriteria(currentSession()).list();
	}

	@Override
	public List<ExportRequirementData> findRequirementToExportFromFolder(List<Long> params) {
		if (!params.isEmpty()) {

			List<Object[]> listObject;
			List<Long> listReqNodeId;
			List<Long> reqIds;

			List<Object> rootReqs = findRootContentRequirement(params);
			listReqNodeId = findRequirementIds(params);
			if (listReqNodeId == null || listReqNodeId.isEmpty()) {
				return formatExportResult(addRootContentToExportData(rootReqs, new ArrayList<Object[]>()));
			}
			reqIds = findAllRequirementInExportData(listReqNodeId);
			listObject = findRequirementExportData(reqIds);

			if (!rootReqs.isEmpty()) {
				listObject = addRootContentToExportData(rootReqs, listObject);
			}

			return formatExportResult(listObject);

		} else {
			return Collections.emptyList();

		}
	}

	@SuppressWarnings("unchecked")
	private List<Long> findRequirementIds(List<Long> params) {
		if (!params.isEmpty()) {
			Session session = currentSession();
			String sql = "select DESCENDANT_ID from RLN_RELATIONSHIP where ANCESTOR_ID in (:list)";

			List<BigInteger> list;
			List<Long> result = new ArrayList<Long>();
			result.addAll(params); // the inputs are also part of the output.
			List<Long> local = params;

			do {
				Query sqlQuery = session.createSQLQuery(sql);
				sqlQuery.setParameterList("list", local, new LongType());
				list = sqlQuery.list();
				if (!list.isEmpty()) {
					local.clear();
					for (BigInteger bint : list) {
						local.add(bint.longValue());
						result.add(bint.longValue());
					}
				}
			} while (!list.isEmpty());
			if (result.isEmpty()) {
				return null;
			}
			return result;

		} else {
			return Collections.emptyList();

		}

	}

	private List<Object[]> findRequirementExportData(final List<Long> requirementIds) {
		if (!requirementIds.isEmpty()) {
			SetQueryParametersCallback newCallBack1 = new SetQueryParametersCallback() {
				@Override
				public void setQueryParameters(Query query) {
					query.setParameterList("rIds", requirementIds, new LongType());
				}
			};

			return executeListNamedQuery("requirement.findRequirementExportData", newCallBack1);

		} else {
			return Collections.emptyList();
		}
	}

	private List<Long> findAllRequirementInExportData(final List<Long> requirementIds) {
		if (!requirementIds.isEmpty()) {
			SetQueryParametersCallback newCallBack1 = new SetQueryParametersCallback() {
				@Override
				public void setQueryParameters(Query query) {
					query.setParameterList("rIds", requirementIds, new LongType());
				}
			};

			List<Object> resultList = executeListNamedQuery("requirement.findRequirementInExportData", newCallBack1);
			List<Long> reqIds = new ArrayList<Long>(resultList.size());

			for (Object obj : resultList) {
				reqIds.add(Long.parseLong(obj.toString()));
			}
			return reqIds;
		} else {
			return Collections.emptyList();
		}
	}

	private List<ExportRequirementData> formatExportResult(List<Object[]> list) {
		if (!list.isEmpty()) {
			List<ExportRequirementData> exportList = new ArrayList<ExportRequirementData>();

			for (Object[] tuple : list) {
				Requirement req = (Requirement) tuple[0];
				String folder = (String) tuple[1];
				ExportRequirementData erd = new ExportRequirementData(req, folder);
				exportList.add(erd);
			}

			return exportList;
		} else {
			return Collections.emptyList();
		}
	}

	private List<Object> findRootContentRequirement(final List<Long> params) {
		if (!params.isEmpty()) {
			SetQueryParametersCallback newCallBack1 = new SetQueryParametersCallback() {
				@Override
				public void setQueryParameters(Query query) {
					query.setParameterList("paramIds", params, new LongType());
				}
			};
			return executeListNamedQuery("requirement.findRootContentRequirement", newCallBack1);

		} else {
			return Collections.emptyList();

		}
	}

	@Override
	public List<ExportRequirementData> findRequirementToExportFromLibrary(final List<Long> libIds) {

		if (!libIds.isEmpty()) {
			SetQueryParametersCallback newCallBack1 = new SetQueryParametersCallback() {
				@Override
				public void setQueryParameters(Query query) {
					query.setParameterList("rIds", libIds, new LongType());
				}
			};
			List<Long> result = executeListNamedQuery("requirement.findAllRootContent", newCallBack1);

			return findRequirementToExportFromFolder(result);
		} else {
			return Collections.emptyList();
		}
	}

	private List<Object[]> addRootContentToExportData(List<Object> rootRequirement, List<Object[]> folderContent) {
		for (Object obj : rootRequirement) {
			Requirement objReq = (Requirement) obj;
			Object[] tab = { objReq, objReq.getProject().getName() };
			folderContent.add(tab);
		}
		return folderContent;
	}

}
