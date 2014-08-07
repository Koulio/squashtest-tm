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
require([ "common" ], function() {
	require(["jquery", "app/pubsub", "squash.translator", "squash.basicwidgets", "workspace.event-bus",
	         "app/ws/squashtm.workspace", "contextual-content-handlers", "jquery.squash.fragmenttabs",
	         "bugtracker/bugtracker-panel", "test-suite-management", "test-suite/execution-buttons-panel",
	         "test-automation/auto-execution-buttons-panel", "jquery.cookie"],
			function($, ps, messages, basicwidg, eventBus, WS, contentHandlers, Frag, bugtracker, tsmanagement){
		"use strict";

		$(document).on("click", "#duplicate-test-suite-button", function() {
			console.log("click", "#duplicate-test-suite-button");
			$( "#confirm-duplicate-test-suite-dialog" ).confirmDialog( "open" );
			return false;
		});

		/* post a request to duplicate the test suite */
		/* should be put in global ns and referenced someplace */
		function duplicateTestSuite(){
			return $.ajax({
				"url" : squashtm.page.api.copy,
				type : "POST",
				data : [],
				dataType : "json"
			});
		}
		
		// ******** rename popup *************
		
		var renameDialog = $("#rename-testsuite-dialog");
		renameDialog.formDialog();
		
		renameDialog.on('formdialogopen', function(){
			var name = $.trim($("#test-suite-name").text());
			$("#rename-test-suite-name").val(name);			
		});
		
		renameDialog.on('formdialogconfirm', function(){
			$.ajax({
				url : config.testSuiteURL,
				type : 'POST',
				dataType : 'json',
				data : { "newName" : $("#rename-test-suite-name").val() }
			})
			.done(function(json){
				renameDialog.formDialog('close');
				
				eventBus.trigger("node.rename", {
					identity : config.identity,
					newName : json.newName
				});
			});
		});
		
		renameDialog.on('formdialogcancel', function(){
			renameDialog.formDialog('close');
		});
		
		$("#rename-test-suite-button").on('click', function(){
			renameDialog.formDialog('open');
		});

		/* duplication sucess handler */
		/* should be put in global ns and referenced someplace */
		/* should be refreshed */

		var duplicateTestSuiteSuccess;

		if (config.isFullPage) {
			duplicateTestSuiteSuccess = function(idOfDuplicate) {
				$.squash.openMessage(messages.get("test-suite.duplicate.success.title"), messages.get("test-suite.duplicate.success.message"));
			};
		} else {
			duplicateTestSuiteSuccess = function(idOfDuplicate) {
				eventBus.trigger("node.add", {
					parent : squashtm.page.parentIdentity,
					child :{
						resid : idOfDuplicate,
						rel : "test-suite"
					}
				});
			};
		}

		squashtm.execution = squashtm.execution || {};
		squashtm.execution.refresh = function() {
			eventBus.trigger("context.content-modified");
		};

		ps.subscribe("reload.test-suite", function(){
			var config = _.extend({}, squashtm.page);

			config = _.defaults(config, {
				isFullPage : false,
				hasBugtracker : false,
				hasFields : false
			});

			WS.init();
			basicwidg.init();

			// registers contextual events
			// TODO should be unregistered before ?
			function refreshExecButtons() {
				console.log("refreshExecButtons");
				var $panel = $("#test-suite-exec-btn-group");
				$panel.load($panel.data("content-url"));
			}

			eventBus.onContextual("context.content-modified", refreshExecButtons);
			eventBus.onContextual("context.content-modified", refreshStatistics); // WTF window namespace alert !
			eventBus.onContextual("context.content-modified", function() {
				$("#test-suite-test-plans-table").squashTable().refresh();
			});

			// some other pre-whichever-refactor event handling
			var nameHandler = contentHandlers.getSimpleNameHandler();

			nameHandler.identity = squashtm.page.identity;
			nameHandler.nameDisplay = "#test-suite-name";

			// ****** tabs configuration *******

			var fragConf = {
				cookie : "iteration-tab-cookie"
			};
			Frag.init(fragConf);

			if (config.hasBugtracker) {
				bugtracker.load(config.bugtracker);
			}

			if (config.hasFields) {
				$("#test-suite-custom-fields-content").load(config.customFields.url+"?boundEntityId="+config.identity.resid+"&boundEntityType=TEST_SUITE");
			}

			var dialog = $( "#confirm-duplicate-test-suite-dialog" );

			var confirmHandler = function() {
				dialog.confirmDialog("close");
				duplicateTestSuite().done(function(json){
					duplicateTestSuiteSuccess(json);
				});
			};

			dialog.confirmDialog({confirm: confirmHandler});
			console.log("test-suite-page refresh");
		});
		console.log("test-suite-page.js loaded");
	});

});
