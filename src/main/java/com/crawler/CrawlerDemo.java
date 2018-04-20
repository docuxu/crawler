package com.crawler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawlerDemo {

	public String responseHtmlString(String username, String password) {
		// 登陆 Url
		String loginUrl = "https://account.chsi.com.cn/passport/login?entrytype=yzgr&service=http://yz.chsi.com.cn/j_spring_cas_security_check";
		// 需登陆后访问的 Url
		String dataUrl = "https://yz.chsi.com.cn/bsbm/stu/bsViewBmxx.do?bmh=1006099899";
		HttpClient httpClient = new HttpClient();
		String strlt = "";
		String loginCookie = "";
		String returnString = "";
		try {
			Connection con = Jsoup.connect(loginUrl);
			Response rs = con.execute();
			Document doc = Jsoup.parse(rs.body());
			Map<String, String> map = rs.cookies();
			System.out.println(map.size());
			Set<Entry<String, String>> set = map.entrySet();
			Iterator<Entry<String, String>> it = set.iterator();
			if (it.hasNext()) {
				Map.Entry<String, String> entry = (Entry<String, String>) it.next();
				System.out.println("key:" + entry.getKey() + " value:" + entry.getValue());
				loginCookie = entry.getKey() + "=" + entry.getValue();
				System.out.println("loginCookie:" + loginCookie);
			}
			// System.out.println(doc);
			Elements element = doc.select("input[name=lt]");
			for (Element ele : element) {
				String JdbookID = ele.attr("value");
				strlt = JdbookID;
			}
			System.out.println("lt:" + strlt);
		} catch (IOException e) {
			System.out.println("页面获取异常！");
			e.printStackTrace();
		}
		// 设置登陆时要求的信息，用户名和密码
		NameValuePair[] data = { new NameValuePair("username", username), new NameValuePair("password", password),
				new NameValuePair("lt", strlt), new NameValuePair("_eventId", "submit"),
				new NameValuePair("submit", "登陆"), new NameValuePair("lt", strlt),
				new NameValuePair("_eventId", "submit") };
		// 模拟登陆，按实际服务器端要求选用 Post 或 Get 请求方式
		PostMethod postMethod = new PostMethod(loginUrl);
		postMethod.setRequestHeader("cookie", loginCookie);
		postMethod.setRequestHeader("Referer", loginUrl);
		postMethod.setRequestHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
		postMethod.setRequestBody(data);
		try {
			int statusCode = httpClient.executeMethod(postMethod);
			System.out.println(statusCode);
			// 获得登陆后的 Cookie
			Cookie[] cookies = httpClient.getState().getCookies();
			StringBuffer tmpcookies = new StringBuffer();
			for (Cookie c : cookies) {
				if (cookies.length == 1) {
					tmpcookies.append(c.toString());
				} else {
					tmpcookies.append(c.toString() + "; ");
				}
				// System.out.println("tmpcookies = " + c.toString());
			}
			if (statusCode == 302) {// 重定向到新的URL
				Header locationHeader = postMethod.getResponseHeader("location");
				String location = "";
				if (locationHeader != null) {
					location = locationHeader.getValue();
					// System.out.println(location);
					try {
						GetMethod getMethod = new GetMethod(location);
						getMethod.setRequestHeader("cookie", tmpcookies.toString());
						// 每次访问需授权的网址时需带上前面的 cookie 作为通行证
						getMethod.setRequestHeader("User-Agent",
								"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
						httpClient.executeMethod(getMethod);
						Cookie[] cookies1 = httpClient.getState().getCookies();
						StringBuffer tmpcookies1 = new StringBuffer();
						for (Cookie c : cookies1) {
							if (cookies1.length == 1) {
								tmpcookies1.append(c.toString());
							} else {
								tmpcookies1.append(c.toString() + "; ");
							}
							// System.out.println("tmpcookies1 = " +
							// c.toString());
						}
						GetMethod getMethodIndex = new GetMethod("https://yz.chsi.com.cn/bsbm/index.do");
						getMethodIndex.setRequestHeader("cookie", tmpcookies1.toString());
						// 每次访问需授权的网址时需带上前面的 cookie 作为通行证
						getMethodIndex.setRequestHeader("User-Agent",
								"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
						httpClient.executeMethod(getMethodIndex);
						Cookie[] cookies2 = httpClient.getState().getCookies();
						StringBuffer tmpcookies2 = new StringBuffer();
						for (Cookie c : cookies2) {
							if (cookies2.length == 1) {
								tmpcookies2.append(c.toString());
							} else {
								tmpcookies2.append(c.toString() + "; ");
							}
							// System.out.println("tmpcookies2 = " +
							// c.toString());
						}

						GetMethod getMethodMain = new GetMethod(dataUrl);
						getMethodMain.setRequestHeader("cookie", tmpcookies2.toString());
						getMethodMain.setRequestHeader("Referer", "https://yz.chsi.com.cn/bsbm/index.do");
						getMethodMain.setRequestHeader("User-Agent",
								"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
						httpClient.executeMethod(getMethodMain);
						returnString = getMethodMain.getResponseBodyAsString();
					} catch (Exception e) {
						System.out.println("目标页面请求Cookie无效");
					}
				}
			} else {
				System.out.println("票据中心请求Cookie无效");
			}
		} catch (Exception e) {
			System.out.println("登录失败");
		}
		return returnString;
	}

	public InputStream responseHtmlStream(String username, String password) {
		// 登陆 Url
		String loginUrl = "https://account.chsi.com.cn/passport/login?entrytype=yzgr&service=http://yz.chsi.com.cn/j_spring_cas_security_check";
		// 需登陆后访问的 Url
		String dataUrl = "https://yz.chsi.com.cn/bsbm/stu/bsViewBmxx.do?bmh=1006099899";
		HttpClient httpClient = new HttpClient();
		String strlt = "";
		String loginCookie = "";
		InputStream returnStream = null;
		try {
			Connection con = Jsoup.connect(loginUrl);
			Response rs = con.execute();
			Document doc = Jsoup.parse(rs.body());
			Map<String, String> map = rs.cookies();
			System.out.println(map.size());
			Set<Entry<String, String>> set = map.entrySet();
			Iterator<Entry<String, String>> it = set.iterator();
			if (it.hasNext()) {
				Map.Entry<String, String> entry = (Entry<String, String>) it.next();
				System.out.println("key:" + entry.getKey() + " value:" + entry.getValue());
				loginCookie = entry.getKey() + "=" + entry.getValue();
				System.out.println("loginCookie:" + loginCookie);
			}
			// System.out.println(doc);
			Elements element = doc.select("input[name=lt]");
			for (Element ele : element) {
				String JdbookID = ele.attr("value");
				strlt = JdbookID;
			}
			System.out.println("lt:" + strlt);
		} catch (IOException e) {
			System.out.println("页面获取异常！");
			e.printStackTrace();
		}
		// 设置登陆时要求的信息，用户名和密码
		NameValuePair[] data = { new NameValuePair("username", username), new NameValuePair("password", password),
				new NameValuePair("lt", strlt), new NameValuePair("_eventId", "submit"),
				new NameValuePair("submit", "登陆"), new NameValuePair("lt", strlt),
				new NameValuePair("_eventId", "submit") };
		// 模拟登陆，按实际服务器端要求选用 Post 或 Get 请求方式
		PostMethod postMethod = new PostMethod(loginUrl);
		postMethod.setRequestHeader("cookie", loginCookie);
		postMethod.setRequestHeader("Referer", loginUrl);
		postMethod.setRequestHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
		postMethod.setRequestBody(data);
		try {
			int statusCode = httpClient.executeMethod(postMethod);
			System.out.println(statusCode);
			// 获得登陆后的 Cookie
			Cookie[] cookies = httpClient.getState().getCookies();
			StringBuffer tmpcookies = new StringBuffer();
			for (Cookie c : cookies) {
				if (cookies.length == 1) {
					tmpcookies.append(c.toString());
				} else {
					tmpcookies.append(c.toString() + "; ");
				}
				// System.out.println("tmpcookies = " + c.toString());
			}
			if (statusCode == 302) {// 重定向到新的URL
				Header locationHeader = postMethod.getResponseHeader("location");
				String location = "";
				if (locationHeader != null) {
					location = locationHeader.getValue();
					// System.out.println(location);
					try {
						GetMethod getMethod = new GetMethod(location);
						getMethod.setRequestHeader("cookie", tmpcookies.toString());
						// 每次访问需授权的网址时需带上前面的 cookie 作为通行证
						getMethod.setRequestHeader("User-Agent",
								"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
						httpClient.executeMethod(getMethod);
						Cookie[] cookies1 = httpClient.getState().getCookies();
						StringBuffer tmpcookies1 = new StringBuffer();
						for (Cookie c : cookies1) {
							if (cookies1.length == 1) {
								tmpcookies1.append(c.toString());
							} else {
								tmpcookies1.append(c.toString() + "; ");
							}
							// System.out.println("tmpcookies1 = " +
							// c.toString());
						}
						GetMethod getMethodIndex = new GetMethod("https://yz.chsi.com.cn/bsbm/index.do");
						getMethodIndex.setRequestHeader("cookie", tmpcookies1.toString());
						// 每次访问需授权的网址时需带上前面的 cookie 作为通行证
						getMethodIndex.setRequestHeader("User-Agent",
								"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
						httpClient.executeMethod(getMethodIndex);
						Cookie[] cookies2 = httpClient.getState().getCookies();
						StringBuffer tmpcookies2 = new StringBuffer();
						for (Cookie c : cookies2) {
							if (cookies2.length == 1) {
								tmpcookies2.append(c.toString());
							} else {
								tmpcookies2.append(c.toString() + "; ");
							}
							// System.out.println("tmpcookies2 = " +
							// c.toString());
						}

						GetMethod getMethodMain = new GetMethod(dataUrl);
						getMethodMain.setRequestHeader("cookie", tmpcookies2.toString());
						getMethodMain.setRequestHeader("Referer", "https://yz.chsi.com.cn/bsbm/index.do");
						getMethodMain.setRequestHeader("User-Agent",
								"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
						httpClient.executeMethod(getMethodMain);
						returnStream = getMethodMain.getResponseBodyAsStream();
					} catch (Exception e) {
						System.out.println("目标页面请求Cookie无效");
					}
				}
			} else {
				System.out.println("票据中心请求Cookie无效");
			}
		} catch (Exception e) {
			System.out.println("登录失败");
		}
		return returnStream;
	}
}
