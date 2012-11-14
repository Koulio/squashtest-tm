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



/**
 * How to : 
 * 
 * the method 'get' accepts a simple javascript object, which values are either string or other simple javascript objects.
 * See it as a composite map of which the leaves are strings.
 * 
 *  Those strings are keys for i18n properties. 
 *  
 *  The function returns the resolved object, of which the keys has been replaced by the corresponding value.
 *  
 *  Note : does not support parameterized messages for the moment.
 *  
 * Example :
 * 
 *  {
 *  	title : 'widget.title',
 *  	browse : {
 *  		back : 'widget.back',
 *  		forth : 'widget.forth'
 *  	},
 *  	buttons : {
 *  		cancel : 'label.Cancel',
 *  		confirm : 'label.Confirm'
 *  	} 
 *  	
 *  }
 * 
 */
define(["jquery"], function($){
	var serviceURL = squashtm.app.contextRoot+"/localization/filler";
	
	return {		
		get : function(messageObject){
			
			$.ajax({
				url : serviceURL,
				headers : {
					'Content-Type' : 'application/json'					
				},
				dataType : 'json',
				type : 'POST',
				data : JSON.stringify(messageObject),
				async : false
			})
			.success(function(json){
				return json;
			});			
		}
	};


});