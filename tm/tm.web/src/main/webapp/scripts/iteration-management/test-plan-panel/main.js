/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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

/*
 * configuration an object as follow :
 * 
 * {
 *		permissions : {
 *			linkable : boolean, can items be added or removed ?
 *			editable : boolean, is the table content editable ?
 *			executable : boolean, can the content be executed ?	
 *		},
 *		basic : {
 *			iterationId : the id of the current iteration
 *			assignableUsers : [ { 'id' : id, 'login' : login } ]
 *		}
 *	}
 * 
 */

define(['squash.translator', './table', './popups'], function(translator, table, popups){
	
	function enhanceConfiguration(origconf){
		
		var conf = $.extend({}, origconf);
		
		var baseURL = squashtm.app.contextRoot;
		
		conf.messages = translator.get({
			executionStatus : {
				UNTESTABLE : "execution.execution-status.UNTESTABLE",
				BLOCKED : "execution.execution-status.BLOCKED",
				FAILURE : "execution.execution-status.FAILURE",
				SUCCESS : "execution.execution-status.SUCCESS",
				RUNNING : "execution.execution-status.RUNNING",
				READY  : "execution.execution-status.READY"
			},
			automatedExecutionTooltip : "label.automatedExecution",
			labelOk : "label.Ok",
			labelCancel : "label.Cancel",
			titleInfo : "popup.title.Info",
			messageNoAutoexecFound : "dialog.execution.auto.overview.error.none",
			unauthorizedTestplanRemoval : "dialog.remove-testcase-association.unauthorized-deletion.message"
		});
		
		conf.urls = {
			 testplanUrl : baseURL + '/iterations/'+conf.basic.iterationId+'/test-plan/',
			 executionsUrl : baseURL + '/executions/',
			 testplanManagerUrl : baseURL + '/iterations/' + conf.basic.iterationId +'/test-plan-manager'
		};
		
		return conf;
	}
	
	function _bindButtons(conf){
		if (conf.permissions.linkable){
			$("#navigate-test-plan-manager").on('click', function(){
				document.location.href = conf.urls.testplanManagerUrl;
			});
			$("#remove-test-plan-button").on('click', function(){
				$("#iter-test-plan-delete-dialog").formDialog('open');
			});
		}
		
		if (conf.permissions.editable){
			$("#assign-users-button").on('click', function(){
				$("#iter-test-plan-batch-assign").formDialog('open');
			});
		}
		
		if (conf.permissions.reorderable){
			$("#reorder-test-plan-button").on('click', function(){
				$("#iter-test-plan-reorder-dialog").confirmDialog('open');
			});
		}
	}
	
	return {
		init : function(origconf){
			
			var conf = enhanceConfiguration(origconf);
			
			table.init(conf);
			popups.init(conf);
			
			_bindButtons(conf);
			
		}
	};	
	
});