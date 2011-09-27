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
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>

<c:url var="treeBaseUrl" value="/test-case-browser"/>
<c:url var="testCasesTableUrl" value="/campaigns/${campaign.id}/linkable-test-cases-table" />
<c:url var="testPlanUrl" value="/campaigns/${ campaign.id }/test-cases" />
<c:url var="nonBelongingTestCasesUrl" value="/campaigns/${ campaign.id }/non-belonging-test-cases" />
<c:url  var="testCaseDetailsBaseUrl" value="/test-cases" /> 

<%-- ----------------------------------- Authorization ----------------------------------------------%>
<c:set var="editable" value="${ false }" /> 
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ campaign }">
	<c:set var="editable" value="${ true }" /> 
</authz:authorized>

<layout:tree-picker-layout removeLabelKey="association_interface.remove.button.label" 
							workspaceTitleKey="workspace.campaign.title" 
							addLabelKey="association_interface.add.button.label" 
							highlightedWorkspace="campaign"
							treeBaseUrl="${treeBaseUrl}"
							isRequirementPaneSearchOn="true" linkable="test-case" isSubPaged="true">
	<jsp:attribute name="head">
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.purple.css" />

		<aggr:decorate-linkable-campaign-test-cases-table tableModelUrl="${ testCasesTableUrl }" testCasesUrl="${ testPlanUrl }" 
			batchRemoveButtonId="remove-items-button" nonBelongingTestCasesUrl="${ nonBelongingTestCasesUrl }" testCaseDetailsBaseUrl="${ testCaseDetailsBaseUrl }" editable="${editable}"/>
		
		<script type="text/javascript">
		selection = new Array();

			
			function storeSelection(){
				$('a.ui-squashtest-tree-inactive').live('click', function(){
					node = $(this).parent("li");
					if ($(node).attr('rel') != "drive")
					{
						if($(this).hasClass("jstree-clicked")){
							if ($(node).attr('rel') == "file"){
								selection.push($(node).attr('resid'));
							}else{
								selection.push(-$(node).attr('resid'))
							}
						}else{
							for(var i = 0; i <selection.length; i++){
								if(selection[i] == $(node).attr('resid') || selection[i] == -$(node).attr('resid')){
									for(var j = i; j <selection.length; j++){
										selection[j] = selection[j + 1];
									}
									selection.pop();
								}
							}
						}
					}
				});
			}
			
			function getTestCasesIds(){
				var tab =  new Array();
				var selected = $( "#tabbed-pane" ).tabs('option', 'selected');
				var tree = $( '#linkable-test-cases-tree' );
				if (selected == 0){
					tree.jstree('get_selected').each(function(index, node){
						if ($( node ).attr('resType') == 'test-cases') {
							tab.push($( node ).attr('resId'));
						}
					});
				}
				if (selected == 1){
					var table = $( '#search-result-datatable' ).dataTable();
					tab = getIdsOfSelectedAssociationTableRows(table, getTestCasesTableRowId);
				}
				return tab;
			}
			

			
			$(function() {

				storeSelection();
				<%-- back button --%>
				
				$("#back").button().click(function(){
					//document.location.href="${referer}";
					history.back();
				});
				
				<%-- test-case addition --%>
				$( '#add-items-button' ).click(function() {
					<%--
					var tree = $( '#linkable-test-cases-tree' );
					var ids = new Array();
					
					ids = getTestCasesIds();
					--%>
					
					var tree = $( '#linkable-test-cases-tree' );
					var ids = selection;
					//tabs selection
					if(selectedTab != 0){
						ids = getIdSelection();
					}
					if (ids.length > 0) {
						$.post('${ testPlanUrl }', { testCasesIds: ids}, refreshTestCases);
					}
					tree.jstree('deselect_all');
					//reset the multiple selection fields
					firstIndex = null;
					lastIndex = null;
					//clear selection
					selection = [];
				});
			});
		</script>
	</jsp:attribute>
	
		<jsp:attribute name="subPageTitle">
		<h2>${campaign.name}&nbsp;:&nbsp;<f:message key="squashtm.library.verifying-test-cases.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="fragment.edit.header.button.back" />
		<input type="button" class="button" value="${backButtonLabel}" onClick="history.back();"/>	
	</jsp:attribute>	
	
	
	<jsp:attribute name="tree">
		<tree:linkables-tree iconSet="testcase"  id="linkable-test-cases-tree" rootModel="${ linkableLibrariesModel }" folderContentUrlHandler="folderContentUrl" driveContentUrlHandler="libraryContentUrl"/>
	</jsp:attribute>

	<jsp:attribute name="tableTitlePane">		
		<div class="snap-left" style="height:100%;">	
			<h2>
				<f:message var="title" key="campaign.test-plan.panel.title"/>
				<label>${title}</label>
			</h2>
		</div>	
		<div style="clear:both;"></div>
	</jsp:attribute>	
	
	<jsp:attribute name="tablePane">
		<aggr:linkable-campaign-test-cases-table />
	</jsp:attribute>
</layout:tree-picker-layout>

