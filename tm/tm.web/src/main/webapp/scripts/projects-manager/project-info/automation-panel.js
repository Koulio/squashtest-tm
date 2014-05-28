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
/*
 settings :
 - tmProjectURL : the url of the TM project
 - availableServers : an array of TestAutomationServer
 - TAServerId : the id of the selected server if there is one, or null if none
 */
define([ "jquery", "jeditable.selectJEditable", "./AddTAProjectsDialog",, "squashtable", "jquery.squash.formdialog" ],
		function($, SelectJEditable, BindPopup, WTF) {
			// *************************************** ConfirmChangePopup **********************************************
			var ConfirmChangePopup = Backbone.View.extend({

				el : "#ta-server-confirm-popup",

				initialize : function(conf) {
					this.changeUrl = conf.tmProjectURL + '/test-automation-server';
					var dialog = this.$el.formDialog();
				},

				events : {
					"formdialogconfirm" : "confirm",
					"formdialogcancel" : "cancel",
					"formdialogclose" : "close"
				},
				confirm : function() {
					var self = this;
					$.ajax({
						url : self.changeUrl,
						type : "post",
						data : {
							serverId : this.newSelectedId
						}
					}).done(function() {
						//trigger confirm success event with active selected id
						self.trigger("confirmChangeServerPopup.confirm.success", [ self.newSelectedId ]);
						self.selectedId = self.newSelectedId;
						self.newSelectedId = null;
						self.close();
					}).fail(function(wtf){
						WTF.handleJsonResponseError(wtf);
						//trigger confirm fail event with active selected id
						self.trigger("confirmChangeServerPopup.confirm.fail", [ self.selectedId ]);
						self.newSelectedId = null;
					});
				},
				cancel : function() {
					var self = this;
					//trigger cancel event with active selected id
					this.trigger("confirmChangeServerPopup.cancel", [ self.selectedId ]);
					self.newSelectedId = null;
					this.close();
				},

				close : function() {
					this.$el.formDialog("close");
				},

				show : function(newSelected) {
					var self = this;
					this.newSelectedId = newSelected;
					this.$el.formDialog("open");
					this.$el.formDialog('setState', 'pleasewait');
					// edit state of popup depending on datas retrieved by ajax
					$.ajax(
							{
								url : squashtm.app.contextRoot + "/test-automation-servers/" + self.selectedId +
										"/usage-status",
								type : "GET"
							}).then(function(status) {
						if (!status.hasExecutedTests) {
							self.$el.formDialog('setState', 'case1');
						} else {
							self.$el.formDialog('setState', 'case2');
						}
					});

				},

				setSelected : function(selected) {
					this.selectedId = selected;
				}
			});
			
			// *************************************** UnbindPopup **********************************************

			var UnbindPopup = Backbone.View.extend({
				el : "#ta-projects-unbind-popup",

				initialize : function() {
					this.$el.formDialog();
				},
				events : {
					'formdialogconfirm' : 'confirm',
					'formdialogcancel' : 'cancel'
				},
				confirm : function() {
					this.trigger("unbindTAProjectPopup.confirm");
					var deletedId = this.$el.data('entity-id');
					alert('Confirmed deletion of ' + deletedId + '!');
				},
				cancel : function() {
					this.trigger("unbindTAProjectPopup.cancel");
					alert('Canceled !');
				}
			});
			// *************************************** AutomationPanel **********************************************

			var AutomationPanel = Backbone.View.extend({

				el : "#test-automation-management-panel",

				initialize : function(conf, popups) {
					var self = this;
					this.popups = popups;
					this.initSelect(conf);
					this.table = $("#ta-projects-table").squashTable({}, {});
					
					//listens to change-server-popup cancel and fail events to update the server's select-jeditable status accordingly
					this.onChangeServerComplete = $.proxy(this._onChangeServerComplete, this);
					this.listenTo(self.popups.confirmChangePopup, "confirmChangeServerPopup.confirm.success",
							self.onChangeServerComplete);
					this.listenTo(self.popups.confirmChangePopup, "confirmChangeServerPopup.cancel",
							self.onChangeServerComplete);
					this.listenTo(self.popups.confirmChangePopup, "confirmChangeServerPopup.confirm.fail",
							self.onChangeServerComplete);
				},

				events : {
					"click #ta-projects-bind-button" : "openBindPopup"
				},
				openBindPopup : function() {
					this.popups.bindPopup.show();
				},
				
				//when the select jeditable popup completes we change the server's select-jeditable status accordingly.
				_onChangeServerComplete : function(newServerId) {
					this.selectServer.setValue(newServerId);
					this.table.refresh();
				},
				
				initSelect : function(conf) {
					var self = this;
					var data = {
						'0' : translator.get('label.NoServer')
					};

					for ( var i = 0, len = conf.availableServers.length; i < len; i++) {
						var server = conf.availableServers[i];
						data[server.id] = server.name;
					}

					if (conf.TAServerId !== null) {
						data.selected = conf.TAServerId;
					}
					this.popups.confirmChangePopup.setSelected(data.selected);
					var targetFunction = function(value, settings) {
						self.popups.confirmChangePopup.show(value);
						return value;
					};
					this.selectServer = new SelectJEditable({
						target : targetFunction,
						componentId : "selected-ta-server-span",
						jeditableSettings : {
							data : data
						}
					});
				}
			});
			// *************************************** automation panel **********************************************

			return {
				init : function(conf) {
					var popups = {
						unbindPopup : new UnbindPopup(conf),
						confirmChangePopup : new ConfirmChangePopup(conf),
						bindPopup : new BindPopup(conf)
					};
					new AutomationPanel(conf, popups);
				}
			};

		});