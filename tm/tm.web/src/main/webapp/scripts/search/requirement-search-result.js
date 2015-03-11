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
define([ "jquery", "backbone", "underscore", "app/util/StringUtil","workspace.routing","workspace.event-bus",
        "./RequirementSearchResultTable", "squash.translator", "app/ws/squashtm.notification",
        "workspace.projects", "jquery.squash", "jqueryui",
		"jquery.squash.togglepanel", "squashtable",
		"jquery.squash.oneshotdialog", "jquery.squash.messagedialog",
		"jquery.squash.confirmdialog", "jquery.squash.milestoneDialog" ], 
		function($, Backbone, _, StringUtil, routing, eventBus, RequirementSearchResultTable, translator, notification, projects) {
	
	var RequirementSearchResultPanel = Backbone.View.extend({

		expanded : false,
		el : "#sub-page",

		initialize : function() {
			this.configureModifyResultsDialog();
			this.getIdsOfSelectedTableRowList =  $.proxy(this._getIdsOfSelectedTableRowList, this);
			this.getIdsOfEditableSelectedTableRowList = $.proxy(this._getIdsOfEditableSelectedTableRowList, this);
			this.updateDisplayedValueInColumn =  $.proxy(this._updateDisplayedValueInColumn, this);
			var model = JSON.parse($("#searchModel").text());
			this.isAssociation = !!$("#associationType").length;
			if(this.isAssociation){
				this.associationType = $("#associationType").text();
				this.associationId = $("#associationId").text();
			}
			this.model = model;
			new RequirementSearchResultTable(model, this.isAssociation, this.associationType, this.associationId);
		},

		events : {
			"click #export-search-result-button" : "exportResults",
			"click #modify-search-result-button" : "editResults",
			"click #new-search-button" : "newSearch",
			"click #modify-search-button" : "modifySearch",
			"click #associate-selection-button" : "associateSelection",
			"click #select-all-button" : "selectAllForAssocation",
			"click #associate-all-button" : "associateAll",
			"click #deselect-all-button" : "deselectAll",
			"click #modify-search-result-milestone-button" : "editMilestone"
		},

	
		
		editMilestone : function(){
			
			var table = $('#requirement-search-result-table').squashTable();
			var ids = table.getSelectedIds(); //ids of requirement
			if (this._containsDuplicate(ids)){

				var warn = translator.get({
					errorTitle : 'popup.title.Info',
					errorMessage : 'message.search.mass-modif.milestone.multiple-req-version'
				});
				$.squash.openMessage(warn.errorTitle, warn.errorMessage);	
			} else {
				
				if (! this.addModifyMilestoneDialog){
					this.initModifyMilestoneDialog();
				} else {
					this.updateMilestoneDialog();
				}
				
				this.addModifyMilestoneDialog.milestoneDialog('open');
			}
			
			
		},

	
		initModifyMilestoneDialog : function() {

			var self = this;
			
			var table = $('#requirement-search-result-table').squashTable();
			var ids = this._getRequirementVersionIdSelectedTableRowList(table);
		
			var dialogOptions = {
					tableSource : routing.buildURL('search-reqV.mass-change.associable-milestone', ids),
					milestonesURL : routing.buildURL('search-reqV.mass-change.bindmilestones', ids), 
					identity : "",
				};
		
			var addModifyMilestoneDialog = $('.bind-milestone-dialog');
            addModifyMilestoneDialog.milestoneDialog(dialogOptions);
			this.addModifyMilestoneDialog = addModifyMilestoneDialog;
			
			
			eventBus.on("node.bindmilestones", function(){
				var table = $('#requirement-search-result-table').squashTable();
				
				table.drawcallbacks.push(function() {   table.deselectRows();
				self._restoreSelect();});
				self._saveSelect();
                table._fnAjaxUpdate();

			});
			
		},
		
		updateMilestoneDialog : function(){
			var self = this;	
			var table = $('#requirement-search-result-table').squashTable();
			var ids = this._getRequirementVersionIdSelectedTableRowList(table);
			
			var tab = $('.bind-milestone-dialog-table').squashTable();
			tab.fnSettings().sAjaxSource = routing.buildURL('search-reqV.mass-change.associable-milestone', ids);
			var addModifyMilestoneDialog = $('.bind-milestone-dialog').milestoneDialog();
			addModifyMilestoneDialog.data().milestoneDialog.options.milestonesURL = routing.buildURL('search-reqV.mass-change.bindmilestones', ids);
	
		},
		
		associateSelection : function(){
			var table = $('#requirement-search-result-table').dataTable();
			var ids = table.squashTable().getSelectedIds();
			if(ids.length === 0){
				notification.showError(translator.get('message.noLinesSelected'));
				return;
			}
			var id = this.associationId;
			var  targetUrl = "";	
			var  returnUrl = "";
			if("testcase" === this.associationType){
				targetUrl =  squashtm.app.contextRoot + "/test-cases/" + id + "/verified-requirements";
			}else if ("teststep" === this.associationType){
				targetUrl = squashtm.app.contextRoot + "/test-steps/" + id + "/verified-requirements";
			}
			
			$.ajax({
				type: "POST",
				url :targetUrl,
				data : { "requirementsIds[]" : ids }
			}).done(function() {
				$("#back").click();
			});
			
		},
		
		selectAllForAssocation : function(){
			var table = $('#requirement-search-result-table').dataTable();
			var rows = table.fnGetNodes();
			var ids = [];
			$(rows).each(function(index, row) {
				ids.push(parseInt($(".element_id", row).text(),10));
			});
			
			table.squashTable().selectRows(ids);
		},
		
		deselectAll : function(){
			var table = $('#requirement-search-result-table').dataTable();
			table.squashTable().deselectRows();
		},
		
		associateAll : function(){
			this.selectAllForAssocation();
			this.associateSelection();
		},
		
		modifySearch : function(){
			if(this.isAssociation){
				this.post(squashtm.app.contextRoot + "advanced-search?searchDomain=requirement&id="+this.associationId+"&associateResultWithType="+this.associationType, {
					searchModel : JSON.stringify(this.model)
				});	
			} else {
				this.post(squashtm.app.contextRoot + "advanced-search?searchDomain=requirement", {
					searchModel : JSON.stringify(this.model)
				});	
			}
		},
		
		post : function (URL, PARAMS) {
			var temp=document.createElement("form");
			temp.action=URL;
			temp.method="POST";
			temp.style.display="none";
			temp.acceptCharset="UTF-8";
			for(var x in PARAMS) {
				var opt=document.createElement("textarea");
				opt.name=x;
				opt.value=PARAMS[x];
				temp.appendChild(opt);
			}
			document.body.appendChild(temp);
			temp.submit();
			return temp;
		},
		
		newSearch : function(){
			
			if(this.isAssociation){
				document.location.href= squashtm.app.contextRoot +"/advanced-search?searchDomain=requirement&id="+this.associationId+"&associateResultWithType="+this.associationType;
			} else {
				document.location.href= squashtm.app.contextRoot +"/advanced-search?searchDomain=requirement";
			}
		},
		
		exportResults : function(){
			document.location.href= squashtm.app.contextRoot +"/advanced-search?requirement&export=csv&searchModel="+JSON.stringify(this.model);
		},
		
		editResults : function(){
			this.addModifyResultDialog.confirmDialog("open");
		},
		
		
		_saveSelect : function saveSelect(){
		var table = $('#requirement-search-result-table').squashTable();
		this.selectedIds = this._getRequirementVersionIdSelectedTableRowList(table);
		},
		
		_restoreSelect : function restoreSelect(){
			
			var selectedIds = this.selectedIds;
			var table = $('#requirement-search-result-table').squashTable();
			
			if ((selectedIds instanceof Array) && (selectedIds.length > 0)) {
				var rows = table.fnGetNodes();
				$(rows).filter(function() {		
					var rId = table.fnGetData(this)["requirement-version-id"];
					return $.inArray(rId, selectedIds) != -1;
				}).addClass('ui-state-row-selected');
			}
			
		},
		
		_containsDuplicate : function containsDuplicate(arr) {arr.sort();
		var last = arr[0];
		for (var i=1; i<arr.length; i++) {
		   if (arr[i] == last) {return true;}
		   last = arr[i];
		}
		return false;
		},
		
		_getIdsOfSelectedTableRowList : function(dataTable) {
			var rows = dataTable.fnGetNodes();
			var ids = [];
			
			$( rows ).each(function(index, row) {
				if ($( row ).attr('class').search('selected') != -1) {
					var data = dataTable.fnGetData(row);
					ids.push(data["requirement-id"]);
				}
			});
			
			return ids;
		},
		_getRequirementVersionIdSelectedTableRowList : function(dataTable) {
			var rows = dataTable.fnGetNodes();
			var ids = [];
			
			$( rows ).each(function(index, row) {
				if ($( row ).attr('class').search('selected') != -1) {
					var data = dataTable.fnGetData(row);
						ids.push(data["requirement-version-id"]);		
				}
			});
			
			return ids;
		},
		_getIdsOfEditableSelectedTableRowList : function(dataTable) {
			var rows = dataTable.fnGetNodes();
			var ids = [];
			
			$( rows ).each(function(index, row) {
				if ($( row ).attr('class').search('selected') != -1) {
					var data = dataTable.fnGetData(row);
					if(data["editable"]){
						ids.push(data["requirement-id"]);
					} 
				}
			});
			
			return ids;
		},
		
		
		enableCategoryModification : function(dialog, table){
			
			var rows = table.getSelectedRows();
			
			if (rows.length === 0){
				return;
			}

			// reset the controls
			$("#modify-search-result-dialog-project-conf-warning").hide();
			$(".mass-change-forbidden").hide();
			$(".mass-change-allowed").show();
			$(".mass-change-infolist-combo").prop('disabled', false);
			
			// find the selected projects unique ids
			var selectedProjects = [];
			rows.each(function(row){
				selectedProjects.push(table.fnGetData(row)['project-id']);
			});
			selectedProjects = _.uniq(selectedProjects);
			
			// check for conflicts
			var difCat = projects.haveDifferentInfolists(selectedProjects, ["category"]);
			
			function populateCombo(select, infolistName){
				var p = projects.findProject(selectedProjects[0]);
				select.empty();
				
				for (var i=0;i<p[infolistName].items.length; i++){
					var item = p[infolistName].items[i];
					var opt = $('<option/>', {
						value : item.code,
						html : item.friendlyLabel
					});
					select.append(opt);
				}				
			}
			
			if (difCat){
				$("#modify-search-result-dialog-project-conf-warning").show();
				$("#category-disabled-icon").show();
				$("#category-checkbox").hide();
			}
			else{
				// we can move on and populate the combo
				populateCombo($("#category-combo select"), 'requirementCategories');
			}
			
		},
		
		_updateDisplayedValueInColumn : function(dataTable, column) {
			var rows = dataTable.fnGetNodes();
			
			$( rows ).each(function(index, row) {
				if ($( row ).attr('class').search('selected') != -1  && dataTable.fnGetData(row)["editable"]) {
					var value = $("#"+column+"-combo").find('option:selected').text();
					$(".editable_"+column, row).text(value);
				}
			});

		},
				
		configureModifyResultsDialog : function() {
			var self = this;
			var addModifyResultDialog = $("#modify-search-result-dialog").confirmDialog();

			
			function loadCombos(comboname){
				$.ajax({
					url : squashtm.app.contextRoot + "/requirements/"+ comboname +"-data",
					dataType : 'json'
				})
				.success(function(json) {
					var combo = $("<select/>"),
						comboCell = $("#"+comboname);
						
					 $.each(json, function(key, value){ 
						var option = $("<option/>",{
							value : key,
							html : value
						});
						combo.append(option);
					 });
					 comboCell.append(combo);
				});
			}
			

			/*
			 * configure the comboboxes. Note that the category combos cannot 
			 * be initialized before we know which requirements were selected.  
			 */
			loadCombos("criticality-combo");
			loadCombos("status-combo");
			$("#category-combo").append('<select/>');
			
			
			addModifyResultDialog.on('change', ':checkbox', function(evt){
				var cbx = $(evt.currentTarget),
					state = cbx.prop('checked'),
					select = cbx.parent().siblings().last().find('select');
				
				select.prop('disabled', !state);
			});
			
			addModifyResultDialog.on("confirmdialogvalidate",function() {});

			addModifyResultDialog.on("confirmdialogconfirm",function() {
				var table = $('#requirement-search-result-table').dataTable();
				var ids = self.getIdsOfSelectedTableRowList(table);
				var columns = ["criticality","category","status"];
				var index = 0;
				
				for(index=0; index<columns.length; index++){
					if($("#"+columns[index]+"-checkbox").prop('checked')){
						self.updateDisplayedValueInColumn(table, columns[index]);
						var value = $("#"+columns[index]+"-combo").find('option:selected').val();
						for(var i=0; i<ids.length; i++){
							var urlPOST = squashtm.app.contextRoot + "/requirements/" + ids[i];
							$.post(urlPOST, {
								value : value,
								id : "requirement-"+columns[index]	
							});
						}
					}
				}
			});
			
			addModifyResultDialog.on('confirmdialogopen',function() {
				addModifyResultDialog.find(':checkbox').prop('checked', false);
				addModifyResultDialog.find('select').prop('disabled', true);
			
				var table = $('#requirement-search-result-table').squashTable();
				var ids = self.getIdsOfSelectedTableRowList(table);
				var editableIds = self.getIdsOfEditableSelectedTableRowList(table);
				if(ids.length === 0) {							
					notification.showError(translator.get('message.noLinesSelected'));
					$(this).confirmDialog('close');
				}else if (editableIds.length === 0){
					notification.showError(translator.get('message.search.modify.noLineWithWritingRightsOrWrongStatus'));
					$(this).confirmDialog('close');
				}else if (editableIds.length < ids.length){							
					notification.showError(translator.get('message.search.modify.noWritingRightsOrWrongStatus'));
					noWritingRightsDialog.messageDialog('open');
				}
				
				self.enableCategoryModification(addModifyResultDialog, table);

			});

			addModifyResultDialog.activate = function(arg) {};

			this.addModifyResultDialog = addModifyResultDialog;
		}
		
	});
	return RequirementSearchResultPanel;
});






