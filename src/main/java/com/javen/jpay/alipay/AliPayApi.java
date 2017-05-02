package com.javen.jpay.alipay;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayDataDataserviceBillDownloadurlQueryRequest;
import com.alipay.api.request.AlipayFundTransOrderQueryRequest;
import com.alipay.api.request.AlipayFundTransToaccountTransferRequest;
import com.alipay.api.request.AlipayTradeCancelRequest;
import com.alipay.api.request.AlipayTradeOrderSettleRequest;
import com.alipay.api.request.AlipayTradePayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayDataDataserviceBillDownloadurlQueryResponse;
import com.alipay.api.response.AlipayFundTransOrderQueryResponse;
import com.alipay.api.response.AlipayFundTransToaccountTransferResponse;
import com.alipay.api.response.AlipayTradeCancelResponse;
import com.alipay.api.response.AlipayTradeOrderSettleResponse;
import com.alipay.api.response.AlipayTradePayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.log.Log;

public class AliPayApi {
	private static Log log = Log.getLog(AliPayApi.class);

	private static final Prop prop = PropKit.use("alipay.properties");

	static AlipayClient alipayClient;
	static String charset = "UTF-8";
	static String privateKey = prop.get("wap_privateKey");
	static String alipayPulicKey = prop.get("wap_alipayPulicKey");
	static String serverUrl = prop.get("wap_serverUrl");
	static String appId = prop.get("wap_appId");
	static String format = "json";

	static {
		alipayClient = new DefaultAlipayClient(serverUrl, appId, privateKey, format, charset, alipayPulicKey, "RSA2");
	}

	/**
	 * Wap支付
	 * https://doc.open.alipay.com/docs/doc.htm?spm=a219a.7629140.0.0.dfHHR3&
	 * treeId=203&articleId=105285&docType=1
	 * 
	 * @throws AlipayApiException
	 * @throws IOException
	 */
	public static void wapPay(HttpServletResponse response,String bizContent,String returnUrl,String notifyUrl) throws AlipayApiException, IOException {
		AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();// 创建API对应的request
		alipayRequest.setReturnUrl(returnUrl);
		alipayRequest.setNotifyUrl(notifyUrl);// 在公共参数中设置回跳和通知地址
		alipayRequest.setBizContent(bizContent);// 填充业务参数
		String form = alipayClient.pageExecute(alipayRequest).getBody(); // 调用SDK生成表单
		HttpServletResponse httpResponse = response;
		httpResponse.setContentType("text/html;charset=" + charset);
		httpResponse.getWriter().write(form);// 直接将完整的表单html输出到页面
		httpResponse.getWriter().flush();
	}

	/**
	 * 条形码支付
	 * https://doc.open.alipay.com/docs/doc.htm?spm=a219a.7629140.0.0.Yhpibd&treeId=194&articleId=105170&docType=1#s4
	 * @throws AlipayApiException
	 */
	public static String tradePay(String bizContent) throws AlipayApiException {
		AlipayTradePayRequest request = new AlipayTradePayRequest();
		request.setBizContent(bizContent);// 填充业务参数
		AlipayTradePayResponse response = alipayClient.execute(request); // 通过alipayClient调用API，获得对应的response类
		return response.getBody();
	}
	/**
	 * 扫码支付
	 * https://doc.open.alipay.com/docs/doc.htm?spm=a219a.7629140.0.0.i0UVZn&treeId=193&articleId=105170&docType=1#s4
	 * @return
	 * @throws AlipayApiException 
	 */
	public static String tradePrecreatePay(String bizContent) throws AlipayApiException{
		AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
		request.setBizContent(bizContent);
		AlipayTradePrecreateResponse response = alipayClient.execute(request);
		return response.getBody();
	}
	/**
	 * 退款
	 * https://doc.open.alipay.com/docs/api.htm?spm=a219a.7395905.0.0.SAyEeI&docType=4&apiId=759
	 * @param content
	 * @return
	 * @throws AlipayApiException
	 */
	public static String tradeRefund(String bizContent) throws AlipayApiException{
		AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
		request.setBizContent(bizContent);
		AlipayTradeRefundResponse response = alipayClient.execute(request);
		return response.getBody();
	}
	/**
	 * 对账
	 * @param bizContent
	 * @return
	 * @throws AlipayApiException
	 */
	public static String billDownloadurlQuery(String bizContent) throws AlipayApiException{
		AlipayDataDataserviceBillDownloadurlQueryRequest request = new AlipayDataDataserviceBillDownloadurlQueryRequest();
		request.setBizContent(bizContent);
		AlipayDataDataserviceBillDownloadurlQueryResponse response = alipayClient.execute(request);
		return response.getBody();
	}
	
	/**
	 * 交易撤销接口
	 * https://doc.open.alipay.com/docs/api.htm?spm=a219a.7395905.0.0.XInh6e&docType=4&apiId=866
	 * @param bizContent
	 * @return
	 * @throws AlipayApiException
	 */
	public static boolean isTradeCancel(String bizContent) throws AlipayApiException{
		AlipayTradeCancelResponse response = tradeCancel(bizContent);
		if(response.isSuccess()){
			return true;
		}
		return false;
	}
	
	public static AlipayTradeCancelResponse tradeCancel(String bizContent) throws AlipayApiException{
		AlipayTradeCancelRequest request = new AlipayTradeCancelRequest();
		request.setBizContent(bizContent);
		AlipayTradeCancelResponse response = alipayClient.execute(request);
		return response;
	}
	
	/**
	 * 交易查询接口
	 * https://doc.open.alipay.com/docs/api.htm?spm=a219a.7395905.0.0.8H2JzG&docType=4&apiId=757
	 * @param bizContent
	 * @return
	 * @throws AlipayApiException
	 */
	public static boolean isTradeQuery(String bizContent) throws AlipayApiException{
		AlipayTradeQueryResponse response = tradeQuery(bizContent);
		if(response.isSuccess()){
			return true;
		}
		return false;
	}
	
	public static AlipayTradeQueryResponse  tradeQuery(String bizContent) throws AlipayApiException{
		AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
		request.setBizContent(bizContent);
		return alipayClient.execute(request);
	}
	
	/**
	 * 交易结算接口
	 * https://doc.open.alipay.com/docs/api.htm?spm=a219a.7395905.0.0.nl0RS3&docType=4&apiId=1147
	 * @param bizContent
	 * @return
	 * @throws AlipayApiException
	 */
	public static boolean isTradeOrderSettle(String bizContent) throws AlipayApiException{
		AlipayTradeOrderSettleResponse  response  = tradeOrderSettle(bizContent);
		if(response.isSuccess()){
			return true;
		}
		return false;
	}
	
	public static AlipayTradeOrderSettleResponse tradeOrderSettle(String bizContent) throws AlipayApiException{
		AlipayTradeOrderSettleRequest request = new AlipayTradeOrderSettleRequest();
		request.setBizContent(bizContent);
		return alipayClient.execute(request);
	}
	
	
	/**
	 * 单笔转账到支付宝账户
	 * https://doc.open.alipay.com/docs/doc.htm?spm=a219a.7629140.0.0.54Ty29&treeId=193&articleId=106236&docType=1
	 * @param content
	 * @return
	 * @throws AlipayApiException
	 */
	public static boolean transfer(String bizContent) throws AlipayApiException{
			AlipayFundTransToaccountTransferRequest request = new AlipayFundTransToaccountTransferRequest();
			request.setBizContent(bizContent);
			AlipayFundTransToaccountTransferResponse response = alipayClient.execute(request);
			String result = response.getBody();
			log.info("transfer result>"+result);
			System.out.println("transfer result>"+result);
			if (response.isSuccess()) {
				return true;
			} else {
				//调用查询接口查询数据
				JSONObject jsonObject = JSONObject.parseObject(result);
				String out_biz_no = jsonObject.getJSONObject("alipay_fund_trans_toaccount_transfer_response").getString("out_biz_no");
				BizContent tqBizContent = new BizContent();
				tqBizContent.setOut_biz_no(out_biz_no);
				boolean isSuccess = transferQuery(JsonKit.toJson(tqBizContent));
				if (isSuccess) {
					return true;
				}
			}
		return false;
	}
	
	/**
	 * 转账查询接口
	 * @param content
	 * @return
	 * @throws AlipayApiException
	 */
	public static boolean transferQuery(String bizContent) throws AlipayApiException{
		AlipayFundTransOrderQueryRequest request = new AlipayFundTransOrderQueryRequest();
		request.setBizContent(bizContent);
		AlipayFundTransOrderQueryResponse response = alipayClient.execute(request);
		log.info("transferQuery result>"+response.getBody());
		System.out.println("transferQuery result>"+response.getBody());
		if(response.isSuccess()){
			return true;
		}
		return false;
	}
}