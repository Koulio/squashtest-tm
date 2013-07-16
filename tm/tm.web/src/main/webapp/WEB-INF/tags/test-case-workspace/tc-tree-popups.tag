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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>

<c:set var="servContext" value="${ pageContext.servletContext.contextPath }"/>

<f:message var="addFolderTitle"  	key="dialog.new-folder.title"/>	
<f:message var="addTestCaseTitle"  	key="dialog.new-test-case.title"/>	
<f:message var="renameNodeTitle"	key="dialog.rename-tree-node.title" />
<f:message var="deleteNodeTitle"	key="dialog.delete-tree-node.title"/>
<f:message var="addLabel"		 	key="label.Add"/>
<f:message var="addAnotherLabel"	key="label.addAnother"/>
<f:message var="cancelLabel"		key="label.Cancel"/>
<f:message var="confirmLabel"		key="label.Confirm"/>

<f:message var="deleteMessagePrefix"	key="dialog.label.delete-node.label.start" />
<f:message var="deleteMessageVariable"  key="dialog.label.delete-nodes.test-cases.label"/>
<f:message var="deleteMessageSuffix"	key="dialog.label.delete-node.label.end"/>
<f:message var="deleteMessageNoUndo"	key="dialog.label.delete-node.label.cantbeundone" />
<f:message var="deleteMessageConfirm"	key="dialog.label.delete-node.label.confirm" />


<div id="treepopups-definition" class="not-displayed">

<div id="add-folder-dialog" class="popup-dialog not-displayed" title="${addFolderTitle}">
	<table class="add-node-attributes">
		<tr>
			<td><label for="add-folder-name"><f:message key="label.Name" /></label></td>
			<td><input id="add-folder-name" type="text" size="50" maxlength="255" /><br />
				<comp:error-message forField="name" />
			</td>
		</tr>
		<tr>
			<td><label for="add-foldder-description"><f:message key="label.Description" /></label></td>
			<td><textarea id="add-folder-description" data-def="isrich"></textarea></td>
		</tr>
	</table>	
	<div class="popup-dialog-buttonpane">
		<input 	type="button" value="${addAnotherLabel}"   	data-def="evt=add-another, mainbtn"/>
		<input 	type="button" value="${addLabel}" 			data-def="evt=add-close"/>
		<input  type="button" value="${cancelLabel}" 		data-def="evt=cancel"/>
	</div>
</div>


<div id="add-test-case-dialog" class="popup-dialog not-displayed" title="${addTestCaseTitle}">
	<table class="add-node-attributes">
		
		<tr>
			<td><label for="add-test-case-name"><f:message key="label.Name" /></label></td>

			<td><input id="add-test-case-name" type="text" size="50" maxlength="255" /><br />
				<comp:error-message forField="name" />
			</td>
		</tr>
		
		<tr>
			<td><label for="add-test-case-reference"><f:message key="label.Reference" /></label></td>
			<td><input id="add-test-case-reference" type=text size="15" maxlength="20"/><br />
				<comp:error-message forField="reference" />	<td>
		</tr>
					
		<tr>
			<td><label for="add-test-case-description"><f:message key="label.Description" /></label></td>
			<td><textarea id="add-test-case-description" data-def="isrich"></textarea></td>
		</tr>
	</table>	
	<div class="popup-dialog-buttonpane">
		<input 	type="button" value="${addAnotherLabel}"   	data-def="evt=add-another, mainbtn"/>
		<input 	type="button" value="${addLabel}" 			data-def="evt=add-close"/>
		<input  type="button" value="${cancelLabel}" 		data-def="evt=cancel"/>
	</div>
</div>


<div id="rename-node-dialog" class="popup-dialog not-displayed" title="${renameNodeTitle}" >

	<span data-def="state=denied">
		<f:message key="dialog.label.rename-node.rejected" />		
	</span>
	
	<div data-def="state=confirm">
		<label for="rename-tree-node-text"><f:message key="dialog.rename.label" /></label>
		<input id="rename-tree-node-text" type="text" size="50" /> <br />
		<comp:error-message forField="name" />
	</div>
	
	<div class="popup-dialog-buttonpane">
		<input type="button" value="${confirmLabel}" 		data-def="evt=confirm, mainbtn=confirm, state=confirm"/>
		<input  type="button" value="${cancelLabel}" 		data-def="evt=cancel, mainbtn"/>
	</div>	

</div>


<div id="delete-node-dialog" class="popup-dialog not-displayed" title="${deleteNodeTitle}">
	
	<div class="waiting-loading" data-def="state=pleasewait"></div>
	
	<div class="not-displayed" data-def="state=confirm">
	
		<div class="display-table-row">
			<div class="display-table-cell delete-node-dialog-warning">
				<!-- content is empty on purpose, let it display a background image. -->
			</div>
			<div class="display-table-cell">
				<p>
					<c:out value="${deleteMessagePrefix}"/>
					<span class='red-warning-message'><c:out value="${deleteMessageVariable}" /></span> 
					<c:out value="${deleteMessageSuffix}" />
				</p>
				
				<div class="not-displayed delete-node-dialog-details">
					<p><f:message key="dialog.delete-tree-node.details"/></p>
					<ul>
					</ul>				
				</div>
				
				<p>
					<span><c:out value="${deleteMessageNoUndo}"/></span>				
					<span class='bold-warning-message'><c:out value="${deleteMessageConfirm}"/></span>				
				</p>				
				
			</div>
		</div>
	</div>
		
	<div class="not-displayed" data-def="state=rejected">
		<f:message key="dialog.label.delete-node.rejected"/>
	</div>
	
	<div class="popup-dialog-buttonpane">
		<input type="button" value="${confirmLabel}" data-def="evt=confirm, mainbtn=confirm, state=confirm"/>
		<input type="button" value="${cancelLabel}"  data-def="evt=cancel,  mainbtn=rejected"/>
	</div>
</div>


</div>