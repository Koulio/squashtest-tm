/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
define([ "jquery", "backbone", "app/lnf/Forms", 'workspace.event-bus', 
         'squash.configmanager', "./NewParameterModel", "jquery.squash.confirmdialog" ], 
         function($, Backbone, Forms, eventBus, confman, NewParameterModel) {
	
	var NewParameterDialog = Backbone.View.extend({
		el : "#add-parameter-dialog",

		initialize : function() {
			this.settings = this.options.settings;
			
			this.$checkboxes = this.$el.find("input:checkbox");
			this.$textAreas = this.$el.find("textarea");
			this.$textFields = this.$el.find("input:text");
			this.$errorMessages = this.$el.find("span.error-message");
			this.model = new NewParameterModel();
			this._resetForm();
		},

		events : {
			"confirmdialogcancel" : "cancel",
			"confirmdialogvalidate" : "validate",
			"confirmdialogconfirm" : "confirm"
		},

		cancel : function(event) {
			this.cleanup();
			this.trigger("newparameterdialog.cancel");
		},

		confirm : function(event) {
			this.cleanup();
			this.trigger("newparameterdialog.confirm");
		},

		validate : function(event) {
			var res = true, self = this;
			this._populateModel();
			var validationErrors = this.model.validateAll();
			
			Forms.form(this.$el).clearState();

			if (validationErrors !== null) {
				for ( var key in validationErrors) {
					Forms.input(this.$("input[name='add-parameter-" + key + "']")).setState("error",
							validationErrors[key]);
				}

				return false;
			}

			$.ajax({
				type : 'post',
				url : self.settings.basic.testCaseUrl + "/parameters/new",
				dataType : 'json',
				// note : we cannot use promise api with async param. see
				// http://bugs.jquery.com/ticket/11013#comment:40
				async : false,
				data : self.model.attributes,
				error : function(jqXHR, textStatus, errorThrown) {
					res = false;
					event.preventDefault();
				}
			});

			return res;
		},

		cleanup : function() {
			this.$el.addClass("not-displayed");
			this._resetForm();
			this.$el.confirmDialog("close");
		},

		_resetForm : function() {
			this.$textFields.val("");
			this.$textAreas.val("");
			this.$errorMessages.text("");
			Forms.form(this.$el).clearState();
		},

		show : function() {
			if (!this.dialogInitialized) {
				this._initializeDialog();
			}

			this.$el.confirmDialog("open");
		},

		_initializeDialog : function() {
			this.$el.confirmDialog();

			function decorateArea() {
				var $area = $(this);
				$area.ckeditor(function() {}, confman.getStdCkeditor());
			}

			this.$textAreas.each(decorateArea);

			this._initializeCkeditorTermination();

			this.dialogInitialized = true;
		},

		_populateModel : function() {
			var model = this.model, $el = this.$el;
			var name = $el.find("#add-parameter-name").val();
			var description =  $el.find("#add-parameter-description").val();
			model.attributes.name = name ;
			model.attributes.description = description;
		},

		_initializeCkeditorTermination : function() {
			var self = this;
			
			eventBus.onContextual('contextualcontent.clear', function(event) {
				self.$textAreas.ckeditorGet().destroy();
			});
		}
	});
	return NewParameterDialog;
});