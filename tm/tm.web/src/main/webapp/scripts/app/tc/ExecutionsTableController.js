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
define([ "jquery", "squash.datatables", "jqueryui" ],
		function($) {
			function init() {
				var execUrlRoot = squashtm.app.contextRoot + "/executions";

				function addExecutionLink(row, data) {
					var id = data["exec-id"];
					var nameCell = row.find("td.exec-name");
					var name = data["exec-name"];
					var href = execUrlRoot + "/" + id;
					nameCell.html('<a href="' + href + '">' + name + '</a>');
				}

				function addStatusBall(row, data) {
					var status = data["raw-exec-status"].toLowerCase();
					row.find("td.exec-status")
							.addClass("exec-status-" + status);
				}

				function drawExecutionRow(nRow, aData, iDisplayIndex,
						iDisplayIndexFull) {
					var row = $(nRow);
					addExecutionLink(row, aData);
					addStatusBall(row, aData);
				}

				var conf = squashtm.app.testCaseExecutionsTable;

				var table = $("#execs-table").dataTable({
					"oLanguage" : {
						"sUrl" : conf.languageUrl
					},
					"bJQueryUI" : true,
					"bAutoWidth" : false,
					"bFilter" : false,
					"bPaginate" : true,
					"sPaginationType" : "squash",
					"iDisplayLength" : conf.displayLength,
					"bProcessing" : true,
					"bServerSide" : true,
					"sAjaxSource" : conf.ajaxSource,
					"bDeferRender" : true,
					"bRetrieve" : true,
					"sDom" : 't<"dataTables_footer"lirp>',
					"iDeferLoading" : conf.deferLoading,
					"aaSorting" : [ [ 10, "desc" ] ],
					"fnRowCallback" : drawExecutionRow,
					"aoColumnDefs" : [ {
						"bVisible" : false,
						"aTargets" : [ 0 ],
						"sClass" : "exec-id",
						"mDataProp" : "exec-id"
					}, {
						"bSortable" : true,
						"aTargets" : [ 1 ],
						"mDataProp" : "project-name"
					}, {
						"bSortable" : true,
						"aTargets" : [ 2 ],
						"mDataProp" : "campaign-name"
					}, {
						"bSortable" : true,
						"aTargets" : [ 3 ],
						"mDataProp" : "iteration-name"
					}, {
						"bSortable" : true,
						"aTargets" : [ 4 ],
						"sClass" : "exec-name",
						"mDataProp" : "exec-name"
					}, {
						"bSortable" : true,
						"aTargets" : [ 5 ],
						"mDataProp" : "exec-mode"
					}, {
						"bSortable" : true,
						"aTargets" : [ 6 ],
						"mDataProp" : "test-suite-name"
					}, {
						"bVisible" : false,
						"aTargets" : [ 7 ],
						"sClass" : "raw-exec-status",
						"mDataProp" : "raw-exec-status"
					}, {
						"bSortable" : true,
						"aTargets" : [ 8 ],
						"sClass" : "exec-status",
						"mDataProp" : "exec-status"
					}, {
						"bSortable" : true,
						"aTargets" : [ 9 ],
						"mDataProp" : "last-exec-by"
					}, {
						"bSortable" : true,
						"aTargets" : [ 10 ],
						"mDataProp" : "last-exec-on"
					} ]
				});

				$(".unstyled").removeClass("unstyled");
			}

			return {
				init : init
			};
		});
