<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org

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
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sq" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<c:url var="treeBaseUrl" value="/requirement-browser/"/>
<c:url var="verifiedRequirementsTableUrl" value="/test-cases/${testCase.id}/verified-requirement-versions/table" />
<c:url var="verifiedRequirementsUrl" value="/test-cases/${ testCase.id }/verified-requirement-versions" />
<c:url var="nonVerifiedRequirementsUrl" value="/test-cases/${ testCase.id }/non-verified-requirement-versions" />

<layout:tree-picker-layout workspaceTitleKey="workspace.test-case.title" 
						   highlightedWorkspace="test-case"
						   treeBaseUrl="${treeBaseUrl}" linkable="requirement" isSubPaged="true">
						   
	<jsp:attribute name="head">
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.green.css" />
		
		<aggr:decorate-verified-requirements-table tableModelUrl="${ verifiedRequirementsTableUrl }" verifiedRequirementsUrl="${ verifiedRequirementsUrl }" 
				nonVerifiedRequirementsUrl="${ nonVerifiedRequirementsUrl }" batchRemoveButtonId="remove-items-button" />
				
		<c:url var="addVerifiedRequirementsUrl" value="/test-cases/${ testCase.id }/verified-requirements" />
		<script type="text/javascript">

			function getRequirementsIds(){
				var tab =  new Array();
				var selected = $( "#tabbed-pane" ).tabs('option', 'selected');
				var tree = $( '#linkable-requirements-tree' );
				if (selected == 0){
					tab = tree.jstree('get_selected')
						  .not(':library')
						  .collect(function(elt){return $(elt).attr('resid');});
				}
				if (selected == 1){
					var table = $( '#search-result-datatable' ).dataTable();
					tab = getIdsOfSelectedAssociationTableRows(table, getRequirementsTableRowId);
				}
				
				return tab;
			}
		
			
			
			$(function() {
				$( "#add-summary-dialog" ).messageDialog();
				
				var summaryMessages = {
					alreadyVerifiedRejections: "<f:message key='test-case.verified-requirement-version.already-verified-rejection' />",
					notLinkableRejections: "<f:message key='requirement-version.verifying-test-case.not-linkable-rejection' />",
					noVerifiableVersionRejections: "<f:message key='test-case.verified-requirement-version.no-verifiable-version-rejection' />" 
				};
					
				var showAddSummary = function(summary) {
					if (summary) {
						var summaryRoot = $( "#add-summary-dialog > ul" );
						summaryRoot.empty();
						
						for(rejectionType in summary) {
							var message = summaryMessages[rejectionType];
							
							if (message) {
								summaryRoot.append('<li>' + message + '</li>');
							}
						}
						
						if (summaryRoot.children().length > 0) {
							$( "#add-summary-dialog" ).messageDialog("open");
						}
					}					
				};
				
				var addHandler = function(data) {
					showAddSummary(data);
					refreshVerifiedRequirements();
				};
					
				<%-- verified requirements addition --%>
				$( '#add-items-button' ).click(function() {
					var tree = $( '#linkable-requirements-tree' );
					var ids = new Array();
					ids = getRequirementsIds();
			
					if (ids.length > 0) {
						$.post('${ addVerifiedRequirementsUrl }', { requirementsIds: ids}, addHandler);
					}
					tree.jstree('deselect_all');
				});				
			});				
		</script>
	</jsp:attribute>
	

	
	<jsp:attribute name="tree">
		<tree:linkables-tree workspaceType="requirement" id="linkable-requirements-tree" rootModel="${ linkableLibrariesModel }"/>
	</jsp:attribute>
	
	<jsp:attribute name="tableTitlePane">		
		<div class="snap-left" style="height:100%;">			
			<h2>
				<f:message var="title" key="test-case.verified_requirements.panel.title"/>
				<span>${title}</span>
			</h2>
		</div>						
		<div style="clear:both;"></div>
	</jsp:attribute>
	
	<jsp:attribute name="subPageTitle">
		<h2>${testCase.name}&nbsp;:&nbsp;<f:message key="squashtm.library.verifiedrequirements.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="fragment.edit.header.button.back" />
		<input type="button" class="button" value="${ backButtonLabel }" onClick="history.back();"/>	
	</jsp:attribute>		
	
	<jsp:attribute name="tablePane">
		<aggr:verified-requirements-table/>
		<div id="add-summary-dialog" class="not-displayed" title="<f:message key='test-case.verified-requirement-version.add-summary-dialog.title' />">
			<ul><li>summary message here</li></ul>
		</div>
	</jsp:attribute>
</layout:tree-picker-layout>

