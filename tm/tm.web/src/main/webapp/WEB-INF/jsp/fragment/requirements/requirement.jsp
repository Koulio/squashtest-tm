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

<%--
  
  Aaaah what a js mess, my eyes are bleeding !

 --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="reqs" tagdir="/WEB-INF/tags/requirements-components" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>
<%@ taglib prefix="csst" uri="http://org.squashtest.tm/taglib/css-transform" %>

<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<s:url var="requirementUrl" value="/requirements/{reqId}">
	<s:param name="reqId" value="${requirement.id}" />
</s:url>
<s:url var="verifyingTCManagerUrl" value="/requirement-versions/${ requirement.currentVersion.id }/verifying-test-cases/manager" /> 
<c:url var="workspaceUrl" value="/requirement-workspace/#" />

<s:url var="getStatusComboContent" value="/requirements/${requirement.id}/next-status" />
<c:url var="customFieldsValuesURL" value="/custom-fields/values" />
<s:url var="createNewVersionUrl" value="/requirements/${requirement.id}/versions/new" />
<%-- ----------------------------------- Authorization ----------------------------------------------%>
<%-- 
that page won't be editable if 
   * the user don't have the correct permission,
   * the requirement status doesn't allow it.

 --%>
 
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="ATTACH" domainObject="${ requirement }">
	<c:set var="attachable" value="${ requirement.modifiable }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ requirement }">
	<c:set var="writable" value="${ requirement.modifiable }"/>
		<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="DELETE" domainObject="${ requirement }">
	<c:set var="deletable" value="${true}"/>
		<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="CREATE" domainObject="${ requirement }">
	<c:set var="creatable" value="${true }"/>
		<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ requirement }">
	<c:set var="linkable" value="${ requirement.linkable }" />
		<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<c:set var="status_editable" value="${ moreThanReadOnly and requirement.status.allowsStatusUpdate }"/>


<f:message var="confirmLabel" key="label.Confirm"/>
<f:message var="cancelLabel" key="label.Cancel"/>
<f:message var="okLabel" key="label.Ok"/>



<c:if test="${writable}">
<script type="text/javascript">
var squashtm = squashtm || {};

require(["common"], function() {
  require(["jquery", "workspace.event-bus"], function($,  eventBus) {
    function renameRequirementSuccess(data){
    	eventBus.trigger('node.rename', { identity : identity, newName : data.newName});    	
    }

    function updateReferenceInTitle(reference){
    	eventBus.trigger('node.update-reference', { identity : identity, newRef : reference});  
    }

    //for technical reasons we handle directly the ajax operation when choosing a category.
    function postUpdateCategory(value, settings){
      $.post("${requirementUrl}", {id:"requirement-category", value : value})
      .done(function(response){
    	eventBus.trigger("node.attribute-changed", {identity : identity, attribute : 'category', value : value.toLowerCase()});
      });

      //in the mean time, must return immediately
      var data = JSON.parse(settings.data);
      return data[value];
    }
    
    squashtm.requirement = squashtm.requirement || {}
    squashtm.requirement.renameRequirementSuccess = renameRequirementSuccess;
    squashtm.requirement.updateReferenceInTitle = updateReferenceInTitle;
    squashtm.requirement.postUpdateCategory = postUpdateCategory;
  });
});
</script>
</c:if>


<%-- ----------------------------------- Init ----------------------------------------------%>
<%-- 
	Code managing the status of a requirement. It is a handler for the 'onsubmit' of a jeditable (see documentation for details).

	It will ask the user, if he chooses to change the requirement status to 'Obsolete', to confirm.
	Because a jQuery dialog will not stop the execution of javascript we have to code in an unnatural way. 
	Basically this function will be called twice : first to invoked the dialog, second to read its response.	
	
	Here is how it works : 
	
		- if any status but 'obsolete' is selected, return true.
		- if 'obsolete' is selected and 'summoned' is false, sets 'summoned' to true, summons the dialog and return false.
		- if 'obsolete' is selected and 'summoned' is true, sets 'summoned' to false and read 'confirm' :
			* 'confirm' is false : reset the widget then return false.
			* 'confirm' is true : send the information then return true
		
	note : Since summoning a dialog will not stop the rest of the code from executing, the second case returns false to ensure that the handler terminates quickly while the dialog
	is still executed.
	
	'summoned' and 'confirm' are attributes of the dialog #. When summoned the dialog will set 'confirm' 
	according to the user response and submit the select again. This will in turn call that hook again, which will eventually read the user response and then only decide whether 
	the user input will be sent or not.
	
	See also the code in #requirement-status-confirm-dialog for details. 
--%>
<c:if test="${status_editable}">
<f:message var="DefaultStatusNotAllowedMessage" key='requirement.status.notAllowed.default' />
<f:message var="ApprovedStatusNotAllowedMessage" key='requirement.status.notAllowed.approved' />
<script type="text/javascript">
		function statusSelect(settings, widget){
			
			//first check if 'obsolete' is selected
			var selected = $(this.find('select')).val();
			
			var toReturn = true;
			
			if (isDisabled(selected)){
				
				toReturn=false;
				
				// wtf is that test ??
				if("disabled.APPROVED"){
					$.squash.openMessage("<f:message key='popup.title.error' />", "${ApprovedStatusNotAllowedMessage}")
					.done(function(){$("#requirement-status").editable().resetForm();});
				}
				else{
					$.squash.openMessage("<f:message key='popup.title.error' />", "${DefaultStatusNotAllowedMessage}")
					.done(function(){$("#requirement-status").editable().resetForm();});
				}
			}
			else if ("OBSOLETE" == selected) {
				var jqDialog = $('#requirement-status-confirm-dialog');
				var summoned = jqDialog.data('summoned');
				
				if (! summoned) {
					statusObsoleteSummonDialog(this, jqDialog);
					toReturn=false;
				} else {	
					jqDialog.data('summoned', false);
					toReturn = statusObsoleteReadDialog(widget, jqDialog);			
				}
			} else {
				toReturn = true;
			}
			
			return toReturn;
		}
			
		function isDisabled(selected){
			return (selected.search(new RegExp("disabled.*"))!=-1);
		}
		
		function statusObsoleteSummonDialog(form, jqDialog){
			jqDialog.data('summoned', true);
			jqDialog.data('callMeBack', form);
			jqDialog.formDialog('open');			
		}
		
		//reset the 'summoned' flag and the widget if needed
		function statusObsoleteReadDialog(widget, jqDialog){
			var response = jqDialog.data('confirm');
			if (false==response) {
				$(widget).html(widget.revert);
				widget.editing  = false;	
			}	
			return response;
		}
		
		
		function statusSelectCallback(){
			document.location.reload();
		}
</script>
</c:if>
<%-- ----------------------------------- CONTENT ----------------------------------------------%>
<%-- ----------------------------------- TITLE ----------------------------------------------%>
<div class="ui-widget-header ui-corner-all ui-state-default fragment-header">
	<div style="float:left;height:100%;">	
		<h2>
		
			<c:set var="completeRequirementName" value="${ requirement.name }" />
			<c:if test="${not empty requirement.reference && fn:length(requirement.reference) > 0}" >
				<c:set var="completeRequirementName" value='${ requirement.reference } - ${ requirement.name }' />
			</c:if>
			<a id="requirement-name" href="${ requirementUrl }/info"><c:out value="${ completeRequirementName }" escapeXml="true"/></a>
			<%-- raw reference and name because we need to get the name and only the name for modification, and then re-compose the title with the reference  --%>
			<span id="requirement-raw-reference" style="display:none"><c:out value="${ requirement.reference }" escapeXml="true"/></span>
			<span id="requirement-raw-name" style="display:none"><c:out value="${ requirement.name }" escapeXml="true"/></span>
		</h2>
	</div>
	<div class="unsnap"></div>		
</div>
<%-- ----------------------------------- /TITLE ----------------------------------------------%>
<%-- ----------------------------------- AUDIT & TOOLBAR  ----------------------------------------------%>	
<div id="requirement-toolbar" class="toolbar-class ui-corner-all" >
	<div  class="toolbar-information-panel">
	<c:url var="currentVersionUrl" value="/requirement-versions/${requirement.currentVersion.id}" />
		<comp:general-information-panel auditableEntity="${ requirement.currentVersion }" entityUrl="${ currentVersionUrl }" />
	</div>

	<div class="toolbar-button-panel">
		<c:if test="${ writable }">
			<input type="button" value='<f:message key="requirement.button.rename.label" />' id="rename-requirement-button" class="sq-btn"/> 
		</c:if>
		<c:if test="${ creatable }">
			<input type="button" value='<f:message key="requirement.button.new-version.label" />' id="new-version-button" class="sq-btn"/>		
		</c:if>
		<input type="button" value="<f:message key='label.print'/>" id="print-requirement-version-button" class="sq-btn"/>
	</div>	

	<div class="unsnap"></div>	
	<c:if test="${ moreThanReadOnly	 }">
	<comp:opened-object otherViewers="${ otherViewers }" objectUrl="${ requirementUrl }" />
	</c:if>
</div>
<%-- ----------------------------------- /AUDIT & TOOLBAR  ----------------------------------------------%>	
<%-- ----------------------------------- TABS  ----------------------------------------------%>	
<csst:jq-tab>
<div class="fragment-tabs fragment-body">
	<ul class="tab-menu">
		<li><a href="#tabs-1"><f:message key="tabs.label.information" /></a></li>
		<li><a href="#tabs-2"><f:message key="label.Attachments" />
		<c:if test="${ requirement.attachmentList.notEmpty }"><span class="hasAttach">!</span></c:if>
		</a></li>
	</ul>
	<%-- ----------------------------------- INFO TAB  ----------------------------------------------%>	
	<div id="tabs-1">
	
	<c:if test="${writable }">
        <c:set var="descrRicheditAttributes" value="class='editable rich-editable' data-def='url=${requirementUrl}'"/>
        <c:set var="referenceEditableAttributes" value="class='editable text-editable' data-def='url=${requirementUrl}, maxlength=50, callback=squashtm.requirement.updateReferenceInTitle'" />
	</c:if>
<%--------------------------- General Informations section ------------------------------------%>

	<f:message var="labelRequirementInfoPanel" key="requirement.panel.general-informations.title"  />
	<comp:toggle-panel id="requirement-information-panel"   title=  '${labelRequirementInfoPanel} <span class="small discret">[ID = ${ requirement.id }]</span>' open="true" >
		<jsp:attribute name="body">
			<div id="edit-requirement-table" class="display-table">
				<div class="display-table-row">
					<label for="requirement-version-number"><f:message key="requirement-version.version-number.label" /></label>
					<div class="display-table-cell" id="requirement-version-number">${ requirement.currentVersion.versionNumber }&nbsp;&nbsp;<a href="<c:url value='/requirements/${ requirement.id }/versions/manager' />"><f:message key="requirement.button.manage-versions.label" /></a></div>
				</div>
			
				
				<div class="display-table-row">
					<label class="display-table-cell"  for="requirement-reference"><f:message key="label.Reference" /></label>
					<div id="requirement-reference" ${referenceEditableAttributes}>${ requirement.reference }</div>
				</div>
				<div class="display-table-row">
					<label for="requirement-criticality" class="display-table-cell"><f:message key="requirement.criticality.combo.label" /></label>
					<div class="display-table-cell">
						<c:choose>
						<c:when test="${writable }">
						<div id="requirement-criticality"><comp:level-message level="${ requirement.criticality }"/></div>
						<comp:select-jeditable componentId="requirement-criticality" jsonData="${criticalityList}" targetUrl="${requirementUrl}" />
						</c:when>
						<c:otherwise>
							<comp:level-message level="${ requirement.criticality }"/>
						</c:otherwise>
						</c:choose>
					</div>
				</div>
				<div class="display-table-row">
					<label for="requirement-category" class="display-table-cell"><f:message key="requirement.category.combo.label" /></label>
					<div class="display-table-cell">
						<c:choose>
							<c:when test="${writable }">
								<div id="requirement-category"><s:message code="${ requirement.category.i18nKey }" htmlEscape="true" /></div>
								<comp:select-jeditable componentId="requirement-category" jsonData="${categoryList}" targetFunction="squashtm.requirement.postUpdateCategory"/>
							</c:when>
							<c:otherwise>
								<s:message code="${ requirement.category.i18nKey }" htmlEscape="true" />
							</c:otherwise>
						</c:choose>
					</div>				
				</div>
				<div class="display-table-row">
					<label for="requirement-status" class="display-table-cell"><f:message key="requirement.status.combo.label" /></label>
					<div class="display-table-cell">
						<c:choose>
						<c:when test="${status_editable}">
						<div id="requirement-status"><comp:level-message level="${ requirement.status }"/></div>
						<comp:select-jeditable componentId="requirement-status" jsonUrl="${getStatusComboContent}" 
												targetUrl="${requirementUrl}"	
												onSubmit="statusSelect" submitCallback="statusSelectCallback"/>
						</c:when>
						<c:otherwise>
							<comp:level-message level="${ requirement.status }"/>
						</c:otherwise>
						</c:choose>
					</div>

				</div>				
			</div>
		</jsp:attribute>
	</comp:toggle-panel>
	<%--------------------------- Description section------------------------------------%>
	<comp:toggle-panel id="requirement-description-panel" titleKey="label.Description" open="true" >
		<jsp:attribute name="body">	
					<div id="requirement-description" ${descrRicheditAttributes}>${ requirement.description }</div>
		</jsp:attribute>
	</comp:toggle-panel>

	<%--------------------------- verifying TestCase section ------------------------------------%>
	<script type="text/javascript">
	require([ "common" ], function() {
		require([ "jquery" ], function($) {
    		$(function(){
    			$("#verifying-test-case-button").click(function(){
    				document.location.href="${verifyingTCManagerUrl}" ;	
    			});
		});
    	
	});
	});
	</script>

	<comp:toggle-panel id="verifying-requirement-panel" titleKey="requirement.verifying_test-case.panel.title" open="true">
		<jsp:attribute name="panelButtons">
			<c:if test="${ linkable }">
				<f:message var="associateLabel" key="requirement.verifying_test-case.manage.button.label"/>
				<f:message var="removeLabel" key="label.removeRequirementsAssociation"/>
				
				
				<input id="verifying-test-case-button" type="button" class="sq-btn" value="${ associateLabel }"/>
				<input id="remove-verifying-test-case-button" type="button" class="sq-btn" value="${ removeLabel }"/>
			</c:if>
		</jsp:attribute>

		<jsp:attribute name="body">
			<reqs:verifying-test-cases-table 
			batchRemoveButtonId="remove-verifying-test-case-button" requirementVersion="${requirement.currentVersion}" 
				editable="${ linkable }" model="${verifyingTestCasesModel}"/>
		</jsp:attribute>
	</comp:toggle-panel>
	
	<reqs:requirement-version-audit-trail requirementVersion="${ requirement.currentVersion }" tableModel="${auditTrailModel}"/>
	
</div>
<%-- ----------------------------------- /INFO TAB  ----------------------------------------------%>	
<%-- ----------------------------------- ATTACHMENT TAB  ----------------------------------------------%>
<at:attachment-tab tabId="tabs-2" entity="${ requirement }" editable="${ attachable }" tableModel="${attachmentsModel}"/>
<%-- ----------------------------------- /ATTACHMENT TAB  ----------------------------------------------%>	
<%-- -------------------------------------------------------- /TABS  ----------------------------------------------%>	
</div>
</csst:jq-tab>
<%-- ----------------------------------------------------------- /CONTENT ----------------------------------------------%>
	
<%-- -----------------------------------POPUPS ----------------------------------------------%>
<%--------------------------- Rename popup -------------------------------------%>

<div class="not-displayed">
<c:if test="${ writable }">
		
    <f:message var="renameDialogTitle" key="dialog.rename-requirement.title"/>
    <div  id="rename-requirement-dialog" class="not-displayed popup-dialog"
          title="${renameDialogTitle}">
    
        <label><f:message key="dialog.rename.label" /></label>
        <input type="text" id="rename-requirement-input" maxlength="255" size="50" /><br/>
        <comp:error-message forField="name"/>
    
    
        <div class="popup-dialog-buttonpane">
          <input type="button" value="${confirmLabel}" data-def="evt=confirm, mainbtn"/>
          <input type="button" value="${cancelLabel}" data-def="evt=cancel"/>        
        </div>
    
    </div>

</c:if>
<%--------------------------- New version popup -------------------------------------%>
<c:if test="${ creatable }">
	<f:message var="confirmNewVersionDialogTitle" key="requirement.new-version.confirm-dialog.title" />	
	<div id="confirm-new-version-dialog" class="not-displayed popup-dialog" title="${ confirmNewVersionDialogTitle }">
		<strong><f:message key="requirement.new-version.confirm-dialog.label" /></strong>
		<input type="button" value="${okLabel}" />
		<input type="button" value="${cancelLabel}" />
	</div>
  		
</c:if>	

<%------------------------------- confirm new status if set to obsolete popup---------------------%>
<c:if test="${status_editable}">
  
      <f:message var="statusChangeDialogTitle" key="dialog.requirement.status.confirm.title"/>
      <div id="requirement-status-confirm-dialog" class="not-displayed"
            title="${statusChangeDialogTitle}">
            
            <span><f:message key="dialog.requirement.status.confirm.text"/></span>
            
            <div class="popup-dialog-buttonpane">
              <input type="button" value="${confirmLabel}" data-def="mainbtn, evt=confirm"/>
              <input type="button" value="${cancelLabel}" data-def="evt=cancel" />
            </div>
      </div>
</c:if>
  
</div>
<%-- -----------------------------------/POPUPS ----------------------------------------------%>
<%-- -----------------------------------SCRIPT ----------------------------------------------%>

<script type="text/javascript">

	var identity = { resid : ${requirement.id}, restype : "requirements"  };
	
	require(["common"], function(){
		require(["jquery", "squash.basicwidgets", "contextual-content-handlers", 
		         "workspace.event-bus", "jquery.squash.fragmenttabs", "custom-field-values", 
		         "jquery.squash.confirmdialog", "jquery.squash.formdialog"], 
					function($, basicwidg,  contentHandlers, eventBus, Frag, cufvalues){
		$(function(){
				basicwidg.init();
				
				var nameHandler = contentHandlers.getNameAndReferenceHandler();
				
				nameHandler.identity = identity;
				nameHandler.nameDisplay = "#requirement-name";
				nameHandler.nameHidden = "#requirement-raw-name";
				nameHandler.referenceHidden = "#requirement-raw-reference";
				
				// ******** version creation **********
				
				var confirmHandler = function() {
					$.ajax({
						type : "post",
						url : "${ createNewVersionUrl }"
					}).done( function() {
						document.location.reload(true);
					});
				};
				
				var newversiondialog = $( "#confirm-new-version-dialog" );
				newversiondialog.confirmDialog({confirm: confirmHandler});
				
				$( "#new-version-button" ).on( "click", function() {
					newversiondialog.confirmDialog( "open" );
					return false;
				});
				
				// *********** status modification **********
				
				var statusChangeDialog = $("#requirement-status-confirm-dialog");
				statusChangeDialog.formDialog();
				
				statusChangeDialog.on('formdialogconfirm', function(){
					statusChangeDialog.formDialog('close');
					statusChangeDialog.data('confirm', true);
					var form = statusChangeDialog.data('callMeBack');
					form.submit();
				});
				
				statusChangeDialog.on('formdialogcancel', function(){
					statusChangeDialog.formDialog('close');
					statusChangeDialog.data('confirm', false);
					var form = statusChangeDialog.data('callMeBack');
					form.submit();				
				});
				
				// ************ rename dialog ********
				
				var renameDialog = $("#rename-requirement-dialog");
				renameDialog.formDialog();
				
				renameDialog.on('formdialogconfirm', function(){
					var url = "${ requirementUrl }",
						params = { newName : $("#rename-requirement-input").val() };
					
					$.ajax({
						url : url,
						type : 'POST',
						dataType : 'json',
						data : params
					}).success(function(json){
						renameDialog.formDialog('close');
						squashtm.requirement.renameRequirementSuccess(json);
					});
					
				});
				
				renameDialog.on('formdialogcancel', function(){
					renameDialog.formDialog('close');
				});
				
				renameDialog.on('formdialogopen', function(){
   					var name = $.trim($('#requirement-raw-name').text());
					$("#rename-requirement-input").val(name);
				});
				
				$("#rename-requirement-button").on('click', function(){
					renameDialog.formDialog('open');
				});
				
				//****** tabs configuration *******
	
				Frag.init();

				// ***** other events from the contextual content ********
				
				$("#print-requirement-version-button").click(function(){
					window.open("${currentVersionUrl}?format=printable", "_blank");
				});
				
				eventBus.onContextual('tc-req-links-updated', function(evt){
					$("#verifying-test-cases-table").squashTable().refresh();					
				});
				
			
				<c:if test="${hasCUF}">
				<%-- loading the custom fields --%>
				$.getJSON("${customFieldsValuesURL}?boundEntityId=${requirement.currentVersion.boundEntityId}&boundEntityType=${requirement.currentVersion.boundEntityType}")
				.success(function(jsonCufs){	
					var mode = '<c:out value="${writable ? 'jeditable' : 'static'}"/>';					
					cufvalues.infoSupport.init("#edit-requirement-table", jsonCufs, mode);
				});
		    	</c:if>

				
			});
		});
	});
	
	

		
</script>
<%-- -----------------------------------/ SCRIPT ----------------------------------------------%>

