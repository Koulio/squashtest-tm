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
define(['jquery', 'tree', 'custom-field-values', 'workspace.projects', 'jquery.squash.formdialog'], 
		function($, zetree, cufValuesManager, projects){
	
	function postNode(dialog, tree){
		
		var params = {
			name : dialog.find('#add-campaign-name').val(),
			description : dialog.find('#add-campaign-description').val()
		};
		
		var cufParams = dialog.data('cuf-values-support').readValues();
		
		$.extend(params, cufParams);
		
		return tree.jstree('postNewNode', 'new-campaign', params, true);
	}
	
	
	function addCufHandler(dialog, tree){
		var table = dialog.find('table.add-node-attributes');
		var cufHandler = cufValuesManager.newCreationPopupCUFHandler({table : table});
		
		dialog.on('formdialogopen', function(){
			var projectId = tree.jstree('get_selected').getProjectId();
			var bindings = projects.findProject(projectId).customFieldBindings['CAMPAIGN'];
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
		
		var dialog = $("#add-campaign-dialog").formDialog();
		var tree = zetree.get();
		
		
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