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
define([ "jquery", "backbone", "app/util/StringUtil", "underscore" ], function($, Backbone, StringUtil, _) {

	/*
	 * Defines the model for the list of filtered projects
	 */
	var ProjectFilterModel = Backbone.Model.extend({

		defaults : {
			projectIds :[]
		},
		
		
		select : function(ids) {
			this.attributes.projectIds.val = _.union(this.attributes.projectIds.val, ids);
			
		},
		deselect : function(ids) {
			this.attributes.projectIds.val = _.difference(this.attributes.projectIds.val, ids);
		},
				
		changeProjectState : function(id, checked) {
			if(checked){
				this.attributes.projectIds.val.push(id);
			}else{
				this.attributes.projectIds.val = _.without(this.attributes.projectIds.val, id);
			}
			
		}

	});

	return ProjectFilterModel;
});