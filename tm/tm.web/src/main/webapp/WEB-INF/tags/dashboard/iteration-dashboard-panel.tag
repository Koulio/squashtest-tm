<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2013 Henix, henix.fr

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU Lesser General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Lesser General Public License for more details.

        You should have received a copy of the GNU Lesser General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ tag language="java" pageEncoding="utf-8" body-content="empty" description="structure of a dashboard for iterations. No javascript."%>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ attribute name="url" required="true" description="url where to get the data" %>
<%@ attribute name="cacheKey" required="false" description="if set, will use the cache using that key" %>


<f:message var="advanceTitle" key="title.CampaignCumulativeAdvancement"/>
<f:message var="statisticsTitle" key="title.IterationStatistics"/>
<f:message var="inventoryTitle" key="title.TestInventoryByIteration"/>
<f:message var="refreshLabel" key="label.Refresh" />

<div id="dashboard-master" data-def="url=${url}">

	<div class="toolbar">
		<span class="dashboard-timestamp not-displayed"><f:message key="dashboard.meta.timestamp.label"/></span> 
		<input type="button" class="dashboard-refresh-button button" role="button" value="${refreshLabel}"/>	
		<input type="button" class="button" role="button" value="I WANT MY ICON !"/>
	</div>
	
	
	<%-- alternate contents : when no data are available we'll display an empty pane, when there are some we'll display the rest. --%>
	
	<div class="dashboard-figleaf">
		
		<div class="dashboard-figleaf-notready" style="text-align : center">
			<h3 class="dashboard-figleaf-notready-title"><f:message key="dashboard.notready.title"/></h3>
		</div>
	
		<div class="dashboard-figleaf-figures not-displayed">

			<%-- second dashboard : campaign statistics --%>
			<comp:toggle-panel id="dashboard-statistics" title="${statisticsTitle}">
				<jsp:attribute name="body">
					
					
					<div id="dashboard-testcase-status" class="dashboard-narrow-item dashboard-pie" data-def="model-attribute=iterationTestCaseStatusStatistics">
						
						<h2 class="dashboard-item-title"><f:message key="dashboard.campaigns.status.title"/></h2>
						
						<div class="dashboard-figures">
							<div id="dashboard-testcase-status-view" class="dashboard-item-view"></div>
						</div>
						
						<div class="dashboard-item-meta">					
							
						
							<div class="dashboard-item-legend">
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#BDD3FF"></div>
									<span><f:message key="execution.execution-status.READY" /></span>
								</div>
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#6699FF"></div>
									<span><f:message key="execution.execution-status.RUNNING" /></span>
								</div>
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#99CC00"></div>
									<span><f:message key="execution.execution-status.SUCCESS" /></span>
								</div>
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#FF3300"></div>
									<span><f:message key="execution.execution-status.FAILURE" /></span>
								</div>
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#FFCC00"></div>
									<span><f:message key="execution.execution-status.BLOCKED" /></span>
								</div>
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#969696"></div>
									<span><f:message key="execution.execution-status.UNTESTABLE" /></span>
								</div>
							</div>
						</div>
					</div>
						
					<div id="dashboard-success-rate" class="dashboard-narrow-item" data-def="model-attribute=iterationTestCaseSuccessRateStatistics">
					
						<h2 class="dashboard-item-title"><f:message key="dashboard.campaigns.successrate.title"/></h2>
											
						<div class="dashboard-figures">
							<div id="dashboard-success-rate-view" class="dashboard-item-view"></div>
						</div>
						
						
						<div class="dashboard-item-meta">					
						
							<div class="dashboard-item-legend">
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#99CC00"></div>
									<span><f:message key="execution.execution-status.SUCCESS" /></span>
								</div>
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#FF3300"></div>
									<span><f:message key="execution.execution-status.FAILURE" /></span>
								</div>
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#969696"></div>
									<span><f:message key="label.Other" /></span>
								</div>
							</div>
						</div>
					</div>
	

					<div id="dashboard-nonexecuted-testcase-importance" class="dashboard-narrow-item dashboard-pie" data-def="model-attribute=iterationNonExecutedTestCaseImportanceStatistics">
						
						<h2 class="dashboard-item-title"><f:message key="dashboard.campaigns.importance.title"/></h2>
						
						<div class="dashboard-figures">
							<div id="dashboard-nonexecuted-testcase-importance-view" class="dashboard-item-view"></div>
						</div>
						
						<div class="dashboard-item-meta">					
							
						
							<div class="dashboard-item-legend">
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#FCEDB6"></div>
									<span><f:message key="test-case.importance.LOW" /></span>
								</div>
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#FBD329"></div>
									<span><f:message key="test-case.importance.MEDIUM" /></span>
								</div>
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#FDA627"></div>
									<span><f:message key="test-case.importance.HIGH" /></span>
								</div>
								<div>
									<div class="dashboard-legend-sample-color" style="background-color:#FD7927"></div>
									<span><f:message key="test-case.importance.VERY_HIGH" /></span>
								</div>
							</div>
						</div>
					</div>
	
				</jsp:attribute>
			</comp:toggle-panel>
			
			<comp:toggle-panel id="test-suite-statistics" title="${statisticsTitle}">
				<jsp:attribute name="body">
				<div class="dashboard-figures">		
					<table id="dashboard-test-inventory" class="dashboard-table" data-def="model-attribute=testsuiteTestInventoryStatisticsList">
						<thead>
							<tr>
								<th colspan="1"></th>
								<th colspan="3"></th>
								<th colspan="11"></th>
								<th colspan="4"></th>
							</tr>
							<tr>				
								<th></th>
								<th></th>
								<th></th>
							    <th></th>
								<th></th>
								<th></th>															    								
								<th></th>
								<th></th>
								<th></th>								
								<th></th>
								<th></th>								
								<th></th>
								<th></th>
								<th></th>
								<th></th>								
								<th></th>
								<th></th>
								<th></th>								
								<th></th>
							</tr>
						</thead>
					
						<tbody>
							<tr class="dashboard-table-template-emptyrow">
								<td colspan="19" class="std-border">No record founds (résultat vide) (internationalise moi ça)</td>
							</tr>
							<tr class="dashboard-table-template-datarow">
								<td class="std-border light-border">{{this.[0]}}</td>
								<td class="std-border light-border">{{this.[1]}}</td>
								<td class="std-border light-border">{{this.[2]}}</td>
								<td class="std-border light-border">{{this.[3]}}</td>								
								<td class="std-border light-border">{{this.[4]}}</td>
								<td class="std-border light-border">{{this.[5]}}</td>
								<td class="std-border light-border">{{this.[6]}}</td>
								<td class="std-border light-border">{{this.[7]}}</td>
								<td class="std-border light-border">{{this.[8]}}</td>
								<td class="std-border light-border">{{this.[9]}}</td>
								<td class="std-border light-border">{{this.[10]}}</td>								
								<td class="std-border light-border">{{this.[11]}}</td>
								<td class="std-border light-border">{{this.[12]}}</td>
								<td class="std-border light-border">{{this.[13]}}</td>
								<td class="std-border light-border">{{this.[14]}}</td>
								<td class="std-border light-border">{{this.[15]}}</td>								
								<td class="std-border light-border">{{this.[16]}}</td>
								<td class="std-border light-border">{{this.[17]}}</td>
								<td class="std-border light-border">{{this.[18]}}</td>
							</tr>
						</tbody>			
					</table>		
				</div>
				</jsp:attribute>
			</comp:toggle-panel>
		</div>
	
	</div>
</div>