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
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="camp" tagdir="/WEB-INF/tags/campaigns-components"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>

<f:message var="squashlocale" key="squashtm.locale" />

<comp:datepicker-manager locale="${squashlocale}" />

<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<c:url var="campaignUrl" value="/campaigns/${campaign.id}" />

<s:url var="campaignInfoUrl" value="/campaigns/{campId}/general">
	<s:param name="campId" value="${campaign.id}" />
</s:url>
<s:url var="campaignPlanningUrl" value="/campaigns/{campId}/planning">
	<s:param name="campId" value="${campaign.id}" />
</s:url>
<s:url var="assignableUsersUrl"
	value="/campaigns/{campId}/assignable-users">
	<s:param name="campId" value="${campaign.id}" />
</s:url>
<s:url var="campaignStatisticsUrl" value="/campaigns/{campId}/statistics">
	<s:param name="campId" value="${campaign.id }"/>
</s:url>
<s:url var="assignTestCasesUrl"
	value="/campaigns/${ campaign.id }/batch-assign-user" />

<c:url var="testCaseManagerUrl"
	value="/campaigns/${ campaign.id }/test-plan/manager" />

<c:url var="workspaceUrl" value="/campaign-workspace/#" />

<s:url var="btEntityUrl" value="/bugtracker/campaign/{id}">
	<s:param name="id" value="${campaign.id}" />
</s:url>

<c:url var="customFieldsValuesURL" value="/custom-fields/values" />

<%-- ----------------------------------- Authorization ----------------------------------------------%>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE"
	domainObject="${ campaign }">
	<c:set var="writable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="ATTACH" domainObject="${ campaign }">
	<c:set var="attachable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>

<authz:authorized hasRole="ROLE_ADMIN" hasPermission="DELETE" domainObject="${ campaign }">
	<c:set var="deletable" value="${true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="CREATE" domainObject="${ campaign }">
	<c:set var="creatable" value="${true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="LINK"	domainObject="${ campaign }">
	<c:set var="linkable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="LINK"	domainObject="${ campaign }">
	<c:set var="linkable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>



<div
	class="ui-widget-header ui-state-default ui-corner-all fragment-header">

	<div style="float: left; height: 100%;">
		<h2>
			<span><f:message key="label.Campaign" />&nbsp;:&nbsp;</span><a
				id="campaign-name" href="${ campaignUrl }/info"><c:out
					value="${ campaign.name }" escapeXml="true" />
			</a>
		</h2>
	</div>

	<div style="clear: both;"></div>
	<c:if test="${writable}">
		<pop:popup id="rename-campaign-dialog"
			titleKey="dialog.rename-campaign.title" isContextual="true"
			openedBy="rename-campaign-button">
			<jsp:attribute name="buttons">
			
				<f:message var="label" key="dialog.rename-campaign.title" />
				'${ label }': function() {
					var url = "${ campaignUrl }";
					$.ajax({
						url : url,
						dataType : 'json', 
						type : 'post', 
						data : { newName : $("#rename-campaign-name").val() }
					}).done(renameCampaignSuccess);
				},			
				<pop:cancel-button />
			</jsp:attribute>
			<jsp:attribute name="body">
				<script type="text/javascript">
					$("#rename-campaign-dialog").bind("dialogopen",
							function(event, ui) {
								var name = $.trim($('#campaign-name').text());
								$("#rename-campaign-name").val(name);

							}
					);
				</script>			
				<label><f:message key="dialog.rename.label" />
				</label>
				<input type="text" id="rename-campaign-name" maxlength="255" size="50" />
				<br />
				<comp:error-message forField="name" />	
		
			</jsp:attribute>
		</pop:popup>
	</c:if>
</div>



<div id="campaign-toolbar" class="toolbar-class ui-corner-all ">
	<div class="toolbar-information-panel">
		<comp:general-information-panel auditableEntity="${campaign}" />
	</div>
	<div class="toolbar-button-panel">
	
		
		<c:if test="${ writable }">
			<input type="button"
				value='<f:message key="label.Rename" />'
				id="rename-campaign-button" />
		</c:if>

		
		<c:if test="${ deletable }">
			<input type="button"
				value='<f:message key="label.Remove" />'
				id="delete-campaign-button" />
		</c:if>
	</div>
	<div style="clear: both;"></div>
	<c:if test="${ moreThanReadOnly }">
		<comp:opened-object otherViewers="${ otherViewers }"
			objectUrl="${ campaignUrl }" isContextual="${ ! param.isInfoPage }" />
	</c:if>
</div>



<div class="fragment-tabs fragment-body">
	<ul>
		<li><a href="#tabs-1"><f:message key="tabs.label.information" />
		</a>
		</li>
		<li><a href="#tabs-2"><f:message key="tabs.label.test-plan" />
		</a>
		</li>
		<li><a href="#tabs-3"><f:message key="label.Attachments" />
				<c:if test="${ campaign.attachmentList.notEmpty }">
					<span class="hasAttach">!</span>
				</c:if>
		</a>
		</li>
	</ul>
	<div id="tabs-1">
		<c:if test="${ writable }">
			<comp:rich-jeditable targetUrl="${ campaignUrl }"
				componentId="campaign-description" />
		</c:if>

		<script type="text/javascript">
		require([ "common" ], function () {
			require([ "jquery", "domReady", "jqueryui" ], function ($, domReady) {
				/* simple initialization for simple components */
				domReady(function() {
					$("#").button().click(function() {
						$("#campaign-description").html('');
						return false;
					});
				});
			});
		});
		</script>

		<comp:toggle-panel id="campaign-description-panel"
			classes="information-panel" titleKey="label.Description"
			isContextual="true" open="true">
			<jsp:attribute name="body">
				<div id="campaign-description">${ campaign.description }</div>
			</jsp:attribute>
		</comp:toggle-panel>
		
		
		<%----------------------------------- Custom Fields -----------------------------------------------%>
		
		<comp:toggle-panel id="campaign-custom-fields" 
			titleKey="generics.customfieldvalues.title" isContextual="true"	open="${hasCUF}">
			<jsp:attribute name="body">
				<div id="campaign-custom-fields-content" class="display-table">
				<div class="waiting-loading minimal-height"></div>
				</div>
			</jsp:attribute>
		</comp:toggle-panel>
		
		


		<%--------------------------- Planning section ------------------------------------%>
		<comp:toggle-panel id="datepicker-panel"
			titleKey="label.Planning" isContextual="true"
			open="true">
			<jsp:attribute name="body">
	<div class="datepicker-panel">
		<table class="datepicker-table">
			<tr>
				<td class="datepicker-table-col">
					<comp:datepicker
									fmtLabel="dialog.label.campaign.scheduled_start.label"
									url="${campaignPlanningUrl}" datePickerId="scheduled-start"
									paramName="scheduledStart" isContextual="true"
									initialDate="${campaign.scheduledStartDate.time}"
									editable="${ writable }">	
					</comp:datepicker>
				</td>
				<td class="datepicker-table-col">
					<comp:datepicker-auto datePickerId="actual-start"
									url="${campaignPlanningUrl}"
									fmtLabel="dialog.label.campaign.actual_start.label"
									paramName="actualStart" autosetParamName="setActualStartAuto"
									isAuto="${campaign.actualStartAuto}"
									initialDate="${campaign.actualStartDate.time}"
									isContextual="true" editable="${ writable }">
					</comp:datepicker-auto>
				</td>
			</tr>
			<tr>
				<td class="datepicker-table-col">
					<comp:datepicker
									fmtLabel="dialog.label.campaign.scheduled_end.label"
									url="${campaignPlanningUrl}" datePickerId="scheduled-end"
									paramName="scheduledEnd" isContextual="true"
									initialDate="${campaign.scheduledEndDate.time}"
									editable="${ writable }">	
					</comp:datepicker>				
				</td>
				<td class="datepicker-table-col">
					<comp:datepicker-auto datePickerId="actual-end"
									url="${campaignPlanningUrl}"
									fmtLabel="dialog.label.campaign.actual_end.label"
									paramName="actualEnd" autosetParamName="setActualEndAuto"
									isAuto="${campaign.actualEndAuto}"
									initialDate="${campaign.actualEndDate.time}"
									isContextual="true" editable="${ writable }">
					</comp:datepicker-auto>
				</td>
			</tr>
		</table>
	</div>
	</jsp:attribute>
		</comp:toggle-panel>
		<%--------------------------- /Planning section ------------------------------------%>
		<%-- ------------------ statistiques --------------------------- --%>
		<comp:statistics-panel statisticsEntity="${ statistics }" statisticsUrl="${ campaignStatisticsUrl }"/>
		<%-- ------------------ /statistiques --------------------------- --%>
	</div>
	<div id="tabs-2" class="table-tab">

		<%--------------------------- Test plan section ------------------------------------%>
		<script type="text/javascript">
		require([ "common" ], function () {
			require([ "jquery", "domReady", "jqueryui" ], function ($, domReady) {
				/* simple initialization for simple components */
				domReady(function() {
					$("#test-case-button").button().click(function() {
						document.location.href = "${testCaseManagerUrl}";
					});
					$("#remove-test-case-button").button();
					
					<c:if test="${hasCUF}">
					<%-- loading the custom field panel --%>
					$("#campaign-custom-fields-content").load("${customFieldsValuesURL}?boundEntityId=${campaign.boundEntityId}&boundEntityType=${campaign.boundEntityType}");
					</c:if>			    	
				});
			});
		});
		</script>

		<div class="toolbar">
			<f:message var="associateLabel"	key="label.Add" />
			<f:message var="removeLabel" key="label.Remove" />
			<f:message var="assignLabel" key="label.Assign" />			
			<f:message var="reorderLabel" key="label.Reorder" />
			<f:message var="reorderTooltip"	 key="tooltips.ReorderTestPlan" />
			
			<c:if test="${ linkable }">
				<input id="test-case-button" type="button" value="${associateLabel}" class="button" />
			</c:if>
			<c:if test="${ writable }">
				<input id="reorder-test-plan-button"	type="button" value="${reorderLabel}" 	class="button" title="${reorderTooltip}"/>
			</c:if>
			<c:if test="${ linkable }">
				<input id="remove-test-case-button" type="button"
					value="${removeLabel}" class="button" />
				<input id="assign-test-case-button" type="button"
					value="${assignLabel}" class="button" />
			</c:if>
		</div>
		<div class="table-tab-wrap">
			<camp:campaign-test-plan-table
				batchRemoveButtonId="remove-test-case-button"
				editable="${ linkable }" assignableUsersUrl="${assignableUsersUrl}"
				reorderable="${linkable}"
				campaignUrl="${ campaignUrl }"
				testCaseMultipleRemovalPopupId="delete-multiple-test-cases-dialog" 
				campaign="${campaign}"/>
		</div>


		<%--------------------------- Deletion confirmation popup for Test plan section ------------------------------------%>

		<pop:popup id="delete-multiple-test-cases-dialog"
			openedBy="remove-test-case-button"
			titleKey="dialog.remove-testcase-associations.title">
			<jsp:attribute name="buttons">
		<f:message var="label" key="label.Yes" />
				'${ label }' : function(){
						$("#delete-multiple-test-cases-dialog").data("answer","yes");
						$("#delete-multiple-test-cases-dialog").dialog("close");
				},
				
		<pop:cancel-button /> 
	</jsp:attribute>
			<jsp:attribute name="body">
			<f:message var="emptyMessage"
					key="message.EmptyTableSelection" />			
		<script type="text/javascript">
				$("#delete-multiple-test-cases-dialog").bind( "dialogopen", function(event, ui){
					var _id =  $("#delete-multiple-test-cases-dialog").data("entity-id");
					
					var ids = [];
					
					if(!_id){
						ids = $( '#test-cases-table' ).squashTable().getSelectedIds();
					} else {
						ids.push(_id);
					}
				
					$("#delete-multiple-test-cases-dialog").data("entity-id", null);
					
					if (ids.length == 0) {
						$.squash.openMessage("<f:message key='popup.title.error' />", "${emptyMessage}");
						$(this).dialog('close');
					}
					
					 this.selIds = ids;
				});
			</script>
		<f:message key="dialog.remove-testcase-associations.message" />
	</jsp:attribute>
		</pop:popup>


	</div>

	<%------------------------------ Attachments bloc ---------------------------------------------%>

	<at:attachment-tab tabId="tabs-3" entity="${ campaign }" editable="${ attachable }" tableModel="${attachmentsModel}"/>
	

</div>
<%--------------------------- Deletion confirmation popup -------------------------------------%>
<c:if test="${ deletable }">


	<comp:delete-contextual-node-dialog
		itemId="${campaign.id}"
		successCallback="deleteCampaignSuccess"
		openedBy="delete-campaign-button"
		titleKey="dialog.delete-campaign.title" />

</c:if>


<%--------------------------- Assign User popup -------------------------------------%>


<pop:popup id="batch-assign-test-case"
	titleKey="label.AssignUser" isContextual="true"
	openedBy="assign-test-case-button" closeOnSuccess="false">
	<jsp:attribute name="buttons">
		
			<f:message var="label" key="label.AssignUser" />
			'${ label }': function() {
				var url = "${ assignTestCasesUrl }";
				var table = $( '#test-cases-table' ).squashTable();
				var ids = table.getSelectedIds();

				var user = $(".batch-select", this).val();
			
				$.post(url, { itemIds: ids, userId: user}, function(){
					table.refresh();
					$("#batch-assign-test-case").dialog('close');
				});
				
			},			
			<pop:cancel-button />
		</jsp:attribute>
	<jsp:attribute name="body">
			<f:message var="emptyMessage"
			key="message.EmptyTableSelection" />
			<script type="text/javascript">
				$("#batch-assign-test-case").bind("dialogopen",function(event, ui) {
						var table = $('#test-cases-table').squashTable();
						var ids = table.getSelectedIds();
						if (ids.length > 0) {
							var pop = this;
							$.get("${assignableUsersUrl}","json")
								.success(function(jsonList) {var select = $(".batch-select",pop);
									select.empty();
									for ( var i = 0; i < jsonList.length; i++) {
										select.append('<option value="'+jsonList[i].id+'">'
														+ jsonList[i].login
														+ '</option>');
									}
								});
						} else {
							$.squash.openMessage("<f:message key='popup.title.error' />","${emptyMessage}");
							$(this).dialog('close');
						}
					});
			</script>
			<span><f:message key="message.AssignTestCaseToUser" />
		</span>
			<select class="batch-select"></select>
			
		</jsp:attribute>
</pop:popup>



 <f:message key="tabs.label.issues" var="tabIssueLabel"/>
<script type="text/javascript">

	var identity = { obj_id : ${campaign.id}, obj_restype : "campaigns"  };

	
	require(["domReady", "require"], function(domReady, require){
		domReady(function(){
			require(["jquery", "contextual-content-handlers", "jquery.squash.fragmenttabs", "bugtracker", 'workspace.contextual-content', "jqueryui"], 
					function($, contentHandlers, Frag, bugtracker, contextualContent){
				
				$('#delete-campaign-button').button();
				$('#rename-campaign-button').button();
				
				var nameHandler = contentHandlers.getSimpleNameHandler();
				
				nameHandler.identity = identity;
				nameHandler.nameDisplay = "#campaign-name";
				
				contextualContent.addListener(nameHandler);				
		
				
				//****** tabs configuration ***********
				
				var fragConf = {
					beforeLoad : Frag.confHelper.fnCacheRequests	
				};
				Frag.init(fragConf);
				
				<c:if test="${campaign.project.bugtrackerConnected}">
				bugtracker.btPanel.load({
					url : "${btEntityUrl}",
					label : "${tabIssueLabel}"
				});
				</c:if>
				
			});
		});
	});
	
	function renameCampaignSuccess(data){
		var evt = new EventRename(identity, data.newName);
		squashtm.workspace.contextualContent.fire(null, evt);		
	};					
	



	/* deletion success handler */
	function deleteCampaignSuccess() {
		<c:choose>
		<%-- case one : we were in a sub page context. We need to navigate back to the workspace. --%>
		<c:when test="${param.isInfoPage}" >
		document.location.href = "${workspaceUrl}";
		</c:when>
		<%-- case two : we were already in the workspace. we simply reload it (todo : make something better). --%>
		<c:otherwise>
		location.reload(true);
		</c:otherwise>
		</c:choose>
	}

	/* deletion failure handler */
	function deleteCampaignFailure(xhr) {
		$.squash.openMessage("<f:message key='popup.title.error' />",
				xhr.statusText);
	}
</script>

<comp:decorate-buttons />
