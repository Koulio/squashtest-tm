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
define([ "jquery", "backbone", "handlebars", "app/lnf/Forms",
		"jquery.squash.formdialog" ], function($, Backbone, Handlebars,  Forms) {
	"use strict";
	/**
	 * Saves the model
	 * @param model
	 * @param event (optional) if invoked in repsonse to an event, please provide that event.
	 * @returns {Boolean} true if model was saved without errors
	 */
	function saveModel(model, event) {
		var res = true;

		$.ajax({
			type : 'post',
			url : squashtm.app.contextRoot + "/generic-projects/new",
			dataType : 'json',
			// note : we cannot use promise api with async param. see
			// http://bugs.jquery.com/ticket/11013#comment:40
			async : false,
			data : model,
			error : function(jqXHR, textStatus, errorThrown) {
				res = false;
				if (!!event) {
					event.preventDefault();
				}
			}
		});

		return res;
	}

	var View = Backbone.View.extend({
		el : "#add-project-dialog",

		initialize : function() {
			this.$checkboxes = this.$el.find("input:checkbox");
			this.$textAreas = this.$el.find("textarea");
			this.$textFields = this.$el.find("input:text");
			this.$errorMessages = this.$el.find("span.error-message");

			this._resetForm();
		},

		events : {
			"formdialogaddanother" : "addAnother",
			"formdialogconfirm" : "addAndClose",
			"formdialogcancel" : "cancel",
		},

		cancel : function(event) {
			this.cleanup();
			this.trigger("newproject.cancel", { source: event, view: this });
		},

		addProject: function(event, postStep) {
			this._populateModel();
			Forms.form(this.$el).clearState();

			if (saveModel(this.model, event)) {
				this.trigger("newproject.added", { source: event, view: this, model: this.model });
				postStep.call(this, event);
			}
		},

		addAnother : function(event) {
			this.addProject(event, function() {
				this.$el.addClass("not-displayed");
				this._resetForm();
			});
		},

		addAndClose: function(event) {
			this.addProject(event, this.cleanup);
		},

		cleanup : function() {
			this.$el.addClass("not-displayed");
			this._resetForm();
			this.$el.formDialog("close");
		},

		_resetForm : function() {
			this.$checkboxes.prop("checked", false);
			this.$textFields.val("");
			this.$textAreas.val("");
			this.$errorMessages.text("");
			Forms.form(this.$el).clearState();
		},

		show : function() {
			if (!this.dialogInitialized) {
				this._initializeDialog();
			}

			this.$el.formDialog("open");
		},

		_initializeDialog : function() {
			this.$el.formDialog();

			function decorateArea() {
				$(this).ckeditor(function() {
				}, {
					customConfig : squashtm.app.contextRoot + "/styles/ckeditor/ckeditor-config.js",
					language : squashtm.app.ckeditorLanguage
				});
			}

			this.$textAreas.each(decorateArea);

			this.dialogInitialized = true;
		},

		_populateModel : function() {
			var model = this.model, $el = this.$el;

			model.name = $el.find("#add-project-name").val();
			model.description = $el.find("#add-project-description").val();
			model.label = $el.find("#add-project-label").val();
			model.isTemplate = $el.find("input:checkbox[name='isTemplate']").prop("checked");
		}
	});

	return View;
});