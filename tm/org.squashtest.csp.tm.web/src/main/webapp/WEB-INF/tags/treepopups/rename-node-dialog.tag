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
<%@ tag body-content="empty" description="popup for node renaming. Requires a tree to be present in the context."%>


<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>


<%@ attribute name="treeSelector" description="jQuerySelector for the tree."%>
<%@ attribute name="successCallback" description="javascript callback in case of success."%>
<%@ attribute name="treeNodeButton" required="true" description="the javascript button that will open the dialog" %>

		
<pop:popup id="rename-node-dialog" titleKey="dialog.rename-tree-node.title" >
	<jsp:attribute name="buttons">
		<f:message var="label" key="tree.button.rename-node.label" />	
		'${ label }': function() {
			var url = $('${treeSelector}').data('selectedResourceUrl');
			<jq:ajaxcall url="url" dataType="json" httpMethod="POST" useData="true" successHandler="${successCallback}">
				<jq:params-bindings newName="#rename-tree-node-text" />
			</jq:ajaxcall>	
		},
	<pop:cancel-button />
	</jsp:attribute>
	<jsp:attribute name="additionalSetup">
		open : function(){
			var tree = $('${treeSelector}');
			var node = tree.jstree("get_selected");
			var operations = tree.jstree('allowedOperations');
			if (! operations.match("rename") ){
				<f:message key="dialog.label.rename-node.rejected" var="renameForbiddenLabel"/>
				$(this).dialog('close');
				displayInformationNotification("${renameForbiddenLabel }");
			}
			else{
				var name = node.attr('name');
				$("#rename-tree-node-text").val(name);
			}			
		}
	</jsp:attribute>

	<jsp:attribute name="body">
			
		<label for="rename-tree-node-text"><f:message key="dialog.rename.label" /></label>
		<input id="rename-tree-node-text" type="text" size="50" /> <br />
		<comp:error-message forField="name" />		
	</jsp:attribute>		
</pop:popup>	

<script type="text/javascript">
	$(function(){		
		${treeNodeButton}.click(function(){
			$('#rename-node-dialog').dialog('open');
			return false;		
		});		
	});


</script>