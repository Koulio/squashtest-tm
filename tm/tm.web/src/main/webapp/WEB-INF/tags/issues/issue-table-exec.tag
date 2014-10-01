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
<%@ tag description="Table displaying the issues for an ExecutionStep" body-content="empty" %>

<%@ tag language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>	

<%@ attribute name="interfaceDescriptor" type="java.lang.Object" required="true" description="an object holding the labels for the interface" %>
<%@ attribute name="dataUrl" required="true" description="where the table will fetch its data" %>
<%@ attribute name="bugTrackerUrl" required="true" description="where the delete buttons send the delete instruction" %>
<%@ attribute name="entityId" required="true" description="id of the current execution" %>
<%@ attribute name="freeSettings" required="true" description="added settings to issue table" %>
<%@ attribute name="executable" required="true" description="if the user has EXECUTE rights on the execution" %>
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

<script type="text/javascript">
	
	function refreshTestPlan() {
		$('#issue-table').squashTable().refresh();
	}

	function issueTableRowCallback(row, data, displayIndex) {
		checkEmptyValues(row, data);
		return row;
	}
	
	<%-- we check the assignee only (for now) --%>
	function checkEmptyValues(row, data){
		var assignee = data['assignee'];
		var correctAssignee = (assignee!=="") ? assignee : "${interfaceDescriptor.tableNoAssigneeLabel}"; 
		var td=$(row).find("td:eq(4)");
		$(td).html(correctAssignee);
	}
	
	/* ************************** datatable settings ********************* */


	$(function() {
		
		var tableSettings = { 
				"aaSorting" : [[0,'desc']],
				"fnRowCallback" : issueTableRowCallback
			};		
		
			var squashSettings = {
				enableDnD : false,
				deleteButtons : {
					url : '${bugTrackerUrl}/issues/{local-id}',
					popupmessage : '<f:message key="dialog.remove-testcase-association.message" />',
					tooltip : '<f:message key="test-case.verified_requirement_item.remove.button.label" />',
					success : function(data) {
						refreshTestPlan();
					}
				}
			};

			$("#issue-table").squashTable(tableSettings, squashSettings);
	});
	
</script>
	
<c:url value='/datatables/messages' var="tableLangUrl" />
<c:if test="${executable}">
	<c:set var="deleteBtnClause" value=", sClass=delete-button"/>
</c:if>
<table id="issue-table" data-def="ajaxsource=${dataUrl}">
	<thead >
		<tr>
			<th data-def="map=remote-id, link={issue-url}, center, select, double-narrow">${interfaceDescriptor.tableIssueIDHeader}</th>
			<th data-def="map=summary">${interfaceDescriptor.tableSummaryHeader}</th>
			<th data-def="map=priority">${interfaceDescriptor.tablePriorityHeader}</th>
			<th data-def="map=status">${interfaceDescriptor.tableStatusHeader}</th>
			<th data-def="map=assignee">${interfaceDescriptor.tableAssigneeHeader}</th>
			<th data-def="map=owner">${interfaceDescriptor.tableReportedInHeader}</th>
			<th data-def="map=empty-delete-holder, narrow, center${deleteBtnClause}"></th>
		</tr>
	</thead>
	<tbody><%-- Will be populated through ajax --%></tbody>
</table>



