/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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


/**
 * Here is a good template your could use for your own milestone panels.
 * Please pay attention to the classes milestone-panel-X.
 * 
 * 	<div class="milestone-panel-master">
 * 		<div class="toolbar">
 * 			<input type="button" class="sq-btn milestone-panel-bind-button" value="+"/>
 * 			<input type="button" class="sq-btn milestone-panel-unbind-button" value="-"/>
 * 		</div>
 * 
 * 		<div class="table-tab-wrap">
 * 			
 * 			<table class="milestone-panel-table" data-def="...">
 * 				table definition goes here 
 * 			</table>
 * 
 * 		</div>
 * 
 * 		<div class="bind-milestone-dialog popup-dialog not-displayed">
 * 			add popup definition goes here, see file jquery.squash.milestoneDialog.js
 * 		</div>
 * 
 * 		<script>
 * 
 * 			var conf = {
 * 				see documentation below
 * 			}
 * 
 * 			require(["milestones/milestone-panel"], function(panel){
 * 				panel.init(conf);
 * 			});
 * 
 * 		</script>
 *
 * 	</div>
 * 
 * 
 * 
 * configuration :
 * 
 * {
 * 
 * 	basic : {
 * 		element : the jquery selector for the panel (optional),
 * 		identity : {
 * 			resid : the id of the test-case/whatever this panel sits in
 * 			restype : the type of entity : 'testcases etc'
 * 		}, 
 * 		currentModel : if provided, the table will be initialized with it
 * 	},
 * 
 * 	urls : {
 * 
 * 		currentTableSource : the url where to fetch the content for the table of currently bound milestones
 * 
 * 		bindTableSource : the url where to fetch the content for the table of not-yet bound milestones
 * 		
 * 		milestonesURL : the URL where to post or delete milestones binding 
 * 	},
 * 
 * 	permissions : {
 * 		editable : boolean, whether the user can or not edit the table content
 * 	}
 * 
 * }
 * 
 * 
 * Events : 
 *  
 * 	- node.unbindmilestones : the entity was removed from the scope of one or several milestones.
 * 							The event comes with a companion data : { identity : identity, milestones : [array, of, milestoneids] } 
 * 		
 * 
 */
define(["jquery", "workspace.event-bus", "app/ws/squashtm.notification", "squashtable", "./jquery.squash.milestoneDialog"], 
		function($, eventBus, notification){
	
	
	function init(conf){

		// now we begin		
		var element = $(conf.element || ".milestone-panel-master");
		
		var tblCnf = {
			bServerSide : false,
			iDeferLoading : conf.basic.currentModel.length,
			aaData : conf.basic.currentModel
		},
		squashCnf = {
				
		};
		
		
		var currentTable = element.find('.milestone-panel-table').squashTable(tblCnf, squashCnf);
		
		
		if (conf.permissions.editable){
			
			// add milestones dialog
			var dialogOptions = {
				tableSource : conf.urls.bindTableSource,
				milestonesURL : conf.urls.milestonesURL
			};
			
			var bindDialog = element.find('bind-milestone-dialog');
			bindDialog.milestoneDialog(dialogOptions);
			
			$(".milestone-panel-bind-button").on('click', function(){
				bindDialog.milestoneDialog('open');
			});
			
			eventBus.onContextual('node.bindmilestones', function(){
				currentTable.refresh();
			});
			
			// remove milestones 
			
			$(".milestone-panel-unbind-button").on('click', function(){
				var ids = currentTable.getSelectedIds();
				
				if (ids.length === 0){
					notification.showError('select some milestones ya troll');
				}
				else{
					
					var url = conf.urls.milestonesURL + '/' + ids.join(',');
					
					$.ajax({
						url : url,
						type : 'DELETE'
					})
					.success(function(){
						eventBus.trigger('node.unbindmilestones', {
							identity : conf.basic.identity,
							milestones : [ids]
						});
					});
				}
			});
			
		}		
		
	}
	
	
	return {
		init : init 
	};
	
	
});
