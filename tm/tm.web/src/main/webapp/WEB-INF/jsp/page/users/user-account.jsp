<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2015 Henix, henix.fr

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
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
    
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>


<f:message var="userAccountPasswordLabel" key="label.password" />
<c:url var="userAccountUrl" value="/user-account/update" />

<layout:info-page-layout titleKey="dialog.settings.account.title" highlightedWorkspace="home">
	<jsp:attribute name="head">	
		<comp:sq-css name="squash.grey.css" />
	</jsp:attribute>
  
	<jsp:attribute name="titlePane">
		<h2><f:message key="dialog.settings.account.title" /></h2>	
	</jsp:attribute>
	
	<jsp:attribute name="informationContent">	
	
	<script type="text/javascript">
		require(["common"], function() {
				require(["jquery","squash.basicwidgets"], function($, basic){
					$(function(){
					basic.init();
					$("#back").click(function(){
						history.back();
					});
				});
					
					if(localStorage["requirement-tree-pref"] == 1){
						$('#user-preferences-tree-requirement option:eq(1)').prop('selected', true);
					}
					
					if(localStorage["test-case-tree-pref"] == 1){
						$('#user-preferences-tree-test-case option:eq(1)').prop('selected', true);
					}
					
					if(localStorage["campaign-tree-pref"] == 1){
						$('#user-preferences-tree-campaign option:eq(1)').prop('selected', true);
					}
					
					$("#user-preferences-tree-requirement").change(function(){
						localStorage["requirement-tree-pref"] = $("#user-preferences-tree-requirement").val();
					});
					
					$("#user-preferences-tree-test-case").change(function(){
						localStorage["test-case-tree-pref"] = $("#user-preferences-tree-test-case").val();
					});
					
					$("#user-preferences-tree-campaign").change(function(){
						localStorage["campaign-tree-pref"] = $("#user-preferences-tree-campaign").val();
					});
					
			});
		});

		

		
	</script>
	<div id="user-login-div" class="ui-widget-header ui-corner-all ui-state-default fragment-header">
	
		<div style="float: left; height: 3em">
			<h2>
				<label for="user-login-header"><f:message key="user.header.title" /></label>
				<c:out value="${ user.login }" escapeXml="true" />
			</h2>
		</div>
		<div class="snap-right"><f:message var="back" key="label.Back" /> 
			<input id="back" type="button" value="${ back }" class="sq-btn" />
		</div>

		<div class="unsnap"></div>
	
	</div>
	
	<div class="fragment-body">

		<comp:toggle-panel id="basic-info-panel" titleKey="user.account.basicinfo.label" open="true" >
			<jsp:attribute name="body">
				<div class="display-table">
					<div class="user-account-unmodifiable-field display-table-row">
						<label><f:message key="label.Name"/></label>
						<div class="display-table-cell"><span>${user.firstName } ${user.lastName}</span></div>
					</div>
					<div class="display-table-row">
						<label ><f:message key="label.Email"/></label>
						<div class="display-table-cell editable text-editable" data-def="url=${userAccountUrl}, width=200"><span id="user-account-email">${user.email}</span></div>
					</div>
					<div class="display-table-row">
						<label ><f:message key="label.Group"/></label>
						<div class="display-table-cell"><span><f:message key="user.account.group.${user.group.qualifiedName}.label" /></span></div>
					</div>
        <c:if test="${ authenticationProvider.managedPassword }">
        <div class="display-table-row">
          <label for="managed-pwd"><f:message key="label.password"/></label>
          <div class="display-table-cell">
            <span id="managed-pwd"><f:message key="message.managedPassword" /></span>
          </div>
        </div>
        </c:if>
				</div>
				<br/>
        <c:if test="${ not authenticationProvider.managedPassword }">
				<input type="button" id="change-password-button" value="${ userAccountPasswordLabel }" class="button" />									
        </c:if>
			</jsp:attribute>
		</comp:toggle-panel>	
		<comp:toggle-panel id="project-permission-panel" titleKey="user.project-rights.title.label" open="true">
			<jsp:attribute name="body">
				<table id="project-permission-table" data-def="hover">
				<thead>
					<tr>
						<th data-def="sortable, target=0"><f:message key="label.project" /></th>
						<th data-def="sortable, target=1"><f:message key="label.Permission" /></th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="projectPermission" items="${ projectPermissions }">
					<tr><td>${ projectPermission.project.name }</td><td><f:message key="user.project-rights.${projectPermission.permissionGroup.simpleName}.label" /></td></tr>
					</c:forEach>
				</tbody>
			</table>
			</jsp:attribute>
		</comp:toggle-panel>		
		
		<comp:toggle-panel id="tree-order-panel" titleKey="user-preferences.tree-order.title" open="true" >
			<jsp:attribute name="body">
				<div class="display-table">
				<div class="display-table-row">
						<label><f:message key="user-preferences.tree-order.requirement.title"/></label>
						<div class="display-table-cell">
							<select id="user-preferences-tree-requirement">
								<option value="0"><f:message key="user-preferences.tree-order.alphabetical"/></option>
								<option value="1"><f:message key="user-preferences.tree-order.custom"/></option>
							</select>
						</div>
				</div>
			    <div class="display-table-row">
						<label><f:message key="user-preferences.tree-order.testcase.title"/></label>
						<div class="display-table-cell">
							<span>
							<select id="user-preferences-tree-test-case">
								<option value="0"><f:message key="user-preferences.tree-order.alphabetical"/></option>
								<option value="1"><f:message key="user-preferences.tree-order.custom"/></option>
							</select>
							</span>
						</div>
				</div>
				<div class="display-table-row">
										<label><f:message key="user-preferences.tree-order.campaign.title"/></label>
						<div class="display-table-cell">
							<span>
							<select id="user-preferences-tree-campaign">
								<option value="0"><f:message key="user-preferences.tree-order.alphabetical"/></option>
								<option value="1"><f:message key="user-preferences.tree-order.custom"/></option>
							</select>
							</span>
						</div>
				</div>
				</div>
			</jsp:attribute>
		</comp:toggle-panel>
    
        <f:message var="milestoneReferentialMode" key="user-preferences.tree-order.referentiel.label" />
        <f:message var="milestoneMilestoneMode" key="user-preferences.milestone" />
       
          <c:if test="${ milestoneFeatureEnabled }">
        	<comp:toggle-panel id="library-display-mode-panel" titleKey="user-preferences.tree-order.mode.title" open="true" >
			<jsp:attribute name="body">
				<div class="display-table">			
				<div class="display-table-row">
					<div class="display-table-cell">  
					<label for="toggle-activation-checkbox" ><f:message key="user-preferences.tree-order.mode.label"/></label>
					</div>
					<div class="customHeigth">
					<div class="display-table-cell">                  		
                 			<input id="toggle-MODE-checkbox" type="checkbox" 
                 	          data-def="width=35, on_label='${milestoneMilestoneMode}', off_label='${milestoneReferentialMode}'" style="display: none;"/>
                 		</div>
                 	</div>
			</div>
							
			<div class="display-table-row">
				<div class="display-table-cell">  
				<label for="choose-your-mode" ><f:message key="user-preferences.milestone"/></label>
				</div>
				<div id="labelchoose" class="customHeigth">
				 <select id="milestone-group" class="combobox" >
			        <c:forEach items="${ milestoneList }" var="milestone"> 
			           <option value = "${milestone.id}" >${milestone.label}</option>
		            </c:forEach>                    
				</select>
			</div>
			</jsp:attribute>
			</comp:toggle-panel>
		 </c:if>
		
	</div>
    <c:if test="${ not authenticationProvider.managedPassword }">
	   <comp:user-account-password-popup/>
    </c:if>
	</jsp:attribute>
</layout:info-page-layout>

<script type="text/javascript">
  require(["common"], function() {
    require(["jquery", "projects-manager", "jquery.squash.fragmenttabs", "squash.attributeparser", 
  	         "project/ProjectToolbar", "app/ws/squashtm.notification", "squash.translator", "milestone-manager/milestone-activation", "squashtable", "jquery.switchButton", "jquery.cookie"], 
        function($, projectsManager, Frag, attrparser, ProjectToolbar, notification, translator, milestoneActivation){    	
      
  	  $("#project-permission-table").squashTable({
  		  'bServerSide' : false,
  		  'sDom' : '<r>t<i>',
  		  'sPaginationType' : 'full_numbers'
  	  },{});

  	  var milestoneGroup = $("#milestone-group") ;
  	  
  	  $(function() {
  	  			milestoneActivation.init(milestoneGroup);
		 		init(projectsManager, Frag);	
	 		
		 		new ProjectToolbar(); 		

			  	$("#toggle-MODE-checkbox").change(function(){
			  		toggleStatusActivation("MODE");
			  	 		if ($("#toggle-MODE-checkbox").prop('checked')) {
					 		 document.getElementById("milestone-group").disabled = false; 		
						 		}
			  	 		else 	{
					 		 document.getElementById("milestone-group").disabled = true;		
					 		}
			  	  });
	  	});

  		function init(projectsManager, Frag){
   			Frag.init();

  		};

  		function toggleStatusActivation(status){
  			var shouldActivate = $("#toggle-"+status+"-checkbox").prop('checked');
  			if (shouldActivate){
  				$("#milestone-group option[value='']").remove();
  				activateStatus();
  			}
  			else{
  				deactivateStatus();
  			}
  		};	

  		function activateStatus(){
  			milestoneActivation.activateStatus(milestoneGroup);
  		};
  		
  		function deactivateStatus(){
  			milestoneActivation.deactivateStatus(milestoneGroup);
  		};
    })
  });
</script>	