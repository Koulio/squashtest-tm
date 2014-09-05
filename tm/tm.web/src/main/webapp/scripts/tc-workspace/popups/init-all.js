/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
define(['./add-folder-popup', './add-test-case-popup' , 
        './rename-node-popup', './delete-node-popup', 
        './import-excel-popup', './import-links-popup',
        './export-popup'], 
		function(folderpopup, tcpopup, renamepopup, deletepopup, importpopup, linkspopup, exportpopup){
	
	
	function init(){
		
		folderpopup.init();
		tcpopup.init();
		renamepopup.init();
		
		importpopup.init();
		linkspopup.init();
		exportpopup.init();
		
		deletepopup.init();
		
	}
	
	return {
		init : init
	};

});