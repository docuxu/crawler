package com.crawler;
import java.io.InputStream;
public class TestDemo{
   public static void main(String[] args) {
	CrawlerDemo crawlerDemo = new CrawlerDemo();
	String returnString=crawlerDemo.responseHtmlString("18012927776", "123456qwe");
	System.out.println(returnString);
	//InputStream inputStream=crawlerDemo.responseHtmlStream("18012927776", "123456qwe");
}
}
