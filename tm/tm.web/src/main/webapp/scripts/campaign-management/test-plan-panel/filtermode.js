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
define(["jquery"], function($){

	function _hideFilterFields(_bNoredraw) {
		var table = $("#test-cases-table").squashTable(),
			settings = table.fnSettings();
		table.find(".th_input").hide();
		
		var inputs = table.find('.filter_input');
		inputs.each(function(index){
			settings.aoPreSearchCols[index].sSearch = '';
		});
		
		if ( _bNoredraw !== true){
			table.refresh();
		}
	}

	function _showFilterFields() {
		var table = $("#test-cases-table").squashTable(),
			settings = table.fnSettings();
		
		table.find(".th_input").show();
		
		var inputs = table.find('.filter_input');
		inputs.each(function(index){
			settings.aoPreSearchCols[index].sSearch = this.value;
		});
		
		table.refresh();
	}
	


	function _initializeFilterFields(initconf) {
		
		function _createCombo(th, id, content){
			var combo = $("<select id='"+id+"' class='th_input filter_input'/>");
			
			var nullOption = new Option("", "");
			$(nullOption).html("");
			
			combo.append(nullOption);
			
			$.each(content, function(index, value) {
				var o = new Option(value, index);
				$(o).html(value);
				combo.append(o);
			});			
			
			th.append(combo);
		}
		

		$("#test-cases-table_filter").hide();
		
		var users = initconf.basic.assignableUsers,
			weights = initconf.basic.weights,
			modes = initconf.basic.modes;
		
		var table = $("#test-cases-table");
		
		
		
		table.find('.tp-th-project-name,.tp-th-reference,.tp-th-name')
			.append("<input class='th_input filter_input'/>");
		
		
		var execmodeTH = table.find("th.tp-th-exec-mode"),
			importanceTH = table.find('.tp-th-importance'),
			statusTH = table.find('.tp-th-status'),
			assigneeTH = table.find('.tp-th-assignee');
		

		_createCombo(execmodeTH, "#filter-mode-combo", modes);
		_createCombo(assigneeTH, "#filter-user-combo", users);
		_createCombo(importanceTH, "#filter-weight-combo", weights);

		
		
		$(".th_input").click(function(event) {
			event.stopPropagation();
		}).keypress(function(event){
			if (event.which == 13 )
			{
				event.stopPropagation();
				event.preventDefault();
				event.target.blur();
				event.target.focus();
			}
		});
		
		table.find("th").hover(function(event) {
			event.stopPropagation();
		});
	
		$(".filter_input").change(function() {
			var sTable = table.squashTable(),
				settings = sTable.fnSettings(),
				api = settings.oApi,
				headers = table.find('th');
			
			var visiIndex =  headers.index($(this).parents('th:first')),
				realIndex = api._fnVisibleToColumnIndex( settings, visiIndex );
			
			sTable.fnFilter(this.value, realIndex);
		});

		
		_hideFilterFields(true);
	}
	
	return {
		initializeFilterFields : _initializeFilterFields,
		hideFilterFields : _hideFilterFields,
		showFilterFields : _showFilterFields
	};

});