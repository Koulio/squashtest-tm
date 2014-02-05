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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>
<%@ taglib prefix="tc" tagdir="/WEB-INF/tags/test-cases-components"%>
<%@ taglib prefix="csst" uri="http://org.squashtest.tm/taglib/css-transform" %>


<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>

<%-- used for copy/paste of steps --%>
<script>
require(["common"], function() {
	require(["jquery.cookie"]);
})
</script>

<%------------------------------------- URLs ----------------------------------------------%>


<c:url var="testCaseUrl" 					value="/test-cases/${testCase.id}"/>
<c:url var="executionsTabUrl"				value="/test-cases/${testCase.id}/executions?tab="/>
<c:url var="stepTabUrl"						value="/test-cases/${testCase.id}/steps/panel" />
 <c:url var="importanceAutoUrl" 				value="/test-cases/${testCase.id}/importanceAuto"/>

<%-- ----------------------------------- Authorization ----------------------------------------------%>
<%-- 
	if no variable 'editable' was provided in the context, we'll set one according to the authorization the user
	was granted for that object. 
--%>

<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE"
	domainObject="${ testCase }">
	<c:set var="writable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="ATTACH"
	domainObject="${ testCase }">
	<c:set var="attachable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>

<authz:authorized hasRole="ROLE_ADMIN" hasPermission="DELETE"
	domainObject="${ testCase }">
	<c:set var="deletable" value="${true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="CREATE"
	domainObject="${ testCase }">
	<c:set var="creatable" value="${true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="LINK"
	domainObject="${ testCase }">
	<c:set var="linkable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>


<%---------------------------- Test Case Header ------------------------------%>

<tc:test-case-header testCase="${testCase}" />


<%---------------------------- Test Case Informations ------------------------------%>

<tc:test-case-toolbar testCase="${testCase}" isInfoPage="${param.isInfoPage}" otherViewers="${otherViewers}"   
					  moreThanReadOnly="${moreThanReadOnly}"  writable="${writable}" deletable="${deletable}" />

<%-- --------------------------------------- Test Case body --------------------------------------- --%>
<csst:jq-tab>
<div class="fragment-tabs fragment-body">

	<%--  ------------------ main tab panel --------------------------------- --%>
	<ul class="tab-menu">
		<li>
			<a href="#tab-tc-informations"><f:message key="tabs.label.information" /></a>
		</li>
		<li>
			<a href="${stepTabUrl}"><f:message key="tabs.label.steps" /></a>
		</li>
		<li>
			<a href="${testCaseUrl}/parameters/panel"><f:message key="label.parameters" /></a>
		</li>
		<li>
			<a href="#tabs-tc-attachments"><f:message key="label.Attachments" />
			<c:if test="${ testCase.attachmentList.notEmpty }">
				<span class="hasAttach">!</span>
			</c:if> 
			</a>
		</li>
		<li>
			<a href="${executionsTabUrl}"><f:message key="label.executions" /> </a>
		</li>
	</ul>
	
		

	<div id="tab-tc-informations">
			
		<%-- ------------------------- Description Panel ------------------------- --%>
	
		<tc:test-case-description 	testCase="${testCase}" 
									testCaseImportanceLabel="${testCaseImportanceLabel}"
									writable="${writable}"/>
		

		<%----------------------------------- Prerequisites -----------------------------------------------%>

		<tc:test-case-prerequisites testCase="${testCase}" />


		<%--------------------------- Verified Requirements section ------------------------------------%>

		<tc:test-case-verified-requirement-bloc linkable="${ linkable }" testCase="${testCase}"  containerId="contextual-content"/>


		<%--------------------------- calling test case section ------------------------------------%>

		<tc:calling-test-cases-panel testCase="${testCase}"/>


	</div>
	
	<%-- ------------------------- /Description Panel ------------------------- --%>
	<%------------------------------ Attachments  ---------------------------------------------%>	
	
	<at:attachment-tab tabId="tabs-tc-attachments"  entity="${ testCase }"  editable="${ attachable }" tableModel="${attachmentsModel}"/>
	
	<%------------------------------ /Attachments  ---------------------------------------------%>


</div>
</csst:jq-tab>


<%-- ===================================== INIT =============================== --%>

<tc:test-case-main-js testCase="${testCase}" isInfoPage="${param.isInfoPage}" callingTestCasesModel="${callingTestCasesModel}"/>

<%-- ===================================== /INIT =============================== --%>


<%-- ===================================== popups =============================== --%>

<tc:test-case-popups testCase="${testCase}" writable="${writable}" deletable="${deletable}" />

<%-- ===================================== /popups =============================== --%>
		
<%-- ===================================== Test Automation code  =============================== --%>

<c:if test="${testCase.project.testAutomationEnabled}">
	<tc:testcase-script-elt-code testCase="${testCase}"
								 canModify="${writable}" 
								 testCaseUrl="${testCaseUrl}" />
</c:if>

<%-- ===================================== /Test Automation code  ===============================  --%>

<script type="text/javascript" th:inline="javascript">
	/*<![CDATA[*/
	var squashtm = squashtm || {};
  	squashtm.app = squashtm.app || {} ;	 
    require([ "common" ], function() {
        require([ "jquery", "test-case-management" ],
			function($, testCaseManagement) {
					var settings = {
							urls : {
								testCaseUrl : "${testCaseUrl}",
								importanceAutoUrl : "${importanceAutoUrl}"
							},
							writable : ${writable},
							testCaseImportanceComboJson : ${testCaseImportanceComboJson},
							testCaseNatureComboJson : ${testCaseNatureComboJson},
							testCaseStatusComboJson : ${testCaseStatusComboJson},
							testCaseTypeComboJson : ${testCaseTypeComboJson},							
							importanceAuto : ${testCase.importanceAuto},
							testCaseId : ${testCase.id}
					};
					
				$(function() {
					testCaseManagement.initInfosTab(settings);
				});
			});
    });
	/*]]>*/
</script>

