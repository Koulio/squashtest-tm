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
define([ "jquery", "backbone", "underscore",
         "./GeneralInfosPanel",
         "./PrerequisitePanel",
         "../../test-automation/testcase-test-automation",
         "../../verified-requirements/TestCaseVerifiedRequirementsPanel",
         "bugtracker/bugtracker-panel",
         "workspace.event-bus",
         "squash.translator",
         "squashtable"], function($,
		Backbone, _, GeneralInfosPanel, PrerequisitePanel, testcaseTestAutomation,  TestCaseVerifiedRequirementsPanel, bugtrackerPanel, eventBus, translator) {


	var InfoTab = Backbone.View.extend({

		el : "#tab-tc-informations",

		initialize : function() {
			var self = this;
			this.settings = this.options.settings;
			this.sendUpdateReqToTree = $.proxy(this._sendUpdateReqToTree, this);
			this.sendUpdateHasStepsToTree = $.proxy(this._sendUpdateHasStepsToTree, this);

			// general infos
			this.generalInfosPanel = new GeneralInfosPanel({
				settings : this.settings,
				parentTab : this
			});

			// prerequisite
			this.prerequisitePanel = new PrerequisitePanel({
				settings : this.settings,
				parentTab : this
			});

			// test automation
			this._initTestAutomation();

			// verified requirements
			this.verifiedRequirementsPanel = new TestCaseVerifiedRequirementsPanel();
			this.listenTo(this.verifiedRequirementsPanel.table, "verifiedrequirementversions.refresh", this.generalInfosPanel.refreshImportanceIfAuto);
			this.listenTo(this.verifiedRequirementsPanel.table, "verifiedrequirementversions.refresh", this.sendUpdateReqToTree);

			// calling test cases table
			var tablesettings = {
				'aaData' : this.settings.callingTestCases
			};
			this.callingTestCasesTable = this.$el.find("#calling-test-case-table")
											.squashTable(tablesettings, {});

			// CUFs
			if (this.settings.hasCufs){
				$.get(this.settings.urls.cufValuesUrl)
				.success(function(data){
					$("#test-case-description-table").append(data);
				});
			}

			// bugtracker
			if (this.settings.hasBugtracker){
				bugtrackerPanel.load({
					url : this.settings.urls.bugtrackerUrl,
					label : translator.get('tabs.label.issues')
				});
			}

			// application-wide events

			eventBus.onContextual("testStepsTable.pastedCallSteps", this.verifiedRequirementsPanel.table.refreshRestore);
			eventBus.onContextual("testStepsTable.deletedCallSteps", this.verifiedRequirementsPanel.table.refreshRestore);
			eventBus.onContextual("testStepsTable.noMoreSteps", function(){self.sendUpdateHasStepsToTree(false);});
			eventBus.onContextual("testStepsTable.stepAdded", function(){self.sendUpdateHasStepsToTree(true);});
			eventBus.onContextual('tc-req-links-updated', function(){
				$("#verified-requirements-table").squashTable().refresh();
				try{
					$("#test-steps-table").squashTable().refresh();
				}catch(notloadedyet){
					//no problems
				}
			});

		},

		events : {},

		_initTestAutomation : function(){

			if (this.settings.isAutomated){
				
				var conf = {
					canModify			: this.settings.writable,
					testAutomationUrl	: this.settings.urls.automationUrl + '/tests'
				};
				
				testcaseTestAutomation.init(conf);
				
			}
			/*
			if (this.settings.isAutomated){
				new TestAutomationPicker({
					selector : "#ta-picker-popup",
					testAutomationURL : this.settings.urls.automationUrl + '/tests',
					successCallback : function(newName){
						$("#ta-picker-link").text(newName);
						$("#remove-ta-link").show();
					}
				});

				new TestAutomationRemover({
					automatedTestRemovalUrl : this.settings.urls.automationUrl,
					successCallback : function(){
						$("#ta-picker-link").text(translator.get('label.dot.pick'));
						$("#remove-ta-link").hide();
					},
					confirmPopupSelector : "#test-automation-removal-confirm-dialog"
				});

				$("#ta-picker-link").on('click', function(){
					$("#ta-picker-popup").formDialog('open');
				});

				$("#remove-ta-link").on('click', function(){
					$("#test-automation-removal-confirm-dialog").confirmDialog('open');
					return false;
				});
			}
			*/
			
		},

		_sendUpdateReqToTree : function(){
			eventBus.trigger("node.update-reqCoverage", {targetIds : [this.settings.testCaseId]});
		},

		_sendUpdateHasStepsToTree : function(hasSteps){
			eventBus.trigger("node.attribute-changed", {identity : { resid : this.settings.testCaseId, restype : "test-cases"  }, attribute : 'hassteps', value : ""+hasSteps});
		}
	});
	return InfoTab;
});