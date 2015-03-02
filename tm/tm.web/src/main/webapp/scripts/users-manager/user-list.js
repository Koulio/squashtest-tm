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
/**
 * settings : { data : { tableData : the json model of the data displayed by the
 * datatable },
 *
 * urls : { rootContext : the base url of the application backUrl : the url
 * where to go where to go when clicking the #back button baseUrl : the base url
 * of all regaring user adminitration (listing, adding, removing etc) },
 * language : { missingNewPassword : error message when the new password input
 * wasn't typed in missingConfirmPassword : same, for the confirmation input
 * differentConfirmation : error message when the new password and confirmation
 * button ok : label for ok cancel : label
 * for cancel } }
 */

define([ "jquery", "squash.translator", 
         "jquery.squash.fragmenttabs", "squashtable", 
         "jquery.squash.confirmdialog", "jquery.squash.formdialog" ],
		function($, translator, Frag) {

	// ---------------- add user dialog ----------------------------


	// note : I don't trust hasOwnProperty due to its cross-browser issues.
	// We'll
	// do it low tech once again.
	function isFilled(selector) {
		var value = $(selector).val();
		if (!value.length) {
			return false;
		} else {
			return true;
		}

	}

	function buildPasswordValidation(settings) {
		if (settings.managedPassword === true) {
			// password is managed by provider, we don't perform any password check
			return function() { return true; };
		}

		var language = settings.language;
		return function() {
			var lang = language;

			// first, clear error messages
			$("#add-user-table span.error-message").html('');

			var newPassOkay = true;
			var confirmPassOkay = true;
			var samePassesOkay = true;

			if (!isFilled("#add-user-password")) {
				$("span.error-message.password-error").html(
						lang.missingNewPassword);
				newPassOkay = false;
			}

			if (!isFilled("#new-user-confirmpass")) {
				$("span.error-message.confirmpass-error").html(
						lang.missingConfirmPassword);
				confirmPassOkay = false;
			}

			if ((newPassOkay) && (confirmPassOkay)) {
				var pass = $("#add-user-password").val();
				var confirm = $("#new-user-confirmpass").val();

				if (pass != confirm) {
					$("span.error-message.password-error").html(
							lang.differentConfirmation);
					samePassesOkay = false;
				}
			}

			return ((newPassOkay) && (confirmPassOkay) && (samePassesOkay));

		};
	}

	function readForm(settings) {
		var form = {
			login : $("#add-user-login").val(),
			firstName : $("#add-user-firstName").val(),
			lastName : $("#add-user-lastName").val(),
			email : $("#add-user-email").val(),
			groupId : $("#add-user-group").val()
		};

		if (settings.managedPassword) {
			form.noPassword = true;
		} else {
			form.password = $("#add-user-password").val();
		}

		return form;
	}

	// ------------- dialog init -----------------

	function initDialog(settings) {

		// new user popup

		var passValidation = buildPasswordValidation(settings);
		
		var adduserDialog = $("#add-user-dialog");
		adduserDialog.formDialog({width : 600});		
		
		adduserDialog.on('formdialogconfirm', function(){
			if (!passValidation()){
				return;
			}
			var url = settings.urls.baseUrl + "/new";
			$.ajax({
				url : url,
				type : 'POST',
				dataType : 'json',
				data : readForm(settings)
			}).success(function(){
				$('#users-list-table').squashTable().refresh();
				adduserDialog.formDialog('close');
			});
		});
		
		adduserDialog.on('formdialogcancel', function(){
			adduserDialog.formDialog('close');
		});
		
		adduserDialog.on('formdialogopen', function(){
			$("#add-user-group").val($("#add-user-group option:last").val());
		});
		
		adduserDialog.on('formdialogaddanother', function(){
			if (!passValidation()){
				return;
			}
			var url = settings.urls.baseUrl + "/new";
			$.ajax({
				url : url,
				type : 'POST',
				dataType : 'json',
				data : readForm(settings)
			}).success(function(){
				$('#users-list-table').squashTable().refresh();
				$("#add-user-dialog").formDialog('open');
			});
		});
		
		
		$("#add-user-button").on('click', function(){
			adduserDialog.formDialog('open');
		});
		
		// confirm deletion
		$("#delete-user-dialog").confirmDialog().on('confirmdialogconfirm', function(){
			var $this = $(this),
			table = $("#users-list-table").squashTable();

			var userId = $this.data('entity-id'),
				userIds = (!! userId) ? [ userId ] : table.getSelectedIds();

			$this.data('entity-id');	//reset
			var url = squashtm.app.contextRoot+'/administration/users/'+userIds.join(',');
			$.ajax({
				url : url,
				type : 'delete'
			})
			.done(function(){
				table.refresh();
			});
		});

	}


	// ---------------------- button ----------------------

	function initButtons(settings) {
		
		
		/* There's something that remove and replace an element of the css and destroy the apparence of the buttons */
		$("#add-user-button").button();
		$("#add-user-button").removeClass("ui-button-text-only").addClass("ui-button-text-icon-primary");
		$("#add-user-button > span").removeClass("ui-button-text");
		$("#delete-user-button").button();
		$("#delete-user-button").removeClass("ui-button-text-only").addClass("ui-button-text-icon-primary");
		$("#delete-user-button > span").removeClass("ui-button-text");

		function displayNothingSelected(){
			var warn = translator.get({
				errorTitle : 'popup.title.Info',
				errorMessage : 'message.EmptyTableSelection'
			});
			$.squash.openMessage(warn.errorTitle, warn.errorMessage);
		}


		$("#deactivate-user-button").button().on('click', function(){
			var table =  $("#users-list-table").squashTable();
			var ids = table.getSelectedIds();
			if (ids.length>0){
				table.deactivateUsers(ids);
			}
			else{
				displayNothingSelected();
			}
		});

		$("#activate-user-button").button().on('click', function(){
			var table =  $("#users-list-table").squashTable();
			var ids = table.getSelectedIds();
			if (ids.length>0){
				table.activateUsers(ids);
			}
			else{
				displayNothingSelected();
			}
		});

		$("#delete-user-button").button().on('click', function(){
			var ids = $("#users-list-table").squashTable().getSelectedIds();
			if (ids.length>0){
				var popup = $("#delete-user-dialog");
				popup.data('entity-id',null);
				popup.confirmDialog('open');
			}
			else{
				displayNothingSelected();
			}
		});

		$("#back").click(function() {
			document.location.href = settings.urls.backUrl;
		});
	}

	// ----------- table ---------------------

	function drawCallback(){
		var table = this;

		// activation button

		useractiveCells = table.find('tbody .user-active-cell');

		useractiveCells.each(function(){

			var value = table.fnGetData(this),
				$cell = $(this);

			var btnclass = (value) ? 'table-icon user-active-btn icon-user-activated' : 'table-icon user-active-btn icon-user-deactivated';

			$cell.empty().append('<a href="#" class="'+btnclass+'"/>');

		});

	}

	function initTable(settings) {

		var datatableSettings = {
			"aaData" : settings.data.tableData,
			"fnDrawCallback" : drawCallback
		};

		var squashSettings = {

				deleteButtons : {
					delegate : "#delete-user-dialog",
					tooltip : translator.get('label.Remove')
				},
		
			functions : {


				activateUsers : function(ids){
					var table = this;
					var url = squashtm.app.contextRoot+"/administration/users/"+ids.join(',')+'/activate';
					$.post(url).done(function(){
						table._changeActivation(ids, true, "table-icon user-active-btn icon-user-activated");
					});
				},

				deactivateUsers : function(ids){
					var table = this;
					var url = squashtm.app.contextRoot+"/administration/users/"+ids.join(',')+'/deactivate';
					$.post(url).done(function(){
						table._changeActivation(ids, false, "table-icon user-active-btn icon-user-deactivated");
					});
				},

				_changeActivation : function(ids, value, cssclass){
					var _table = this;
					var rows = _table.getRowsByIds(ids);
					rows.each(function(){
						var data = _table.fnGetData(this);
						data['user-active'] = value;
					});
					rows.find('a.user-active-btn').attr('class', '').addClass(cssclass);
				}
			}
		};

		var table = $("#users-list-table").squashTable(datatableSettings, squashSettings);

		// various hooks

		table.on('click', 'a.user-active-btn', function(evt){

			var tr = this.parentNode.parentNode,
				data = table.fnGetData(tr),
				id = data['user-id'],
				active = data['user-active'];

			if (active){
				table.deactivateUsers([id]);
			}else{
				table.activateUsers([id]);
			}


		});

	}

	function initTabs(){
		Frag.init();
	}


	function init(settings) {
		initTabs();
		initButtons(settings);
		initTable(settings);
		initDialog(settings);
	}

	return {
		initUserListPage : init
	};

});