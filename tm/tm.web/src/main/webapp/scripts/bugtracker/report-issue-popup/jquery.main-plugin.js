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

define(["jquery", "./default-field-view", "./advanced-field-view", "jqueryui"], function($, DefaultFieldView, AdvancedFieldView){


	/*
	  report-issue-dialog is the javascript object handling the behaviour of the popup that will post 
	  a new issue or attach an existing issue to the current entity.
	*/

	/*
		the settings object must provide :
		- bugTrackerId : the id of the concerned bugtracker
		- labels : an object such as
			- emptyAssigneeLabel : label that should be displayed when the assignable user list is empty
			- emptyVersionLabel : same for version list
			- emptyCategoryLabel : same for category list
			- emptyPriorityLabel : same for priority
		
		- reportUrl : the url where to GET empty/POST filled bug reports
		- findUrl : the url where to GET remote issues
		
		- callback : any callback function. Can accept one argument : the json status of the operation.

	*/
	
	function init(settings){

		var self = this;
		
		//issue model
		this.model = new Backbone.Model();	
		this.mdlTemplate=null;				
	
		//urls
		this.reportUrl = settings.reportUrl;
		this.searchUrl = settings.searchUrl;
		this.bugTrackerId = settings.bugTrackerId;
		
		//main panels of the popup
		this.pleaseWait = $(".pleasewait", this);
		this.content = $(".content", this);

		//the radio buttons
		this.attachRadio = $(".attach-radio", this);
		this.reportRadio = $(".report-radio", this);
				
		//the issue id (if any)
		this.idText = $(".id-text", this);
		
		//the submit button
		this.postButton = $('.post-button', this.next());
		
		//search issue buttons. We also turn it into a jQuery button on the fly.
		this.searchButton = $('.attach-issue input[type="button"]', this).button();
		
		
		//the error display
		this.error = $(".issue-report-error", this);
		this.error.popupError();
		
		
		//a callback when the post is a success
		this.callback=settings.callback;
			
			
		//bind the spans standing for label for the radio buttons
		//(actual labels would have been preferable except for the default style)
		this.find(".issue-radio-label").click(function(){
			$(this).prev("input[type='radio']").click();
		});
		
		
		//and last but not least, the subview that manages the fields
		this.fieldsView = null;
	
	}

	$.fn.btIssueDialog = function(settings){

		var self = this;
		
		init.call(this, settings);
			
		/* ************** some events ****************** */
		
		this.attachRadio.click(function(){
			toAttachMode();
		});
			
		this.reportRadio.click(function(){
			toReportMode();
		});	
	
		this.searchButton.click(function(){
			searchIssue();
		});
		
		this.idText.keypress(function(evt){
			if (evt.which == '13'){
				searchIssue();
				return false;
			}
		});
				
		/* ************* public popup state methods **************** */

		var enableControls = $.proxy(function(){
			if (this.fieldsView !== null){
				this.fieldsView.enableControls();
			}
		}, self);
		
		var disableControls = $.proxy(function(){
			if (this.fieldsView !== null){
				this.fieldsView.disableControls();
			}
		}, self);
		
		
		var toAttachMode = $.proxy(function(){
			flipToMain();
			enableIdSearch();
			disableControls();
			disablePost();
		}, self);
		
		var toReportMode = $.proxy(function(){
			flipToMain();
			disableIdSearch();
			resetModel();
			enableControls();
			enablePost();
		}, self);
		

		var flipToPleaseWait = $.proxy(function(){
			this.pleaseWait.show();
			this.content.hide();
		}, self);
		
		
		var flipToMain = $.proxy(function(){
			this.content.show();
			this.pleaseWait.hide();	
		}, self);
	
		var enablePost = $.proxy(function(){
			this.postButton.button('option', 'disabled', false);
		}, self);
		
		
		var disablePost = $.proxy(function(){
			this.postButton.button('option', 'disabled', true);
		}, self);

		
		var enableSearch = $.proxy(function(){
			this.searchButton.button('option', 'disabled', false);
		}, self);
		
		
		var disableSearch = $.proxy(function(){
			this.searchButton.button('option', 'disabled', true);
		}, self);
		
		
		var enableIdSearch = $.proxy(function(){
			with(this){
				idText.removeAttr('disabled');
				enableSearch();
			}
		}, self);
		
		var disableIdSearch = $.proxy(function(){
			with(this){
				idText.attr('disabled', 'disabled');
				disableSearch();
			}
		}, self);
		

		/* ********************** model and view management ************ */
			
		var isDefaultIssueModel = $.proxy(function(){
			return (this.model.get('project').schemes===undefined)
		}, self);
		
		var setModel = $.proxy(function(newModel){
			
			this.model.set(newModel);			
			createViewForModel();
			
			this.idText.val(this.model.get('id'));			
			this.fieldsView.readIn();
			
		}, self);
		
		
		var createViewForModel = $.proxy(function(){
			if (this.fieldsView==null){
				var view;
				
				if (isDefaultIssueModel()){
					view = new DefaultFieldView({
						el : this.find('.issue-report-fields').get(0),
						model : this.model,
						labels : settings.labels
					});
				}
				else{
					view = new AdvancedFieldView({
						el : this.find('.issue-report-fields').get(0),
						model : this.model,
						labels : settings.labels
					});
				}			
				
				this.fieldsView = view;
			}
		}, self);
		

			
		var resetModel = $.proxy(function(){
			getIssueModelTemplate()
			.done(function(){
				var copy = $.extend(true, {}, self.mdlTemplate);
				setModel(copy);	
			})
			.fail(bugReportError);
		}, self);
		
		
		
		var getIssueModelTemplate = $.proxy(function(){
			
			var jobDone = $.Deferred();				
			
			if (! this.mdlTemplate){

				flipToPleaseWait();		
				
				$.ajax({
					url : self.reportUrl,
					type : "GET",
					dataType : "json"			
				})
				.done(function(response){
					self.mdlTemplate = response;
					flipToMain();
					jobDone.resolve();
				})
				.fail(jobDone.reject)
				.then(flipToMain);
			
			}
			else{
				jobDone.resolve();
			}
			
			return jobDone.promise();
		}, self);

		
		
		// we let the usual error handling do its job here
		// if the error is a field validation error, let's display in the error display
		// we're doing it manually because in some context (ieo for instance) the general 
		// error handler is not present.
		var bugReportError = $.proxy(function(jqXHR, textStatus, errorThrown){
			try{
				var message = $.parseJSON(jqXHR.responseText).fieldValidationErrors[0].errorMessage;
				this.error.find('.error-message').text(message);
			}catch(ex){
				// well maybe that wasn't for us after all
			}
			flipToMain();
			this.error.popupError('show');
		}, self);
		
		
		var searchIssue = $.proxy(function(){
			var id = this.idText.val() ||"(none)";
			
			flipToPleaseWait();
			
			$.ajax({
				url : self.searchUrl+id,
				type : 'GET',
				dataType : 'json',
				data : {"bugTrackerId" : self.bugTrackerId}
			})
			.done(function(response){
				setModel(response);
				enablePost();
			})
			.fail(bugReportError)
			.then(flipToMain);
			
		}, self);
		
		
		/* ************* public ************************ */

		this.submitIssue = $.proxy(function(){
			
			flipToPleaseWait();
			
			this.fieldsView.readOut();
			var strModel = JSON.stringify(this.model.toJSON());
			
			$.ajax({
				url : self.reportUrl,
				type : 'POST',
				data : strModel,
				contentType: 'application/json',
				dataType : 'json'
			})
			.done(function(){
				self.dialog('close');
				if (self.callback){
					self.callback.apply(self, arguments);
				}
			})
			.fail(bugReportError);
		}, self);
		
		/* ************* events ************************ */
		
		//the opening of the popup :
		this.bind("dialogopen", function(){
			self.postButton.focus();
			self.reportRadio.click();
		});
		
		//the action bound to click on the first button
		this.dialog('option').buttons[0].click=this.submitIssue;

		return this;

	};	
	
	//though loaded as a module, it doesn't produce anything. It's a jQuery plugin after all.
	return null;
});