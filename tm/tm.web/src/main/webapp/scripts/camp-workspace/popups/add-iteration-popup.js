/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
define(['jquery', 'tree', 'custom-field-values', 'workspace.projects', '../permissions-rules', 'jquery.squash.formdialog'], 
		function($, zetree, cufValuesManager, projects, rules){
	
	function postNode(dialog, tree){
		
		var params = {
			name : dialog.find('#add-iteration-name').val(),
			description : dialog.find('#add-iteration-description').val(),
			copyTestPlan : dialog.find("#copy-test-plan-box").is(':checked')
		};
		
		var cufParams = dialog.data('cuf-values-support').readValues();
		
		$.extend(params, cufParams);
		
		return tree.jstree('postNewNode', 'new-iteration', params, true);
	}
	
	
	function addCufHandler(dialog, tree){
		var table = dialog.find('table.add-node-attributes');
		var cufHandler = cufValuesManager.newCreationPopupCUFHandler({table : table});
		
		dialog.on('formdialogopen', function(){
			var projectId = tree.jstree('get_selected').getProjectId();
			var bindings = projects.findProject(projectId).customFieldBindings['ITERATION'];
			var cufs = $.map(bindings, function(b){return b.customField;});
			
			cufHandler.loadPanel(cufs);	
		});
		
		dialog.on('formdialogcleanup', function(){
			cufHandler.reset();
		});
		
		dialog.on('formdialogclose', function(){
			cufHandler.destroy();
		});
		
		dialog.data('cuf-values-support', cufHandler);
	}
	
	function init(){
		
		var dialog = $("#add-iteration-dialog").formDialog();
		var tree = zetree.get();
		
		// Added to cancel the open if no rights
		dialog.on('formdialogopen', function(){
			var node = tree.jstree('get_selected');
			
			if (! rules.canCreateIteration(node)){
				dialog.formDialog('setState','denied');
			}
			else{
				dialog.formDialog('setState','confirm');
				var name = node.getName();
				dialog.find("#new-iteration-tree-button").val(name);				
			}			
		});
		
		dialog.on('formdialogconfirm', function(){
			var node = tree.jstree('get_selected');
			var url = node.getResourceUrl();
			var name = dialog.find("#new-iteration-tree-button").val();
			
			$.post(url, {newName : name}, null, 'json')
			.done(function(){
				eventBus.trigger("node.rename", { identity : node.getIdentity(), newName : name});
				dialog.formDialog('close');
			});
			
		});
		
		// end

		dialog.on('formdialogadd-close', function(){
			postNode(dialog,tree).then(function(){
				dialog.formDialog('close');
			});			
		});
		
		dialog.on('formdialogadd-another', function(){
			postNode(dialog, tree).then(function(){
				dialog.formDialog('cleanup');
			}) ;		
		});
		
		dialog.on('formdialogcancel', function(){
			dialog.formDialog('close');
		});
		
		
		addCufHandler(dialog, tree);
		
	}
	
	return {
		init : init
	};

});