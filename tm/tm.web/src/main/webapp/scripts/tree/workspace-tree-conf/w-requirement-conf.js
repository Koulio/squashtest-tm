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


define(function(){
	function generate(){

	
		return {
			"types" : {
				"max_depth" : -2, // unlimited without check
				"max_children" : -2, // unlimited w/o check
				"valid_children" : [ "drive" ],
				"start_drag" : false,
				"move_node" : true,
				"delete_node" : false,
				"remove" : false,
				"types" : {
					"requirement" : {
						"valid_children" : ['requirement'],
						"icon" : {
							"image" : baseURL+'/images/Icon_Tree_TestCase.png'
						}
					},
					"folder" : {
						"valid_children" : [ "requirement", "folder" ],
						"icon" : {
							"image" : baseURL+'/images/Icon_Tree_Folder.png'
						}
					},
					"drive" : {
						"valid_children" : [ "requirement", "folder" ],
						"icon" : {
							"image" : baseURL+'/images/root.png'
						}
					}
				}
			}
		}
	
	}
});