package com.accountbook.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.accountbook.modle.result.Result;
import com.accountbook.service.impl.TokenServiceImpl;

/**
 * token检查
 * 
 * @author xinjun
 *
 */
public class TokenFilter implements Filter {

	private TokenServiceImpl tokenService;

	/**
	 * 这个list中配置不需要token的Controller
	 */
	@SuppressWarnings("serial")
	public static List<String> C = new ArrayList<String>() {
		{
			add("");
			add("images");
			add("tb.html");
			add("help");
			add("testing");

		}
	};
	/**
	 * 这个list中配置不需要token的Controller中的method
	 */
	@SuppressWarnings("serial")
	public static List<String> M = new ArrayList<String>() {
		{
			add("fromWX");
		}
	};

	/**
	 * 这个list中配置不需要token的Controller/method
	 */
	@SuppressWarnings("serial")
	public static List<String> CM = new ArrayList<String>() {
		{
			add("group/qr");
			add("image/get");
			add("image/delete");
		}
	};

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String token = request.getParameter("token");
		String uri = ((HttpServletRequest) request).getRequestURI();

		 System.out.println("TokenFilter.uri:"+uri);
		 System.out.println("TokenFilter.token:"+token);
		if (token != null && !"".equals(token)) {
			if (tokenService == null)
				tokenService = new TokenServiceImpl();
			Result tokenValidResult = tokenService.validate(token);

			if ((int) tokenValidResult.get("status") == Result.RESULT_TOKEN_INVALID) {
				response.getOutputStream().write("{\"status\":1,\"msg\":\"token无效\"}".getBytes("utf-8"));
				// System.out.println("TokenFilter.error:"+token);
				return;
			}
			String findId = tokenValidResult.get("msg").toString();
			request.setAttribute("userid", findId);
			chain.doFilter(request, response);
		} else {
			// 如果有些接口不需要token,保证这些接口的正常访问
			String[] split = uri.split("/");
			String controllerName = split.length >= 3 ? split[2] : "";
			String methodName = split.length >= 4 ? split[3] : "";


			// 方便测试
			if (request.getParameter("TEST") != null) {
				request.setAttribute("userid", "oCBrx0FreB-L8pIQM5_RYDGoWOKQ");
				chain.doFilter(request, response);
				return;
			}

			System.out.println("TokenFilter.controllerName:" + controllerName);
			System.out.println("TokenFilter.methodName:" + methodName);

			if (C.contains(controllerName)) {
				chain.doFilter(request, response);
				return;
			}
			if (M.contains(methodName)) {
				chain.doFilter(request, response);
				return;
			}
			if (CM.contains(controllerName + "/" + methodName)) {
				chain.doFilter(request, response);
				return;
			}
			response.getOutputStream().write("{\"status\":1,\"msg\":\"token无效\"}".getBytes("utf-8"));
			// System.out.println("TokenFilter.nullToken");
		}

	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {

	}

}
