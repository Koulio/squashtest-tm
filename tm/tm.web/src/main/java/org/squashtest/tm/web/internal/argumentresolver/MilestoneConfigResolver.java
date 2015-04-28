package org.squashtest.tm.web.internal.argumentresolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.util.WebUtils;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.milestone.MilestoneFinderService;

/**
 * Use this to know what current milestone instead of @CookieValue. It's check if the milestone in the cookie exist
 * and if the current user can see this milestone.  Return the milestone if ok else return null.
 * @author jsimon
 *
 */
public class MilestoneConfigResolver   implements HandlerMethodArgumentResolver  {

	@Inject
	private MilestoneFinderService milestoneFinderService;
	
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	 public static @interface CurrentMilestone {  

		 }

	private static final String MILESTONE = "milestones";  

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(CurrentMilestone.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        
		    HttpServletRequest servletRequest = (HttpServletRequest) webRequest.getNativeRequest();
            Cookie cookie = WebUtils.getCookie(servletRequest, MILESTONE);
            //it's under 9000 ! just a fake id in case we don't find cookie.
		    String cookieId = cookie != null ? cookie.getValue() : "-9000";
			final Long milestoneId = Long.parseLong(cookieId);
			List<Milestone> visibles = milestoneFinderService.findAllVisibleToCurrentUser();
			Milestone milestone = (Milestone) CollectionUtils.find(visibles, new Predicate() {	
				@Override
				public boolean evaluate(Object milestone) {		
					return ((Milestone)milestone).getId() == milestoneId;
				}
			});		
		   return milestone != null ? milestone : null;  
	}
}
