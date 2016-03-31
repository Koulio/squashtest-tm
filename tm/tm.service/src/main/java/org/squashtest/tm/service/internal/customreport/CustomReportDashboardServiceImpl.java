/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.service.internal.customreport;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.customreport.CustomReportChartBinding;
import org.squashtest.tm.domain.customreport.CustomReportDashboard;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.users.PartyPreference;
import org.squashtest.tm.domain.users.preferences.CorePartyPreference;
import org.squashtest.tm.domain.users.preferences.HomeContentValues;
import org.squashtest.tm.service.customreport.CustomReportDashboardService;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.service.internal.repository.CustomReportChartBindingDao;
import org.squashtest.tm.service.internal.repository.CustomReportDashboardDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.user.PartyPreferenceService;

@Service("org.squashtest.tm.service.customreport.CustomReportDashboardService")
public class CustomReportDashboardServiceImpl implements
		CustomReportDashboardService {

	@Inject
	CustomReportDashboardDao customReportDashboardDao;

	@Inject
	CustomReportChartBindingDao bindingDao;

	@Inject
	CustomReportLibraryNodeService crlnService;

	@Inject
	private SessionFactory sessionFactory;

	@Inject
	protected PermissionEvaluationService permissionService;

	@Inject
	private PartyPreferenceService partyPreferenceService;

	@Override
	public CustomReportDashboard findById(Long id) {
		return customReportDashboardDao.findById(id);
	}

	@Override
	@PreAuthorize("hasPermission(#newBinding.dashboard.id, 'org.squashtest.tm.domain.customreport.CustomReportDashboard' ,'WRITE') "
			+ OR_HAS_ROLE_ADMIN)
	public void bindChart(CustomReportChartBinding newBinding) {
		bindingDao.persist(newBinding);
		sessionFactory.getCurrentSession().flush();
	}

	@Override
	public void updateGridPosition(List<CustomReportChartBinding> transientBindings) {
		for (CustomReportChartBinding transientBinding : transientBindings) {
			updateBinding(transientBinding);
		}

	}

	private void updateBinding(CustomReportChartBinding transientBinding) {
		CustomReportChartBinding persistedBinding = bindingDao.findById(transientBinding.getId());
		if(permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN","WRITE",persistedBinding.getDashboard())
				&& persistedBinding.hasMoved(transientBinding)){
			persistedBinding.move(transientBinding);
		}
	}

	@Override
	@PreAuthorize("hasPermission(#id, 'org.squashtest.tm.domain.customreport.CustomReportChartBinding' ,'WRITE') "
			+ OR_HAS_ROLE_ADMIN)
	public void unbindChart(Long id) {
		CustomReportChartBinding chartBinding = bindingDao.findById(id);
		bindingDao.remove(chartBinding);
	}

	@Override
	@PreAuthorize("hasPermission(#bindingId, 'org.squashtest.tm.domain.customreport.CustomReportChartBinding' ,'WRITE') "
			+ OR_HAS_ROLE_ADMIN)
	public CustomReportChartBinding changeBindedChart(long bindingId,
			long chartNodeId) {
		CustomReportChartBinding chartBinding = bindingDao.findById(bindingId);
		ChartDefinition chartDefinition = crlnService.findChartDefinitionByNodeId(chartNodeId);
		chartBinding.setChart(chartDefinition);
		return chartBinding;
	}

	@Override
	public void chooseFavoriteDashboardForCurrentUser(long nodeId) {
		CustomReportLibraryNode node = crlnService.findCustomReportLibraryNodeById(nodeId);
		if (node != null){
			partyPreferenceService.addOrUpdatePreferenceForCurrentUser(
				CorePartyPreference.FAVORITE_DASHBOARD.getPreferenceKey(),String.valueOf(node.getId()));
		}
	}

	@Override
	public boolean shouldShowDashboardOnHomePage() {
		String key = CorePartyPreference.HOME_WORKSPACE_CONTENT.getPreferenceKey();
		PartyPreference pref = partyPreferenceService.findPreferenceForCurrentUser(key);
		if (pref == null){
			return false;
		}
		String content = pref.getPreferenceValue();
		if (StringUtils.isEmpty(content) || content.equals(HomeContentValues.MESSAGE.getPreferenceValue())){
			return false;
		}
		if (content.equals(HomeContentValues.DASHBOARD.getPreferenceValue())) {
			return true;
		}
		return false;
	}

	@Override
	public boolean canShowDashboardOnHomePage() {
		String key = CorePartyPreference.FAVORITE_DASHBOARD.getPreferenceKey();
		PartyPreference pref = partyPreferenceService.findPreferenceForCurrentUser(key);
		if (pref == null){
			return false;
		}
		String candidateDashboardId = pref.getPreferenceValue();
		if (StringUtils.isEmpty(candidateDashboardId)){
			return false;
		}
		Long dashboardId = Long.parseLong(candidateDashboardId);
		CustomReportLibraryNode node = crlnService.findCustomReportLibraryNodeById(dashboardId);
		return node != null;
	}
}
