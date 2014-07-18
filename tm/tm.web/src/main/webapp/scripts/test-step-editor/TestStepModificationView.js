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
define([ "jquery", "backbone", "./TestStepInfoModel", 
         "../verified-requirements/TestStepVerifiedRequirementsPanel",
		"app/lnf/Forms", "custom-field-values", "squash.configmanager",
		"app/ws/squashtm.notification",
		"jquery.squash", "jqueryui", "jquery.ckeditor", "jeditable",
		"ckeditor", "jeditable.ckeditor", "jquery.squash.jeditable", "jquery.squash.squashbutton",
		"datepicker/jquery.squash.datepicker-locales" ], function($, Backbone, TestStepInfoModel,
		VerifiedRequirementsPanel, Forms, cufValues, confman) {
	
	var editTCS = squashtm.app.editTCS;
	/*
	 * Defines the controller for the custom fields table.
	 */
	var TestStepModificationView = Backbone.View.extend({
		el : "#information-content",
		initialize : function() {
			if (squashtm.app.verifiedRequirementsBlocSettings) {
				this.verifiedRequirementPanel = new VerifiedRequirementsPanel();
			}
			this.configureCKEs();
			this.configureButtons();
			this.configureCUFs();

			this.initializeModel();

		},
		initializeModel : function() {
			var modelAttributes = {};
			this.fillModelAttributes(modelAttributes);
			this.model = new TestStepInfoModel(modelAttributes);
		},
		fillModelAttributes : function(modelAttributes) {
			var self = this;
			var fields = this.$(".test-step-attr");
			$.each(fields, function(index, value) {
				modelAttributes[$(value).attr("name")] = $(value).val();
			});

			var cufValuesValues = this.$(".custom-field");
			if (cufValuesValues.length > 0) {
				modelAttributes.cufValues = {};
				cufValuesValues.each(function(index, elt) {
					var $elt = $(elt);
					modelAttributes.cufValues[$elt.data("value-id")] = self.getInputValue($elt);
				});
			}
		},
		configureButtons : function() {
			this.$("#next-test-step-button").squashButton({
				disabled : editTCS.nextId == -1,
				text : false,
				icons : {
					primary : "ui-icon-triangle-1-e"
				}
			});

			this.$("#previous-test-step-button").squashButton({
				disabled : editTCS.previousId == -1,
				text : false,
				icons : {
					primary : "ui-icon-triangle-1-w"
				}
			});

			$.squash.decorateButtons();

			// ===============toogle buttons=================
			// this line below is here because toggle panel
			// buttons cannot be bound with the 'events'
			// property of Backbone.View.²²
			// my guess is that the event is bound to the button
			// before it is moved from it's "span.not-displayed"
			// to the toggle panel header.
			// TODO change our way to make toggle panels buttons
			// =============/toogle buttons===================
			this.$("#save-test-step-button").on('click', $.proxy(this.saveStep, this));

		},

		
		configureCUFs : function() {
			var self = this,
				cufDefinitions = editTCS.cufDefinitions,
				editable = editTCS.writable;
			
			if (cufDefinitions.length>0){
				var mode = (editable) ? "editable" : "static";
				cufValues.infoSupport.init("#test-step-infos-table", cufDefinitions, mode);
			}
			
			// also, like for configureCKEs below, the onChange event 
			// must be treated unconventionnaly. We need this for RICH_TEXT cufs.
			var areas = $(".custom-field textarea").each(function(){
				var area = this;
				var id = this.id;
				CKEDITOR.instances[id].on('change', function(){
					self.updateModelCufAttr({currentTarget : $(area).parent().eq(0)});
				});
			});
		},

		configureCKEs : function() {
			var self = this;
			var textareas = this.$el.find("textarea"),
				ckconf = confman.getStdCkeditor();
			
			textareas.each(function() {
				$(this).ckeditor(function() {
				}, ckconf);

				CKEDITOR.instances[$(this).attr("id")].on('change', function(e) {
					self.updateCKEModelAttr.call(self, e);
				});
			});
		},
		
		events : {
			"click #previous-test-step-button" : "goPrevious",
			"click #next-test-step-button" : "goNext",
			// "change .test-step-attr" : "updateCKEModelAttr",
			// did not work because of _CKE instances (cf method
			// configureCKEs to see how manual binding is done.
			"change .custom-field" : "updateModelCufAttr"

		},

		goPrevious : function(event) {
			document.location.href = squashtm.app.contextRoot + "test-steps/" + editTCS.previousId;
		},
		goNext : function(event) {
			document.location.href = squashtm.app.contextRoot + "test-steps/" + editTCS.nextId;

		},
		updateCKEModelAttr : function(event) {
			var attrInput = event.sender;
			var attrName = attrInput.element.$.getAttribute("name");
			var attrValue = attrInput.getData();
			this.model.set(attrName, attrValue);
		},
		updateModelCufAttr : function(event) {
			var input = event.currentTarget;
			var $input = $(input);
			var name = $input.data("value-id");
			var value = this.getInputValue($input);
			var cufValues = this.model.get("cufValues");
			cufValues[name] = value;
			this.model.set({
				'cufValues' : cufValues
			});
		},
		getInputValue : function(input) {
			var editable = editTCS.writable;
			if (editable){
				return input.editableCustomfield("value");
			}
			else{
				return input.staticCustomfield("value");
			}
		},
		saveStep : function(event) {
			this.clean();
			this.model.save(null, {
			
				// TODO : unfortunately the error handling must be hand made
				error : function(model, xhr, options){
					try{
						var errmsg = JSON.parse(xhr.responseText);
						for (var i=0, errors = errmsg.fieldValidationErrors, len = errors.length; i<len; i++){
							var err = errors[i];
							var id = err.objectName+'-'+err.fieldName,
								msg = err.errorMessage;
							
							var errdiv = $('#'+id);
							Forms.input(errdiv).setState("error", msg);
						}
						
						xhr.errorIsHandled = true;
					}
					catch(ex){
						// let the exception go and the generic handler kick in
					}
				}
			});
		},
		clean : function() {
			Forms.form(this.$el).clearState();
		}

	});
	return TestStepModificationView;
});