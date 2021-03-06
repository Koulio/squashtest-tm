/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
/* TODO move that file in a folder */
	define([], function() {

		var dryRunStart = function(runnerUrl) {
			return $.ajax({
				url : runnerUrl,
				method : 'get',
				data : {
					'dry-run' : ''
				}
			});
		};

		var startResumeClassic = function(runnerUrl) {
			var data = {
				'optimized' : 'false'
			};
			var winDef = {
				name : "classicExecutionRunner",
				features : "height=690, width=810, resizable, scrollbars, dialog, alwaysRaised"
			};
			$.open(runnerUrl, data, winDef);

		};

		var startResumeIEO = function(runnerUrl) {
			var data = {
				'optimized' : 'true'
			};
			var winDef = {};
			runnerUrl = runnerUrl + "?optimized=true";
      window.open(runnerUrl);
		};



	return {

		start : function(runnerUrl, isIEO){
			if (isIEO){
				dryRunStart(runnerUrl).done(startResumeIEO(runnerUrl));
			}
			else {
			dryRunStart(runnerUrl).done(startResumeClassic(runnerUrl));
			}}

	};
	});
