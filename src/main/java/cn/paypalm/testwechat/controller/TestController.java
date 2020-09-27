package cn.paypalm.testwechat.controller;

//import ch.qos.logback.core.util.ContextUtil;

import cn.paypalm.testwechat.OkHttpRequest;
import cn.paypalm.testwechat.security.PPSecurity;
import cn.paypalm.testwechat.utils.ContextUtil;
import cn.paypalm.testwechat.utils.HangTianUtil;
import cn.paypalm.testwechat.utils.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author cyw
 * @date 2019/7/31 19:09
 */
@RestController
public class TestController {
    Hashtable<String,String> map = new Hashtable<String,String>();
    @RequestMapping(value = "/banknotify/getcode", method = RequestMethod.GET)
    public String sayHello(@RequestParam("code") String code , @RequestParam("state") String state) throws Exception{
        System.out.println("code："+code);
        String retJSON = "";
        OkHttpRequest okHttpRequest = new OkHttpRequest();
        //生成accessToken的接口URL
        String appid="wx85bb985f2252f884";//公众号的唯一标识
        String secret="2c9e67cae79c58af0edd4f1d403c0aa8";//公众号的appsecret
        String accessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+appid+"&secret="
                +secret+"&code="+code+"&grant_type=authorization_code";
        System.out.println("accessTokenUrl："+accessTokenUrl);
        try {
            //发送请求
            retJSON = okHttpRequest.get(accessTokenUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("getOpenidReturnJSON："+retJSON);
        JSONObject js = JSON.parseObject(retJSON);

        String openid = (String) js.get("openid");
        String accesstoken = (String) js.get("access_token");
        PPSecurity ppSecurity = null;
        try {
            ppSecurity = cn.paypalm.testwechat.utils.MySecurity.getPPSecurity();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("安全初始化失败,请检查配置是否错误");
        }
        Map<String,String> map = new HashMap<String,String>();
        map.put("opcode","MA5001");//opcode
        //map.put("productid", "WX_NATIVE0201");//产品id，生产另行提供
        map.put("productid", "WX_JSAPI0201");//产品id，生产另行提供
        //map.put("productid", "ALI_NATIVE0201");//产品id，生产另行提供
        map.put("merorderno", cn.paypalm.testwechat.utils.HangTianUtil.genMerOrderno());//商户订单号
        map.put("merorderdate", cn.paypalm.testwechat.utils.HangTianUtil.genOrderDate());//订单日期
        map.put("meruserid", "13212341234");//商户的用户id：手机号或者其他唯一即可
        map.put("tranamt", "1");//交易金额 单位分
        map.put("orderdesc","屠龙刀");//订单描述订单商品名称
        map.put("remark", "测试");//备注，可不填
        map.put("paystyle", "13");//
        map.put("notifyurl", "http://127.0.0.1:8080/htjavademo/MerNotifyResultServlet");//异步通知订单状态，为空时不通知
        map.put("subappid",appid);
        map.put("subopenid",openid);
        System.out.println("请求参数map:"+map);
        //将参数转成xml
        String xml = cn.paypalm.testwechat.utils.HangTianUtil.map2Xml(map);
        System.out.println("请求xml:"+xml);
        //整体xml加密异常自行处理
        byte[] encodeDataByte = ppSecurity.PPFCommDataEncode(xml.getBytes("gbk"));
        xml = new String(encodeDataByte,"gbk");
        xml = URLEncoder.encode(xml,"gbk");
        String merid = ContextUtil.getValue("merid");
        String data = "merid="+merid+"&transdata="+xml;
        System.out.println("最终请求数据:"+data);
        String url = ContextUtil.getValue(map.get("opcode"));
        if(url == null){
            throw new Exception("opcode对应的url配置为空");
        }
        //发送请求
        String res = HttpUtil.request(url,data,true);
        //加密响应数据
        byte[] resB = ppSecurity.PPFCommDataDecode(res.getBytes("gbk"));
        res = new String(resB,"gbk");
        System.out.println("响应数据明文:"+res);
        //将xml转成map
        Map<String,Object> resMap = HangTianUtil.xml2Map(res);
        System.out.println("响应码:"+resMap.get("rspcode")+",响应描述:"+resMap.get("rspdesc"));
        if("000000".equals((String)resMap.get("rspcode"))){
            System.out.println("调起微信支付url:"+resMap.get("payurl"));
            System.out.println("调起微信支付数据:"+resMap.get("paydata"));
        }
        return res;
    }
}
