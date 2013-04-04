/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
define([ "jquery", "backbone", "underscore", "app/util/StringUtil" , "./VerifiedRequirementsTable" ,
		"jquery.squash", "jqueryui", "jquery.squash.togglepanel",
		"jquery.squash.datatables", "jquery.squash.oneshotdialog",
		"jquery.squash.messagedialog", "jquery.squash.confirmdialog" ],
		function($, Backbone, _, StringUtil, VerifiedRequirementsTable) {
			var VRTS = squashtm.app.verifiedRequirementsTableSettings;
				var TestCaseVerifiedRequirementsTable = VerifiedRequirementsTable.extend({
					
					initialize : function(options) {
						this.constructor.__super__.initialize.apply(this, [options]);							
						this.configureNoDirectRequirementSelectedDialog.call(this);
					},
					
					events : {},
					
					squashSettings : function(self){
							
								return {buttons : [{ tooltip :  VRTS.messages.remove,
									cssClass : "",
									condition : function(row, data){ 
										var verified = data["directlyVerified"] == "false" ? false : data["directlyVerified"];
										return verified && VRTS.linkable;
										},
									tdSelector : "td.delete-button",
									uiIcon : "ui-icon-minus",
									onClick : self.removeRowRequirementVersion,
								}]};},
					
					_confirmRemoveRequirements : function(rows){
						var self = this;
						this.toDeleteIds = [];
						var rvIds = $(rows).collect(function(row){return self.table.getODataId(row);});
						var hasRequirement = (rvIds.length > 0);
						if (hasRequirement) {
							var indirects = $(rows).not(function(index,row){
								var data = self.table.fnGetData(row);
								return  data["directlyVerified"] == "false" ? false : data["directlyVerified"];
							});
							if (indirects.length >0){
								this.noDirectRequirementSelectedDialog.messageDialog("open");
							}
							else{ 
								
								this.toDeleteIds = rvIds;
								var obsoleteStatuses = $(rows).not(function(index,row){return self.table.fnGetData(row)["status"]!="OBSOLETE";});							
								if (obsoleteStatuses.length > 0){
									this.confirmRemoveObsoleteRequirementDialog.confirmDialog("open");
								} else {
									this.confirmRemoveRequirementDialog.confirmDialog("open");
								}
							}
						} else {
							this.noRequirementSelectedDialog.messageDialog('open');
						}
										
					},
					
					_requirementsTableRowCallback: function (row, data, displayIndex) {
						var verified = data["directlyVerified"] == "false" ? false : data["directlyVerified"];
						if(VRTS.linkable && verified  && data["status"] !="OBSOLETE"){
							this.addSelectEditableToVersionNumber(row, data);
						}
						this.discriminateDirectVerifications(row, data, displayIndex);
						this.addLinkToTestStep(row, data, displayIndex);
						return row;
					},

					
					configureNoDirectRequirementSelectedDialog : function() {
						this.noDirectRequirementSelectedDialog = $("#no-selected-direct-requirement-dialog").messageDialog();
					},
					
					//=====================================================
					
					
					discriminateDirectVerifications : function (row, data, displayIndex){
						var verified = data["directlyVerified"] == "false" ? false : data["directlyVerified"];
							if (!verified){
								$(row).addClass("requirement-indirect-verification");
								$('td.delete-button', row).html(''); //remove the delete button
							}else{
								$(row).addClass("requirement-direct-verification");
							}
							
					},
					
					addLinkToTestStep: function (row, data, displayIndex){
						var spans = $("span.verifyingStep", row);
						var span = $(spans[0]);
						if(span){
							var stepIndex = span.text();
							var stepId = span.attr("dataId");
							var link = $("<a/>", { 'href' : squashtm.app.contextRoot+"/test-steps/"+stepId});
							link.text(stepIndex);
							var cell = span.parent("td");
							cell.html(link);
						}
					}
					
					

					
				});
				return TestCaseVerifiedRequirementsTable;
			});