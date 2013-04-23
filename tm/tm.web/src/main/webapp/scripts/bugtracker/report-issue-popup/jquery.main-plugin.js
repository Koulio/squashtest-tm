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

define(["jquery", "./default-field-view", "./advanced-field-view", "file-upload", "jqueryui"], function($, DefaultFieldView, AdvancedFieldView, fileUploadUtils){

	

	function SimpleIssuePostHelper(controller){
		
		this.controller = controller
		
		this.postIssue = function(issueModel, url){
			
			var strModel = JSON.stringify(issueModel);		

			return $.ajax({
				url : url,
				type : 'POST',
				data : strModel,
				contentType: 'application/json',
				dataType : 'json'
			});		
		}
		
	}
	
	
	function AdvancedIssuePostHelper(controller){
		
		this.controller = controller;
		
		this.transformUrl = function(url){
			return url.replace(/new-issue/, "new-advanced-issue");
		}
		
		//this remove the properties that Jackson shouldn't bother with - thus preventing crashes
		//everything it needs is in the fieldValues
		this.preprocessIssue = function(issue){
			
			delete issue["priority"];
			delete issue["comment"];
			delete issue["version"];
			delete issue["status"];
			delete issue["description"];
			delete issue["category"];
			delete issue["summary"];
			delete issue["assignee"];
			
			return issue;
		}
		
		
		//an issue is first posted, then its attachments. An error 
		//on posting the attachments should not make the whole thing fail.
		this.postIssue = function(issueModel, url){
			
			var helper = this;
			var effectiveUrl = this.transformUrl(url);
			
			var effectiveModel = this.preprocessIssue(issueModel);			
			var strModel = JSON.stringify(issueModel);		

			var xhr = $.ajax({
				url : effectiveUrl,
				type : 'POST',
				data : strModel,
				contentType: 'application/json',
				dataType : 'json'
			});			
			
			xhr.done(function(json){
				try{
					helper.postAttachments(json);
				}
				catch(wtf){
					if (console && console.log){
						console.log(wtf);
					}
				}
			});
			
			
			return xhr;
		}
		
		
		this.postAttachments = function(json, btName){
			
			var forms = this.controller.fieldsView.getFileUploadForms();
			
			if (forms.length>0){
				
				var btName = this.controller.model.get('bugtracker');
				var url = squashtm.app.contextRoot+'/bugtracker/'+btName+'/remote-issues/'+json.issueId+'/attachments';
				
				for (var i=0;i<forms.length;i++){
					var form = $(forms[i]);
					fileUploadUtils.uploadFilesOnly(form, url);
				}
			}
		}
		
	}

	

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

		******************

		Implementation detail about 'reportUrl' : 
		 - for the regular bugtracker model, this url will be used for both GET and POST
		 - for the advanced bugtracker model, the url where you GET (/new-issue) is slightly 
		 	different from the one where to post (/new-advanced-issue). This discrepancy is 
		 	handled by the code via method getSubmitIssueUrl. 

	*/
	
	function init(settings){

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
		
		
		//and last but not least, the subview that manages the fields, and the post helper
		this.fieldsView = null;
		this.postHelper = null;
	
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
			return ( this.model.get('project').schemes===undefined && this.model.toJSON.fieldValues === undefined );
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
				var postHelper;
				
				if (isDefaultIssueModel()){
					view = new DefaultFieldView({
						el : this.find('.issue-report-fields').get(0),
						model : this.model,
						labels : settings.labels
					});
					
					postHelper = new SimpleIssuePostHelper(self);
				}
				else{
					view = new AdvancedFieldView({
						el : this.find('.issue-report-fields').get(0),
						model : this.model,
						labels : settings.labels
					});
					
					postHelper = new AdvancedIssuePostHelper(self);
				}			
				
				this.fieldsView = view;
				this.postHelper = postHelper;
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
			
			this.fieldsView.readOut();	
			if(!this.model.get("isInvalid")){
	
				flipToPleaseWait();
				
				var model = this.model.toJSON();
				
			var xhr = this.postHelper.postIssue(model, this.reportUrl);
				
			xhr.done(function(){
					self.dialog('close');
					if (self.callback){
						self.callback.apply(self, arguments);
					}
				})
				.fail(bugReportError);
			}
			
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