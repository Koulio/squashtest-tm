<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU Lesser General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Lesser General Public License for more details.

        You should have received a copy of the GNU Lesser General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ attribute name="cssClass" description="additional css classes" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<div id="generic-error-notification-area" class="ui-state-error ui-corner-all ${ cssClass } not-displayed ">
	<span class="ui-icon ui-icon-alert icon"></span><span><f:message key="error.generic.label" />&nbsp;(<a href="#" id="show-generic-error-details"><f:message key="error.generic.button.details.label" /></a>)</span>
</div>

<div id="generic-information-notification-area" class="ui-state-highlight ui-corner-all ${ cssClass } not-displayed ">
	<span class="ui-icon ui-icon-info icon"></span><span id="generic-information-notification-span"></span>
</div>
