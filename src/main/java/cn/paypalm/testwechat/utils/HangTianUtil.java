package cn.paypalm.testwechat.utils;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public class HangTianUtil {
	private static final XmlMapper xmlMapper = new XmlMapper();
	/**生成商户订单号*/
	public static String genMerOrderno(){
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String dateStr = df.format(new Date());
		return "ht"+dateStr;
	}

	/**生成订单日期*/
	public static String genOrderDate(){
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String dateStr = df.format(new Date());
		return dateStr;
	}

	/**将map转成xml*/
	public static String map2Xml(Map<String,String> paramMap){
		StringBuffer xmlData = new StringBuffer();
		xmlData.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><paypalm>");
		Iterator i$ = paramMap.entrySet().iterator();

		while(i$.hasNext()) {
			Map.Entry entry = (Map.Entry)i$.next();
			xmlData.append("<" + (String)entry.getKey() + ">" + (entry.getValue() == null?"":(String)entry.getValue()) + "</" + (String)entry.getKey() + ">");
		}

		xmlData.append("</paypalm>");
		return xmlData.toString();
	}

	/**xml转map*/
	public static Map<String,Object> xml2Map(String xml) throws IOException {
		return xml == null?null:(Map)xmlMapper.readValue(xml, Map.class);
	}
}
