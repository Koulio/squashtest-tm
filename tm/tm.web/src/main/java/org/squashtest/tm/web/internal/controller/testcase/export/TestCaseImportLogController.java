/**
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

package org.squashtest.tm.web.internal.controller.testcase.export;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

/**
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/test-cases/import-logs")
public class TestCaseImportLogController {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseImportLogController.class);

	@Inject
	private TestCaseImportLogHelper logHelper;

	@RequestMapping(value = "{timestamp}", method = RequestMethod.GET)
	public void getExcelImportLog(String timestamp, WebRequest request, HttpServletResponse response) {
		File logFile = logHelper.fetchLogFile(request, timestamp);

		if (logFile.exists()) {
			InputStream is = null;
			try {
				response.setContentType("application/octet-stream");
				response.setHeader("Content-Disposition", "attachment; filename=" + logHelper.logFilename(timestamp)
						+ ".xls");
				is = new BufferedInputStream(new FileInputStream(logFile));
				IOUtils.copy(is, response.getOutputStream());
				response.flushBuffer();

			} catch (IOException e) {
				LOGGER.warn("Could not read test-case import log " + logFile.getPath(), e);
				throw new RuntimeException(e);

			} finally {
				IOUtils.closeQuietly(is);
			}
		} else {
			response.setStatus(HttpStatus.NOT_FOUND.value());
			// TODO should redirect somehow
		}
	}
}