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
define(['jquery'], function($){
	
	if (squashtm && squashtm.datatable && squashtm.datatable.defaults){
		return squashtm.datatable.defaults;
	}
	
	squashtm = squashtm || {};
	
	// defines datatable defaults settings and puts them in the squash
	// namespace.
	
	var oLanguage;
	$.ajax({
		url : squashtm.app.contextRoot+'/datatables/messages',
		type : 'get',
		async : false,
		dataType : 'json',
		success : function(json){
			oLanguage = json;
		}
	});
	
	var datatableDefaults = {
		"bJQueryUI" : true,
		"bAutoWidth" : false,
		"bFilter" : false,
		"bPaginate" : true,
		"sPaginationType" : "squash",
		"iDisplayLength" : 50,
		"bServerSide" : true,
		"bRetrieve" : true,
		"sDom" : 't<"dataTables_footer"lp>',
		"oLanguage" : oLanguage
	};

	squashtm.datatable = squashtm.datatable || {};
	squashtm.datatable.defaults = datatableDefaults;
	
	
	return squashtm.datatable.defaults;
});