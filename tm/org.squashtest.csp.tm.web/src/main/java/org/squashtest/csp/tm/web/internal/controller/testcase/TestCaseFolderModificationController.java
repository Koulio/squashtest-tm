/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.web.internal.controller.testcase;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.service.FolderModificationService;
import org.squashtest.csp.tm.web.internal.controller.generic.FolderModificationController;

@Controller
@RequestMapping("/test-case-folders/{folderId}")
public class TestCaseFolderModificationController extends FolderModificationController<TestCaseFolder> {
	private FolderModificationService<TestCaseFolder> folderModificationService;

	@Override
	protected FolderModificationService<TestCaseFolder> getFolderModificationService() {
		return folderModificationService;
	}

	@ServiceReference(serviceBeanName = "squashtest.tm.service.TestCaseFolderModificationService")
	public final void setFolderModificationService(FolderModificationService<TestCaseFolder> folderModificationService) {
		this.folderModificationService = folderModificationService;
	}

}
