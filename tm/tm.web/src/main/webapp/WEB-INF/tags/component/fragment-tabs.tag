<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2012 Henix, henix.fr

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
<%@ tag description="activation of jquery-ui tabs"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="beforeLoad" required="false" description="if set, will add a beforeLoad hook to the configuration of the tabs." %>

<script type="text/javascript" src="<c:url value='/scripts/squash/squashtm.fragmenttabs.js' />"></script>
<script type="text/javascript">
	$(function() {
		require(["jquery.squash.fragmenttabs"], function(Frag){
			var init = {};
			
			<c:if test="${not empty beforeLoad}">
			init.beforeLoad = ${beforeLoad};
			</c:if>
			
			Frag.init(init);
		});
	});
</script>
