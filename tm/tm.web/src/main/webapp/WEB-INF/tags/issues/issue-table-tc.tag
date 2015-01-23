<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2014 Henix, henix.fr

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
<%@ tag description="Table displaying the issues for a TestCase" body-content="empty" %>
	
<%@ tag language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>	

<%@ attribute name="interfaceDescriptor" type="java.lang.Object" required="true" description="an object holding the labels for the interface"%>
<%@ attribute name="dataUrl" required="true" description="where the table will fetch its data" %>
<%@ attribute name="tableEntries" required="false" type="java.util.List" description="if set, must be valid aaData for datatables. Will then defer the ajax loading of the table." %>

<c:url var="executionUrl" value="/executions/"/>
<c:url var="tableLanguageUrl" value="/datatables/messages" />
<%-- 
	columns are :
	
		- URL  (not shown)
		- ID
		- owner
		- Priority
		- Summary
		- Status
		- Assignation

 --%>

<table id="issue-table" data-def="ajaxsource=${dataUrl}, datakeys-id=id, pre-sort=1-desc">
	<thead>
		<tr>
			<th data-def="select, map=remote-id, link={url}, sWidth=2.5em, sortable">${interfaceDescriptor.tableIssueIDHeader}</th>
			<th data-def="map=summary">${interfaceDescriptor.tableSummaryHeader}</th>
			<th data-def="map=priority">${interfaceDescriptor.tablePriorityHeader}</th>
			<th data-def="map=status">${interfaceDescriptor.tableStatusHeader}</th>
			<th data-def="map=assignee">${interfaceDescriptor.tableAssigneeHeader}</th>
			<th data-def="map=execution, link=${executionUrl}/{execution-id}"><f:message key="test-case.issues.table.column-header.reportedin.label" /></th>
		</tr>
	</thead>
	<tbody><%-- Will be populated through ajax --%></tbody>
</table>

<script type="text/javascript">
require( ["common"], function(){
		require(["jquery","squashtable"], function($){
	$(function(){
			$("#issue-table").squashTable({
				fnRowCallback : function(row, data){
					var correctAssignee = (data["assignee"]!=="") ? data["assignee"] : "${interfaceDescriptor.tableNoAssigneeLabel}";
					var td=$(row).find("td:eq(4)");
					$(td).html(correctAssignee);
					return row;				
				},
				<c:if test="${not empty tableEntries}">
				'aaData' : ${json:serialize(tableEntries)},
				'iDeferLoading' : ${fn:length(tableEntries)}
				</c:if>
			},
			{});
		});
	});
});
</script>
