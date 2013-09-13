/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
define([ "jquery", "backbone", "underscore", "app/util/StringUtil", "jquery.squash", "jqueryui",
		"jquery.squash.togglepanel", "jquery.squash.datatables", "jquery.squash.oneshotdialog",
		"jquery.squash.messagedialog", "jquery.squash.confirmdialog" ], function($, Backbone, _, StringUtil) {
	var UMod = squashtm.app.UMod;
	var UserPermissionsPanel = Backbone.View.extend({
		el : "#permissions",
		initialize : function() {
			this.makeTogglePanel();
			this.configureTable();
			this.configurePopups();
			this.configureButtons();
		},
		events : {
			"change .permission-list" : "changePermission"
		},

		changePermission : function(event) {
			var select = $(event.target);
			var permission_id = select.val();
			var project_id = select.attr('id').replace("permission-list-", "");

			$.ajax({
				type : 'POST',
				url : UMod.permission.url.add,
				data : {
					project : project_id,
					permission : permission_id
				},
				dataType : "json",
				success : function() {

				}
			});
		},

		makeTogglePanel : function() {
			var infoSettings = {
				initiallyOpen : true,
				title : UMod.message.permissionsPanelTitle
			};
			this.$("#project-permission-panel").togglePanel(infoSettings);
		},
		configureTable : function() {
			$("#permission-table").squashTable({
				"fnRowCallback" : function(nRow, data){
					var select = $("#permission-table-templates select").clone();
					select.attr('id', 'permission-list-' + data["project-id"]);
					select.val(data['permission-name']);
					$('.permission-select', nRow).empty().append(select);
				}
			}, {}); // pure DOM conf
		},
		configurePopups : function() {
			this.configureAddPermissionDialog();
			this.configureRemovePermissionDialog();
		},
		configureButtons : function() {
			this.$("#add-permission-button").on('click', $.proxy(this.openAddPermission, this));
		},

		confirmRemovePermission : function(event) {
			var hasPermission = ($("#permission-table").squashTable().getSelectedIds().length > 0);
			if (hasPermission) {
				this.confirmRemovePermissionDialog.confirmDialog("open");
			}
		},

		openAddPermission : function() {
			this.addPermissionDialog.confirmDialog('open');
		},

		removePermissions : function(event) {
			var table = $("#permission-table").squashTable();
			var ids = table.getSelectedIds();
			$.ajax({
				url : UMod.permission.url.remove,
				type : 'post',
				data : {
					project : ids[0]
				}
			}).done(function() {
				$("#permission-table").squashTable().refresh();
			});

		},

		addPermission : function(event) {
			var dialog = this.addPermissionDialog;
			var name = dialog.find('#add-permission-input').val();
		},

		configureRemovePermissionDialog : function() {
			this.confirmRemovePermissionDialog = $("#remove-permission-dialog").confirmDialog();
			this.confirmRemovePermissionDialog.on("confirmdialogconfirm", $.proxy(this.removePermissions, this));
		},

		configureAddPermissionDialog : function() {
			var addPermissionDialog = $("#add-permission-dialog").confirmDialog();
			var table = $("#permission-table").squashTable();
			addPermissionDialog.on("confirmdialogvalidate", function() {
				$.ajax({
					type : 'POST',
					url : UMod.permission.url.add,
					data : {
						project : $("#project-input").val(),
						permission : $("#permission-input").val()
					},
					dataType : "json",
					success : function() {
						$("#permission-table").squashTable().refresh();

					}
				});
			});

			addPermissionDialog.on("confirmdialogconfirm", $.proxy(this.addPermission, this));

			addPermissionDialog.find('#add-permission-input').autocomplete();

			addPermissionDialog.on('confirmdialogopen', function() {
				var dialog = addPermissionDialog;
				var input = dialog.find('#add-permission-input');
				dialog.activate('wait');

				$.ajax({
					url : UMod.permission.url.popup,
					dataType : 'json'
				}).success(function(json) {
					if (json.myprojectList.length === 0) {
						dialog.activate('no-more-projects');
					} else {
						$("#project-input").html("");
						for ( var i = 0; i < json.myprojectList.length; i++) {
							var text = json.myprojectList[i].name;
							var value = json.myprojectList[i].id;
							var option = new Option(text, value);
							$(option).html(text); // for ie8
							$("#project-input").append(option);
						}
						dialog.activate('main');
					}

				});
			});

			addPermissionDialog.activate = function(arg) {
				var cls = '.' + arg;
				this.find('div').not('.popup-dialog-buttonpane').filter(cls).show().end().not(cls).hide();
				if (arg !== 'main') {
					this.next().find('button:first').hide();
				} else {
					this.next().find('button:first').show();
				}
			};

			this.addPermissionDialog = addPermissionDialog;
		}
	});
	return UserPermissionsPanel;
});