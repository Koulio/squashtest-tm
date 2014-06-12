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
define([ "jquery" ], function($) {
	function isBlank(val) {
		// IE8 does not not compute the regex "^\s*$" properly, hence the "trim"
		return (!val) || $.trim(val) === "";
	}
	
	function isEmpty(val) {
		return (!val) || val === "";
	}
	
	function parseAssignation(atom) {
		var members = atom.split(/\s*=\s*/);
		return {
			name : members[0],
			value : (members.length > 1) ? $.trim(members[1]) : 'true'
		};
	}
	function parseSequence(seq) {
		var result = [];
		var statements = seq.split(/\s*,\s*/);
		var i = 0, length = statements.length;

		for (i = 0; i < length; i++) {
			var stmt = statements[i];
			var parser = (stmt.indexOf(',') !== -1) ? parseSequence : parseAssignation;
			result.push(parser(stmt));
		}

		return result;
	}
	
	function getParsedSequenceAttribute(parsedSequence, key){
		for(var k=0; k<parsedSequence.length; k++){
			if(parsedSequence[k].name == key){
				return parsedSequence[k].value;
			}
		}
	}

	return {
		isBlank : isBlank,
		isEmpty : isEmpty,
		parseSequence : parseSequence,
		getParsedSequenceAttribute :getParsedSequenceAttribute
	};
});