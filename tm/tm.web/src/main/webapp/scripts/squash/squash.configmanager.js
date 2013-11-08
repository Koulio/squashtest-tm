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

define([ "jquery", "squash.translator", "datepicker/require.jquery.squash.datepicker-locales" ], function($, translator, regionale) {

	function stdCkeditor() {
		var lang = translator.get('rich-edit.language.value');

		return {
			customConfig : squashtm.app.contextRoot + '/styles/ckeditor/ckeditor-config.js',
			lang : lang
		};
	}
	
	function stdJeditable(){
		return {
			width : '100%',
			submit : squashtm.message.confirm,
			cancel : squashtm.message.cancel,
			maxlength : 255,
			cols : 80,
			max_size : 20,
			onblur : function() {
			},
			placeholder : squashtm.message.placeholder
			
		};
	}
	
	function stdDatepicker(){
		
		//parameterize the locale
		var localemeta = {
			format : 'squashtm.dateformatShort.js',
			locale : 'squashtm.locale'
		};
		
		var message = translator.get(localemeta);
		
		var language = regionale[message.locale] || regionale;
		
		return $.extend(true, {}, {dateFormat : message.format}, language);	
	}

	return {
		getStdChkeditor : stdCkeditor,
		getStdJeditable : stdJeditable
	};

});