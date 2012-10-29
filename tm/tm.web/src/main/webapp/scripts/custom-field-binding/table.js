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


/*
 * settings (see the doc for jquery datatable for details about the native settings):
 * 	{
 * 		selector : the selector for the table,
 * 		languageUrl : the url where to fetch the localization conf object,
 * 		getUrl : the ajaxSource (native),
 * 		deleteUrl : the url where to send DELETE request,
 * 		deferLoading : the iDeferLoading (native),
 * 		oklabel : text for the ok button,
 * 		cancellabel : text for the cancel button
 * }
 */

define(["jquery", "jquery.squash.datatables"], function($){

	return function(settings){
		
		/*
		 * poor's man currying. Some part are taken from jquery.datatables.js
		 * 
		 * UNDER WORK
		 */
		/*function _setObjectPropertyFromDOM(property){
			

			function exists(data){
				var localD = data;
				var a = property.split('.');
				for ( var i=0, iLen=a.length-1 ; i<iLen ; i++ )
				{
					localD = localD[ a[i] ];
					if ( localD === undefined )
					{
						return false;
					}
				}
				return true;
			}
			
			function setValue(data, val){
				var localD = data;
				var a = property.split('.');
				for ( var i=0, iLen=a.length-1 ; i<iLen; i++ )
				{
					var ppt = a[i];
					if (localD[ppt]===undefined){
						localD[ppt] = {};
					}
					localD = localD[ppt];
				}
				localD[a[a.length-1]] = val;
			}
			
			function getValue(data){
				var localD = data;
				var a = property.split('.');
				for ( var i=0, iLen=a.length-1 ; i<iLen ; i++ )
				{
					localD = localD[ a[i] ];
				}
				return localD;		
			}
			
			return function(data,operation,val){
				if (operation=='set' && exists(data)==false){
					setValue(data, val);
				}
				else{
					return getValue(data);
				}
			}
			
		}

		var fn = _setObjectPropertyFromDOM('customField.name');
		*/
		var tableConf = {
			oLanguage :{
				sUrl : settings.languageUrl
			},
			sAjaxSource : settings.getUrl,
			iDeferLoading : settings.deferLoading,
			aoColumnDefs :[
				{'bSortable' : false, 'bVisible' : false, 'aTargets' : [0], 'mDataProp' : 'id'},
				{'bSortable' : false, 'bVisible' : true,  'aTargets' : [1], 'mDataProp' : 'position', 'sWidth' : '2em', 'sClass' : 'centered ui-state-default drag-handle select-handle'},
				//{'bSortable' : false, 'bVisible' : true,  'aTargets' : [2], 'mDataProp' : fn},
				{'bSortable' : false, 'bVisible' : true,  'aTargets' : [2], 'mDataProp' : function(data,operation,val){
					if (operation==='set' && data.customField===undefined){
						data.customField ={};
						data.customField.name=val;
					}
					else{
						return data.customField.name;
					}
				}},				
				{'bSortable' : false, 'bVisible' : true,  'aTargets' : [3], 'mDataProp' : null, 'sWidth' : '2em', 'sClass' : 'delete-button centered'}			
			]		
		};
	
		var squashConf = {
			
			dataKeys : {
				entityId : 'id',
				entityIndex :'position'
			},
			
			enableHover : true,
			
			confirmPopup : {
				oklabel : settings.oklabel,
				cancellabel : settings.cancellabel
			},
			
			deleteButtons : {
				url : settings.deleteUrl+"/{id}",
				popupmessage : settings.deleteMessage,
				tooltip : settings.deleteTooltip,
				success : function(){
					table.refresh();
				}
			},
			
			enableDnD : true
		
		};
		
		$(settings.selector).squashTable(tableConf, squashConf);
		
		var table=$(settings.selector).squashTable();
	
		return table;
		
	};

});