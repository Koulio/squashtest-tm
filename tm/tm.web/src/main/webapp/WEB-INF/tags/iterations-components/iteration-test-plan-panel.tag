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
<%@ tag body-content="empty" description="the test plan panel for an iteration"%>


<%@ attribute name="linkable"  type="java.lang.Boolean" description="can the user link this iteration to test cases ?" %>
<%@ attribute name="editable"  type="java.lang.Boolean" description="can the user modify the existing test plan items ?" %>
<%@ attribute name="executable"type="java.lang.Boolean" description="can the user execute the test plan ?" %>

<%@ attribute name="assignableUsers" type="java.lang.Object" description="a map of users paired by id -> login. The id must be a string."%>
<%@ attribute name="iteration" type="java.lang.Object" description="the instance of iteration" %>


<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>

<s:url var="dtMessagesUrl" value="/datatables/messages" />
<s:url var="tableModelUrl" value="/iterations/{iterId}/test-plan" >
	<s:param name="iterId" value="${iteration.id}"/>
</s:url>


<f:message var="cannotCreateExecutionException" key="squashtm.action.exception.cannotcreateexecution.label" />
<f:message var="unauthorizedDeletion" key="dialog.remove-testcase-association.unauthorized-deletion.message" />
<f:message var="confirmLabel"	key="label.Confirm"/>
<f:message var="cancelLabel"	key="label.Cancel"/>
<f:message var="assignLabel"	key="label.Assign" />



<div id="iteration-test-plans-panel" class="table-tab">

<%-- ==================== THE TOOLBAR ==================== --%>

<div class="toolbar">
	<c:if test="${ linkable }">
		<f:message var="associateLabel" 	key="label.Add" />
		<f:message var="removeLabel" 		key="label.Remove" />
		<f:message var="assignLabel"		key="label.Assign" />
		<f:message var="manageTS" 			key='menu.test-suites.button.main' />
		
		<input id="navigate-test-plan-manager"	type="button" value="${associateLabel}" class="button" />
		<input id="remove-test-plan-button" type="button" value="${removeLabel}" class="button" />
		<input id="assign-users-button" 	type="button" value="${assignLabel}" class="button" />
		<input id="manage-test-suites-menu" type="button" value="${manageTS}" class="button" />
	</c:if>
</div>

<%-- ===================== THE TABLE ===================== --%>
<div class="table-tab-wrap">
<c:if test="${editable}">
	<c:set var="deleteBtnClause" value=", delete-button=#iter-test-plan-delete-dialog"/>
</c:if>
<table id="iteration-test-plans-table" class="test-plan-table" data-def="language=${dtMessagesUrl}, ajaxsource=${tableModelUrl}, hover"  >
	<thead>
		<tr>
			<th data-def="map=entity-index, select, sortable, center, sClass=drag-handle, sWidth=2.5em">#</th>
			<th data-def="map=project-name, sortable"><f:message key="label.project" /></th>
			<th data-def="map=exec-mode, sortable, narrow, sClass=exec-mode">&nbsp;</th><%-- exec mode icon --%>
			<th data-def="map=reference, sortable"><f:message key="label.Reference"/></th>
			<th data-def="map=tc-name, sortable, sClass=toggle-row"><f:message key="iteration.executions.table.column-header.test-case.label" /></th>
			<th data-def="map=importance, sortable"><f:message key="iteration.executions.table.column-header.importance.label" /></th>
			<th data-def="map=dataset, sortable, sWidth=10%"><f:message key="label.Dataset" /></th>
			<th data-def="map=suite, sWidth=10%"><f:message key="iteration.executions.table.column-header.suite.label" /></th>
			<th data-def="map=status, sortable, sWidth=10%, sClass=has-status status-combo"><f:message key="iteration.executions.table.column-header.status.label" /></th>
			<th data-def="map=assignee-login, sortable, sWidth=10%, sClass=assignee-combo"><f:message key="iteration.executions.table.column-header.user.label" /></th>
			<th data-def="map=last-exec-on, sortable, sWidth=10%"><f:message key="iteration.executions.table.column-header.execution-date.label" /></th>
			<th data-def="map=empty-execute-holder, narrow, center, sClass=execute-button">&nbsp;</th>	
			<th data-def="map=empty-delete-holder ${deleteBtnClause}">&nbsp;</th>				
		</tr>
	</thead>
	<tbody><%-- Will be populated through ajax --%></tbody>
</table>

<div id="shortcut-exec-menu-template" class="not-displayed">
		<div class="buttonmenu execute-arrow cursor-pointer"></div> 
		<ul style="display:none">
			<li><a data-tpid="#placeholder-tpid#" class="run-menu-item run-popup" href="javascript:void(0)"><f:message key="test-suite.execution.classic.label"/></a></li>
			<li><a data-tpid="#placeholder-tpid#" class="run-menu-item run-oer" href="javascript:void(0)"><f:message key="test-suite.execution.optimized.label"/></a></li>
		</ul>
</div>


<%-- ============================== THE DIALOGS ========================= --%>


<div id="iter-test-plan-delete-dialog" class="not-displayed popup-dialog" title="<f:message key="test-case.verified_requirement_item.remove.button.label" />">
	<span data-def="state=single-tp" 	style="font-weight:bold;"><f:message key="dialog.remove-testcase-association.message" /></span>
	<span data-def="state=multiple-tp" 	style="font-weight:bold;"><f:message key="dialog.remove-testcase-associations.message" /></span>
	<span data-def="state=empty-selec"><f:message key="message.EmptyTableSelection"/></span>
	
	<div class="popup-dialog-buttonpane"> 
		<input type="button" value="${confirmLabel}" data-def="state=single-tp multiple-tp, mainbtn=single-tp multiple-tp, evt=confirm"/> 
		<input type="button" value="${cancelLabel}" data-def="mainbtn=empty-selec, evt=cancel"/> 
	</div>
</div>

<div id="iter-test-plan-delete-execution-dialog" class="not-displayed popup-dialog" title="<f:message key="dialog.delete-execution.title" />">
	<span style="font-weight:bold;"><f:message key="dialog.delete-execution.message" /></span>
	<div class="popup-dialog-buttonpane"> 
		<input type="button" value="${confirmLabel}"/> 
		<input type="button" value="${cancelLabel}"/> 
	</div>
</div>

<div id="iter-test-plan-batch-assign" class="not-displayed popup-dialog" title="<f:message key="label.AssignUser"/>">
	<div data-def="state=assign">
		<span><f:message key="message.AssignTestCaseToUser"/></span>
		<select class="batch-select">
			<c:forEach var="user" items="${assignableUsers}">
			<option value="${user.key}">${user.value}</option>		
			</c:forEach>
		</select>
	</div>	
	<span data-def="state=empty-selec"><f:message key="message.EmptyTableSelection"/></span>
	
	<div class="popup-dialog-buttonpane"> 
		<input type="button" value="${assignLabel}" data-def="state=assign, mainbtn=assign, evt=confirm"/>
		<input type="button" value="${cancelLabel}" data-def="mainbtn=empty-select, evt=cancel"/>
	</div>
</div>

</div>
</div> <!-- /test plan panel end -->

<script type="text/javascript">
	$(function() {

		require(['iteration-management'], function(iterInit){
			var conf = {
				permissions : {
					linkable : ${linkable},
					editable : ${editable},
					executable : ${executable}
				},
				basic : {
					iterationId : ${iteration.id},
					assignableUsers : ${ json:serialize(assignableUsers) }
				}
			};
			
			iterInit.initTestPlanPanel(conf);
		});
					
			
	});
</script>

