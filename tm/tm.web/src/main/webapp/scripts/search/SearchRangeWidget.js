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
define(["jquery", "jqueryui"], function($){

	var searchwidget = $.widget("search.searchRangeWidget", {
		
		options : {

		},
		
		_create : function(){
			this._super();
		},
		
		fieldvalue : function(value){
			
			if(!value){
				var checked = $($(this.element.children()[0]).children()[0]).prop('checked');
				var min = $($(this.element.children()[0]).children()[2]).val();
				var max = $($(this.element.children()[0]).children()[4]).val();
				var id = $(this.element).attr("id");
				if(checked){
					return {"type" : "RANGE",
					    "minValue" : min,
						"maxValue" : max};
				} else {
					return null;
				}
			} else {
				$($(this.element.children()[0]).children()[0]).attr('checked', 'checked');
				$($(this.element.children()[0]).children()[2]).val(value.minValue);
				$($(this.element.children()[0]).children()[4]).val(value.maxValue);
			}
		}, 
		
		createDom : function(id){
			
		}
	 });
	return searchwidget;
});
