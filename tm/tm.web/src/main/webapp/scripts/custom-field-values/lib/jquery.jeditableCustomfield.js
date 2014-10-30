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
define(
		[ "jquery", "./cuf-values-utils", "squash.configmanager", "jqueryui",
				"jquery.squash.jeditable", "jeditable.datepicker",
				"datepicker/jquery.squash.datepicker-locales", "jquery.squash.tagit" ],
		function($, utils, confman) {

			/* ***************************************************************************************************
			 * 
			 * The following is a builder of postfunction for a jeditable. Its purpose is to  
			 * 
			 * It accepts three parameters :
			 * - idOrURLOrPostFunction : can be either 
			 *		- the id of a custom field, 
			 *		- an url that will be used as is,
			 *		- a function, that will be used as is,
			 *		- nothing, in which case the value of an attribute data-value-id on the element will be used
			 * 
			 * - postProcess : if defined, postProcess will be invoked upon xhr completion
			 * - isDernomalized : if defined and true, the custom field will be treated as a denormalized cuf.
			 * 
			 * ***************************************************************************************************/
			
			function buildPostFunction(idOrURLOrPostfunction, postProcess, isDenormalized) {

				var postProcessFn = postProcess || function(value) {
					return value;
				};
				
				var baseURL  = squashtm.app.contextRoot;
					baseURL += (isDenormalized) ? "/denormalized-fields/values/" : "/custom-fields/values/";
					
				var ajaxconf = {
					data : {},
					type : 'POST'
				};

				var postFunction;
				
				
				switch(typeof idOrURLOrPostfunction){
				
				// case : the argument is already a post function in its own rights
				case "function" : 
					postFunction = idOrURLOrPostfunction;
					break;
					
				// case : the argument is a url. It will be used as is, along with the usual parameters
				case "string" :
					postFunction = function(value) {
						ajaxconf.url = idOrURLOrPostfunction;
						ajaxconf.data.value = value;
						return $.ajax(ajaxconf);
					};
					break;
					
				// case empty : the element must define an attribute 'data-value-id' and that ID will be used 
				// just like in the default clause.
				case undefined :
					postFunction = function(value) {
						var id = $(this).data('value-id');
						ajaxconf.url = baseURL + id;
						ajaxconf.data.value = value;
						return $.ajax(ajaxconf);
					};		
					break;
					
				// case : the argument is assumed to be a number, specifically the ID. We can then 
				// define at which url we need to post.
				default :
					postFunction = function(value) {
						ajaxconf.url = baseURL + idOrURLOrPostfunction;
						ajaxconf.data.value = value;
						return $.ajax(ajaxconf);
					};			
					break;
				
				}

				return function(value, settings) {
					var data = postProcessFn(value, settings);
					postFunction.call(this, data);
					return value;
				};

			}
			
			/* *********************************************************************
			 * 
			 *		Define the custom fields now
			 * 
			 * *********************************************************************/

			function getBasicConf() {
				return confman.getStdJeditable();
			}

			
			
			function initAsDatePicker(elts, cufDefinition,
					idOrURLOrPostfunction) {

				var conf = getBasicConf();

				var format = cufDefinition.format;
				var locale = cufDefinition.locale;

				conf.type = 'datepicker';
				conf.datepicker = confman.getStdDatepicker();
				
				elts.each(function(idx, el){
					var $el = $(el);
					var raw = $el.text();
					var formatted = utils.convertStrDate($.datepicker.ATOM, format, raw);
					$el.text(formatted);
				});

				var postProcess = function(value, settings) {
					return utils.convertStrDate(format, $.datepicker.ATOM,
							value);
				};

				var postFunction = buildPostFunction(idOrURLOrPostfunction,	postProcess, cufDefinition.denormalized);

				elts.editable(postFunction, conf);

			}

			
			
			function initAsList(elts, cufDefinitions, idOrURLOrPostfunction) {
				if (elts.length === 0){
					return;
				}
				

		
				utils.addEmptyValueToDropdownlistIfOptional(cufDefinitions);

				var prepareSelectData = function(options, selected) {

					var i = 0, length = options.length;
					var result = {};

					var opt;
					for (i = 0; i < length; i++) {
						opt = options[i].label;
						result[opt] = opt;
					}

					result.selected = selected;
					return result;

				};

				elts.each(function() {

					var jqThis = $(this);
					var selected = jqThis.text();

					var conf = getBasicConf();
					conf.type = 'select';
					conf.data = prepareSelectData(
							cufDefinitions.options, selected);

					var postFunction = buildPostFunction(idOrURLOrPostfunction, undefined, cufDefinitions.denormalized);

					jqThis.editable(postFunction, conf);

				});
			}

			
			
			function initAsPlainText(elts, cufDefinition, idOrURLOrPostfunction) {

				var conf = getBasicConf();
				conf.type = 'text';

				var postFunction = buildPostFunction(idOrURLOrPostfunction, undefined, cufDefinition.denormalized);

				elts.editable(postFunction, conf);

			}

			
			
			function initAsCheckbox(elts, cufDefinition, idOrURLOrPostfunction) {

				if (elts.length === 0){
					return;
				}
				
				var postFunction = buildPostFunction(idOrURLOrPostfunction, undefined, cufDefinition.denormalized);

				var clickFn = function() {
					var jqThis = $(this);
					var checked = jqThis.prop('checked');
					postFunction.call(jqThis, checked);
				};

				elts.each(function() {

					var jqThis = $(this);
					var chkbx;

					if (jqThis.is('input[type="checkbox"]')) {
						chkbx = jqThis;
					} else if (jqThis.find('input[type="checkbox"]').length > 0) {
						chkbx = jqThis.find('input[type="checkbox"]');
					} else {
						var checked = (jqThis.text().toLowerCase() === "true") ? true : false;
						jqThis.empty();
						chkbx = $('<input type="checkbox"/>');
						chkbx.prop('checked', checked);
						jqThis.append(chkbx);
					}

					chkbx.enable(true);
					chkbx.click(clickFn);

				});

			}
			
			
			function initAsRichtext(elts, cufDefinition, idOrURLOrPostfunction) {

				if (elts.length === 0){
					return;
				}
				
				var postFunction = buildPostFunction(idOrURLOrPostfunction, undefined, cufDefinition.denormalized);
				
				var conf = confman.getJeditableCkeditor();
				
				elts.editable(postFunction, conf);			
				
			}

			function initAsTag(elts, cufDefinition, idOrURLOrPostfunction){
				
				if (elts.length === 0){
					return;
				}
				
				
			}
			
			
			/* ***************************************************************************
			* 
			*										MAIN 
			* 
			* ***************************************************************************/
			
			$.fn.jeditableCustomfield = function(cufDefinition, idOrURLOrPostfunction) {

				var type = cufDefinition.inputType.enumName;

				if (type === "DATE_PICKER") {
					initAsDatePicker(this, cufDefinition, idOrURLOrPostfunction);
				} else if (type === "DROPDOWN_LIST") {
					initAsList(this, cufDefinition, idOrURLOrPostfunction);
				} else if (type === "PLAIN_TEXT") {
					initAsPlainText(this, cufDefinition, idOrURLOrPostfunction);
				} else if (type === "CHECKBOX"){
					initAsCheckbox(this, cufDefinition, idOrURLOrPostfunction);
				} else if (type === "RICH_TEXT"){
					initAsRichtext(this, cufDefinition, idOrURLOrPostfunction);
				}

			};

		});
