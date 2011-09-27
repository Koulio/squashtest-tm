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
<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags/jquery" prefix="jq" %>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates" %>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>

<comp:decorate-toggle-panels />
<comp:rich-jeditable-header />
<dt:datatables-header />
<jq:execution-status-factory/> 

<%-- ----------------------------------- Authorization ----------------------------------------------%>
<c:set var="editable" value="${ false }" /> 
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ execution }">
	<c:set var="editable" value="${ true }" /> 
</authz:authorized>


<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/dateformat.js"></script>


<%-------------------------- urls ------------------------------%>


<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />

<s:url var="executionUrl" value="/executions/{execId}">
	<s:param name="execId" value="${execution.id}" />
</s:url>

<s:url var="executionInfoUrl" value="/executions/{execId}/general">
	<s:param name="execId" value="${execution.id}" />
</s:url>

<s:url var="executeExecutionUrl" value="/execute/{execId}">
	<s:param name="execId" value="${execution.id}"/>
</s:url>

<s:url var="ieoExecutionUrl" value="/execute/{execId}/ieo">
	<s:param name="execId" value="${execution.id}"/>
</s:url>

<s:url var="executionStepsUrl" value="/executions/{execId}/steps">
	<s:param name="execId" value="${execution.id}" />
</s:url>



<s:url var="stepAttachmentManagerUrl" value="/attach-list/">
</s:url>


<s:url var="btEntityUrl" value="/bugtracker/execution/{id}" >
	<s:param name="id" value="${execution.id}"/>
</s:url>



<%-------------------------- /urls ------------------------------%>

<script type="text/javascript">

	/* simple initialization for simple components */
	$(function(){
		$('#delete-execution-button').button();
		$('#execute-execution-button').button();
		$('#ieo-execution-button').button();
	});
	
	/* display the execution name. Used for extern calls (like from the page who will include this fragment)
	*  will refresh the general informations as well*/
	function nodeSetName(name){
		var fullname = name;
		$('#execution-name').html(fullname);		
	}
	
	/* renaming success handler */
	function renameExecutionSuccess(data){
		nodeSetName(data.newName);
						
		$( '#rename-execution-dialog' ).dialog( 'close' );
	}
	
	/* deletion success handler */
	function deleteExecutionSuccess(){
		$( '#delete-execution-confirm' ).dialog( 'close' );
		document.location.href="${parentUrl}" ;				
	}
	
	/* deletion failure handler */
	function deleteExecutionFailure(xhr){
		alert(xhr.statusText);		
	}
</script>

<script type="text/javascript">
	$(function(){
		$("#back").button().click(function(){
			//document.location.href="${referer}";
			history.back();
		});
		
		$("#execute-execution-button").click(function(){
			var url="${executeExecutionUrl}";
			popup = window.open(url,"test", "height=500, width=600, resizable, scrollbars, dialog, alwaysRaised");
		});
		
		$("#ieo-execution-button").click(function(){
			var url="${ieoExecutionUrl}";
			newWindow = window.open(url,"test");
		});
	});
</script>

<script type="text/javascript">
	/* ******** step datatable additional javascript *** */
	
	
	
	function execStepTableDrawCallback(){
		<c:if test="${ editable }">
		decorateAttachmentButtons($('.manage-attachment-button', this));
		decorateEmptyAttachmentButtons($('.manage-attachment-button-empty', this));
		</c:if>
		convertDate(this);
		convertStatus(this);
		<c:if test="${ editable }">
		makeEditable(this);
		</c:if>
		addSmallFontsToCommentary(this);
	}
	
	function execStepTableRowCallback(row, data, displayIndex){
		<c:if test="${ editable }">
		addAttachmentButtonToRowDoV(row, getStepsTableAttchNumber(data), 'manage-attachment-button', 'manage-attachment-button-empty');
		</c:if>
		return row;
	}
	
	//return the number of attachment
	function getStepsTableAttchNumber(rowData) {
		return rowData[8];
	}
	
	function convertDate(dataTable){
		
		//internationalize that thing later
		var strFormat = "dd/mm/yyyy HH:MM";
		
		var rows=dataTable.fnGetNodes();
		if (rows.length==0) return;
		
		$(rows).each(function(){
			var col=$("td:eq(4)", this);
			var strTime=col.html();
			var iTime=parseInt(strTime);
			if (! isNaN(iTime)){
				var newDate = new Date(parseInt(iTime));
				col.html(newDate.format(strFormat));
			}else{
				col.html("<f:message key="auditable-entity.never-modified.label" />");
			}

		});
	}
	
	function convertStatus(dataTable){
		var factory = new ExecutionStatusFactory();
		
		var rows=dataTable.fnGetNodes();
		if (rows.length==0) return;
		
		$(rows).each(function(){
			var col=$("td:eq(3)", this);
			var oldContent=col.html();
			
			var newContent = factory.getDisplayableStatus(oldContent);	
			
			col.html(newContent);
			
		});		
	}
	
	function makeEditable(dataTable){
		
		var rows = dataTable.fnGetNodes();
		/* check if the data is fed */
		if (rows.length==0) return;
		
		$(rows).each(function(){
			var data = dataTable.fnGetData(this);
			var esId = data[0];	
			var columnComment = $("td:eq(6)", this);
			turnToEditable(columnComment,"comment",esId);

			
		});

	}
	
	function turnToEditable(jqOColumn,strTarget,iid){
		
		var content =$(jqOColumn).html();
		var tdId = "esTable"+strTarget+"-"+iid;
		<%--$(jqOColumn).html("<div id=\""+tdId+"\">"+content+"</div>");--%>
		$(jqOColumn).html(content);
		
		var stUrl = "${ executionStepsUrl }/" + iid+"/"+strTarget; 
		
		<%--$( "#"+tdId ).editable(  stUrl, {--%>
		$(jqOColumn).editable( stUrl, {
			type: 'ckeditor',												//this input type isn't standard, refers to jquery.jeditable.ckeditor.js
			ckeditor : { customConfig : '${ ckeConfigUrl }', language: '<f:message key="rich-edit.language.value" />' },				//same comment
			rows: 10, 
			cols: 80,
			placeholder: '<f:message key="rich-edit.placeholder" />',
			submit: '<f:message key="rich-edit.button.ok.label" />',
			cancel: '<f:message key="rich-edit.button.cancel.label" />',
			onblur : function(){},											//this disable the onBlur handler, which would close the jeditable 
																			//when clicking in the rich editor (since it considers the click as 
																			//out of the editing zone)
			callback : function(value, settings){},
			indicator : '<img src="${ pageContext.servletContext.contextPath }/images/indicator.gif" alt="processing..." />' 
			
		});		
		
	}
	<%-- manage attachment button --%>
	$(function() {
		$('#execution-execution-steps-table .has-attachment-cell a').live('click', function() {
			var listId = parseStepListId(this);
			document.location.href = "${stepAttachmentManagerUrl}" + listId + "/attachments/manager?workspace=campaign";
		});
		
	});
	
	function parseStepListId(element) {
		var elementId = element.id;
		return elementId.substr(elementId.indexOf(":") + 1);
	}
	
	function addSmallFontsToCommentary(dataTable){
		var rows = dataTable.fnGetNodes();
		if (rows.length>0){
			$(rows).find("td:eq(6)").addClass("smallfonts");
		}
	}

</script>

<div class="ui-widget-header ui-state-default ui-corner-all fragment-header">

	<div style="float:left;height:100%;">
		<h2>
			<label for="execution-name"><f:message key="execution.execute.header.title" /></label><a id="execution-name" href="${ executionUrl }/info">&#35;<c:out value="${executionRank} - ${ execution.name }" escapeXml="true"/></a>
		</h2>
	</div>
	<c:if test="${param.hasBackButton}">
		<div style="float:right;">
			<f:message var="back" key="fragment.edit.header.button.back" />
			<input id="back" type="button" value="${ back }" />
		</div>	
	</c:if>
	
	<div style="clear:both;"></div>
</div>

<div class="fragment-body">

<div id="execution-toolbar" class="toolbar-class ui-corner-all " >
	<div  class="toolbar-information-panel">
		<div id="general-informations-panel">
			<comp:execution-information-panel auditableEntity="${execution}"/>
		</div>
	</div>
	<div class="toolbar-button-panel">
		<c:choose>
			<c:when test="${execution.executionStatus == 'READY'}">
				<f:message var="executeBtnLabel" key="execution.execute.start.button.label"/>
			</c:when>
			<c:otherwise>
				<f:message var="executeBtnLabel" key="execution.execute.resume.button.label"/>
			</c:otherwise>
		</c:choose>
		<c:if test="${ editable }">
			<input type="button" value="<f:message key="execution.execute.IEO.button.label" />" id="ieo-execution-button"/>
			<input type="button" value="${executeBtnLabel}" id="execute-execution-button"/>
			<%--
			<input type="button" value='<f:message key="execution.execute.remove.button.label" />' id="delete-execution-button" />
			 --%>
		</c:if>
	</div>	
	<div style="clear:both;"></div>	
</div>



<c:if test="${ editable }">
	<comp:rich-jeditable targetUrl="${ executionUrl }" componentId="execution-description"/>
</c:if>

<f:message var="executionComment" key="execution.description.panel.title"/>
<comp:toggle-panel title="${executionComment}" isContextual="true"  open="false">
	<jsp:attribute name="body">
		<div id="execution-description" >${ execution.description }</div>
	</jsp:attribute>
</comp:toggle-panel>

<%-------------------------execution step summary status---------------------------------------%>

<comp:toggle-panel titleKey="executions.execution-steps-summary.panel.title" isContextual="true" open="true">
	<jsp:attribute name="body">
		<comp:decorate-ajax-table url="${executionStepsUrl}" tableId="execution-execution-steps-table" paginate="true">		
			<jsp:attribute name="drawCallback">execStepTableDrawCallback</jsp:attribute>
			<jsp:attribute name="rowCallback">execStepTableRowCallback</jsp:attribute>
			<jsp:attribute name="columnDefs">
				<dt:column-definition targets="0" visible="false" width="2em"/>
				<dt:column-definition targets="1" visible="true" cssClass="select-handle centered" width="2em"/>
				<dt:column-definition targets="2,3,4,5,6" visible="true"/>
				<dt:column-definition targets="7" visible="true"/>
				<dt:column-definition targets="8" visible="false"/>
				<c:if test="${ editable }">
					<dt:column-definition targets="9" visible="true" lastDef="true" width="2em" cssClass="centered has-attachment-cell" />
				</c:if>
				<c:if test="${ !editable }">
					<dt:column-definition targets="9" visible="false" lastDef="true" width="2em" cssClass="centered has-attachment-cell" />
				</c:if>
			</jsp:attribute>					
		</comp:decorate-ajax-table>
		<table id="execution-execution-steps-table">
			<thead>
				<tr>
					<th>Id</th>
					<th><f:message key="executions.steps.table.column-header.rank.label" /></th>
					<th><f:message key="executions.steps.table.column-header.action.label" /></th>		
					<th><f:message key="executions.steps.table.column-header.expected-result.label" /></th>
					<th><f:message key="executions.steps.table.column-header.status.label" /></th>
					<th><f:message key="executions.steps.table.column-header.last-execution.label" /></th>
					<th><f:message key="executions.steps.table.column-header.user.label" /></th>		
					<th><f:message key="executions.steps.table.column-header.comment.label" /></th>
					<th>numberOfAttch(masked)</th>
					<th><f:message key="executions.steps.table.column-header.attachment.label" /></th>
				</tr>
			</thead>
			<tbody>
				<%-- Will be populated through ajax --%>
			</tbody>
		</table>
		<br />
		<div id="execution-step-row-buttons" class="not-displayed">
			<a id="manage-attachment-button" href="#" class="manage-attachment-button"><f:message key="execution.step.manage-attachment.label" /></a>
			<a id="manage-attachment-button-empty" href="#" class="manage-attachment-button-empty"><f:message key="execution.step.add-attachment.label" /></a>
		</div>
	</jsp:attribute>
</comp:toggle-panel>



<%------------------------------ Attachments bloc ---------------------------------------------%> 

<comp:attachment-bloc entity="${execution}" workspaceName="campaign" editable="${ editable }" />


<%------------------------------ bugs section -------------------------------%>
<%--
	this section is loaded asynchronously. The bugtracker might be out of reach indeed.
 --%>	
 <script type="text/javascript">
 	$(function(){
 		$("#bugtracker-section-div").load("${btEntityUrl}");
 	});
 </script>
<div id="bugtracker-section-div">
</div>

<%------------------------------ /bugs section -------------------------------%>


<comp:decorate-buttons />

<%--------------------------- Deletion confirmation popup -------------------------------------%>
<%-- 
*
* unused yet.
*

<comp:popup id="delete-execution-confirm" titleKey="dialog.delete-execution.title" 

isContextual="true" openedBy="delete-execution-button">
	<jsp:attribute name="buttons">
		<f:message var="label" key="dialog.button.confirm" />
		'${ label }': function() {
			var url = "${executionUrl}";
			<jq:ajaxcall 
				url="url"
				httpMethod="DELETE"
				successHandler="deleteExecutionSuccess"
				errorHandler="deleteExecutionFailure" >					
			</jq:ajaxcall>
		},			
		<pop:cancel-button />
	</jsp:attribute>
	<jsp:body>
		<b><f:message key="dialog.delete-execution.message" /></b>
		<br/>				
	</jsp:body>
</comp:popup>
 --%>
<%--------------------------- /Deletion confirmation popup -------------------------------------%>

</div>






    
