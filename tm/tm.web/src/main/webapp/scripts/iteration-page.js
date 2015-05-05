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
require([ "common" ], function() {
	"use strict";

	require([ "jquery", "underscore", "app/pubsub", "squash.basicwidgets", "contextual-content-handlers",
			"jquery.squash.fragmenttabs", "bugtracker/bugtracker-panel", "workspace.event-bus", "iteration-management",
			"app/ws/squashtm.workspace", "custom-field-values", "test-automation/auto-execution-buttons-panel", "jquery.squash.formdialog" ],
			function($, _, ps, basicwidg, contentHandlers, Frag, bugtracker, eventBus, itermanagement, WS, cufvalues) {

		// *********** event handler ***************

		var refreshTestPlan = _.bind(function() {
			console.log("squashtm.execution.refresh");
			$("#iteration-test-plans-table").squashTable().refresh();
		}, window);

		squashtm.execution = squashtm.execution || {};
		squashtm.execution.refresh = refreshTestPlan;

		// this is executed on each fragment load
		ps.subscribe("reload.iteration", function() {
			var config = _.extend({}, squashtm.page);

			config = _.defaults(config, {
				isFullPage : false,
				hasBugtracker : false,
				hasFields : false
			});

			WS.init();
			basicwidg.init();

			var nameHandler = contentHandlers.getSimpleNameHandler();
			nameHandler.identity = squashtm.page.identity;
			nameHandler.nameDisplay = "#iteration-name";

			// todo : uniform the event handling.
			// rem : what does it mean ?
			itermanagement.initEvents();

			// ****** tabs configuration *******

			var fragConf = {
					active : 4,
					cookie : "iteration-tab-cookie",
					activate : function(event, ui) {
						if (ui.newPanel.is("#dashboard-iteration")) {
							eventBus.trigger("dashboard.appear");
						}
					}
			};
			Frag.init(fragConf);

			if (config.hasBugtracker) {
				bugtracker.load(config.bugtracker);
			}

			if (config.hasFields) {
				var url = config.customFields.url;
				$.getJSON(url)
				.success(function(jsonCufs){
					$("#iteration-custom-fields-content .waiting-loading").hide();
					var mode = (config.writable) ? "jeditable" : "static";
					cufvalues.infoSupport.init("#iteration-custom-fields-content", jsonCufs, mode);
				});
			}
			
			
			// ******** rename popup *************
			
			var renameDialog = $("#rename-iteration-dialog");
			renameDialog.formDialog();
			
			renameDialog.on('formdialogopen', function(){
				var name = $.trim($("#iteration-name").text());
				$("#rename-iteration-name").val(name);			
			});
			
			renameDialog.on('formdialogconfirm', function(){
				$.ajax({
					url : config.iterationURL,
					type : 'POST',
					dataType : 'json',
					data : { "newName" : $("#rename-iteration-name").val() }
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
			
			$("#rename-iteration-button").on('click', function(){
				renameDialog.formDialog('open');
			});

			// ********** dashboard **************
			itermanagement.initDashboardPanel({
				master : "#dashboard-master",
				cacheKey : "it" + config.identity.resid
			});


			console.log("iteration-page refresh.iteration");
		});
		console.log("iteration-page.js loaded");
	});
});
