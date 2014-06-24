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
define([ "jquery", "backbone", "app/ws/squashtm.notification", "underscore", "app/lnf/Forms", "jquery.squash.formdialog",
		"squashtest/jquery.squash.popuperror" ], function($, Backbone, WTF, _, Forms) {

	// *************************************** BindPopup **********************************************

	var BindPopup = Backbone.View.extend({

		el : "#ta-projects-bind-popup",

		initialize : function(conf) {
			var self = this;
			// properties
			this.selectedServerId = conf.TAServerId;
			this.updateProjectList = true;
			this.projecUrl = conf.tmProjectURL;
			// initialize
			this.$el.formDialog();
			this.error = this.$(".ta-projectsadd-error").popupError();
			// methods bound to this
			this.manageFatalError = $.proxy(this._manageFatalError, this);
			this.onChangeServerConfirmed = $.proxy(this._onChangeServerConfirmed, this);
			this.buildAndDisplayProjectList = $.proxy(this._buildAndDisplayProjectList, this);
			this.showErrorMessage = $.proxy(this._showErrorMessage, this);
			this.manageBindingError = $.proxy(this._manageBindingError, this);
		},

		events : {
			"formdialogconfirm" : "confirm",
			"formdialogcancel" : "cancel",
			"formdialogopen" : "open"
		},
		open : function() {
			var self = this;
			if (this.updateProjectList) {
				this.$el.formDialog('setState', 'pleasewait');
				$.ajax({
					url : self.projecUrl + "/available-ta-projects",
					type : "get"

				}).done(self.buildAndDisplayProjectList).fail(self.manageFatalError);
				this.updateProjectList = false;
			}
		},
		confirm : function() {
			var self = this;
			// find checked
			var checked = this.$el.find(".ta-project-bind-listdiv input:checkbox:checked");
			if (checked.length === 0) {
				var message = squashtm.app.messages["message.project.bindJob.noneChecked"];
				this.showErrorMessage(message);
				return;
			}
			// map checkbox values to tm label
			var tmLabels = [];
			var hasDuplicateTmLabel = false;
			var datas = _.map(checked, function(item) {
				var $item = $(item);
				var row = $item.parents("tr")[0];
				var input = $(row).find("td.ta-project-tm-label input")[0];
				var tmLabel = $(input).val();
				// checks for duplicate tm labels
				if (!hasDuplicateTmLabel) {
					if (_.contains(tmLabels, tmLabel)) {
						hasDuplicateTmLabel = true;
					} else {
						tmLabels.push(tmLabel);
					}
				}
				return {
					jobName : $item.val(),
					label : tmLabel
				};
			});
			if (hasDuplicateTmLabel) {
				// show error message
				var duplicateMessage = squashtm.app.messages["message.project.bindJob.duplicatelabels"];
				this.showErrorMessage(duplicateMessage);
			} else {
				// send ajax
				$.ajax({
					url : self.projecUrl + "/test-automation-projects/new",
					type : "post",
					contentType : 'application/json',
					dataType : 'json',
					global : false,
					data : JSON.stringify(datas)
				}).done(function() {
					self.trigger("bindTAProjectPopup.confirm.success");
					self.updateProjectList = true;
					self.$el.formDialog('close');
				}).fail(function(xhr) {
					self.trigger("bindTAProjectPopup.confirm.failure");
					self.manageBindingError(xhr);

				});

			}

		},
		cancel : function() {
			this.trigger("bindTAProjectPopup.cancel");
			this.$el.formDialog('close');
		},
		show : function() {
			this.$el.formDialog("open");
		},

		buildProjectList : function(projectList) {
			var tablePanel = this.$el.find(".ta-project-bind-listdiv");
			tablePanel.empty();

			var i = 0;
			var rows = $();

			for (i = 0; i < projectList.length; i++) {
				var row = this.newRow(projectList[i]);
				rows = rows.add(row);
			}

			rows.filter("tr:odd").addClass("odd");
			rows.filter("tr:even").addClass("even");

			tablePanel.append(rows);
		},
		bindProjectListEvents : function() {
			var self = this;
			this.$el.find(".ta-project-bind-listdiv input:checkbox").change(function(event) {
				var $checkbox = $(event.target);
				var row = $checkbox.parents("tr")[0];
				var tmLabelCell = $(row).find("td.ta-project-tm-label")[0];
				self.tmLabelEditable($(tmLabelCell), $checkbox.is(":checked"));
			});
		},
		newRow : function(jsonItem) {
			var source = $("#default-item-tpl").html();
			var template = Handlebars.compile(source);
			var row = template(jsonItem);
			return row;
		},

		tmLabelEditable : function($tmLabelCell, editable) {
			if (editable) {
				$tmLabelCell.find("input").show();
				$tmLabelCell.addClass("edit-state");
			} else {
				$tmLabelCell.find("input").hide();
				$tmLabelCell.removeClass("edit-state");
			}
		},

		setParentPanel : function(parentPanel) {
			var self = this;
			this.parentPanel = parentPanel;
			// event listening
			this.listenTo(self.parentPanel.popups.confirmChangePopup, "confirmChangeServerPopup.confirm.success",
					self.onChangeServerConfirmed);
			// refresh popup on delete project
			this.listenTo(self.parentPanel.popups.unbindPopup, "unbindTAProjectPopup.confirm.success", function() {
				self.updateProjectList = true;
			});
			// refresh popup on edit project
			this.listenTo(self.parentPanel.popups.editTAProjectPopup, "edittestautomationproject.confirm.success", function() {
				self.updateProjectList = true;
			});
			
		},
		_onChangeServerConfirmed : function(newSelectedServer) {
			if (newSelectedServer == this.selectedServerId) {
				return;
			} else {
				this.selectedServerId = newSelectedServer;
				this.updateProjectList = true;
			}
		},
		_manageBindingError :function(request) {
			var json =  $.parseJSON(request.responseText);
			//handle specifically CompositeDomainException containing DuplicateTMLabelException
			var manageFatal = false;
			 if (!!json.fieldValidationErrors) {
				 /* IE8 requires low tech code */
				 var validationErrorList = json.fieldValidationErrors;
				 if (validationErrorList.length > 0) {
					 for ( var counter = 0; counter < validationErrorList.length; counter++) {
						 var fve = validationErrorList[counter];
						 if (fve.fieldName == "label"){
							 var inputs = this.$el.find('.edit-state input').filter(function() { return this.value == fve.fieldValue; });
							 if(inputs.length === 0){manageFatal = true;}
							 Forms.input($(inputs[0])).setState("error", fve.errorMessage);
						 }else{
							 manageFatal = true;
						 }
					}
				 }
			 }else{
				 manageFatal = true;
			 }
			 if(manageFatal){
				 this.manageFatalError(response);
			 }
		},
		_manageFatalError : function(json) {
			var message = "";
			try {
				message = WTF.getErrorMessage(json);
			} catch (parseException) {
				message = json.responseText;
			}
			this.showErrorMessage(message);

		},
		_showErrorMessage : function(message) {
			this.error.find('span').html(message);
			this.error.popupError('show');
		},
		_buildAndDisplayProjectList : function(json) {
			if (json.length > 0) {
				this.buildProjectList(json);
				this.bindProjectListEvents();
				this.$el.formDialog('setState', 'main');
			} else {
				this.$el.formDialog('setState', 'noTAProjectAvailable');
			}
		}
	});
	return BindPopup;
});
