package com.cathay.coindesk;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import com.cathay.coindesk.model.BPI;
import com.cathay.coindesk.model.Coin;
import com.cathay.coindesk.model.CoinRequest;
import com.cathay.coindesk.model.CoinResponse;
import com.cathay.coindesk.repository.BPIRepository;
import com.cathay.coindesk.repository.CoinRepository;
import com.cathay.coindesk.service.CoindeskService;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
class CathayApplicationTests {

	@Autowired
	private CoindeskService coindeskService;

	@Autowired
	CoinRepository coinRepository;

	@Autowired
	BPIRepository bpiRepository;

	boolean hasDataFlag = true; // DB has Data = true

	@BeforeEach
    void setup(){ // 存DB
		Coin coin = new Coin("Bitcoin", "比特幣", "The data was produced from the CoinDesk Bitcoin Price Index (USD). 比特幣");
		coin.setTime(new Date());
		coinRepository.save(coin);
		
		BPI element1 = new BPI("Bitcoin", "GBP", "&pound;", "14,150.1522", "British Pound Sterling", 14150.1522);
		BPI element2 = new BPI("Bitcoin", "EUR", "&euro;", "16,496.4650", "Euro", 16496.465);
		bpiRepository.save(element1);
		bpiRepository.save(element2);
    }
	
	
	/***
	 * 查詢幣別對應表資料API，並顯示其內容
	 */
	@Test
	void serviceDoCoinQueryTest() {
		CoinRequest req = new CoinRequest();
		req.setChartName("Bitcoin");
		CoinResponse actualRes = coindeskService.doCoinQuery(req);
		if(hasDataFlag) {
			Assert.assertEquals("Bitcoin", actualRes.getChartName());
		} else {
			Assert.assertEquals("not found!", actualRes.getChartName());
		}
	}

	/***
	 * 新增幣別對應表資料API
	 */
	@Test
	void serviceCreateCoinTest() {
		CoinRequest req = new CoinRequest();
		req.setDisclaimer(
				"This data was produced from the CoinDesk Bitcoin Price Index (USD). Non-USD currency data converted using hourly conversion rate from openexchangerates.org");
		req.setChartName("Bitcoin");
		req.setMandarinName("比特幣");

		Map<String, BPI> bpiMap = new HashMap<String, BPI>();
		BPI element1 = new BPI("Bitcoin", "USD", "&#36;", "16,934.2840", "United States Dollar", 16934.284);
		BPI element2 = new BPI("Bitcoin", "GBP", "&pound;", "14,150.1522", "British Pound Sterling", 14150.1522);
		BPI element3 = new BPI("Bitcoin", "EUR", "&euro;", "16,496.4650", "Euro", 16496.465);
		bpiMap.put("USD", element1);
		bpiMap.put("GBP", element2);
		bpiMap.put("EUR", element3);
		req.setBpi(bpiMap);

		String actualRes = coindeskService.createCoin(req);
		Assert.assertEquals("Created Coin!", actualRes);
	}

	/***
	 * 更新幣別對應表資料API，並顯示其內容
	 */
	@Test
	void serviceUpdateCoinTest() {
		CoinRequest req = new CoinRequest();
		req.setDisclaimer("Non-USD currency data converted using hourly conversion rate from openexchangerates.org");
		req.setChartName("Bitcoin");
		req.setMandarinName("特比幣");

		Map<String, BPI> bpiMap = new HashMap<String, BPI>();
		BPI element1 = new BPI("Bitcoin", "EUR", "&euro;", "16,934.2840", "Euroooooooooo", 16934.284);
		BPI element2 = new BPI("Bitcoin", "GBP", "&pound;", "14,150.1522", "British Pound Sterling", 14150.1522);
		bpiMap.put("EUR", element1);
		bpiMap.put("GBP", element2);
		req.setBpi(bpiMap);

		CoinResponse actualRes = coindeskService.updateCoin(req);
		
		CoinResponse expectedRes = new CoinResponse();
		expectedRes.setDisclaimer("Non-USD currency data converted using hourly conversion rate from openexchangerates.org");
		expectedRes.setChartName("Bitcoin");
		expectedRes.setMandarinName("特比幣");
		expectedRes.setBpi(bpiMap);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		String dateString = formatter.format(new Date());
		expectedRes.setTime(dateString);
		
		Assert.assertEquals(expectedRes, actualRes);
	}

	/***
	 * 刪除幣別對應表資料API
	 */
	@ExtendWith(MockitoExtension.class)
	void serviceDeleteCoinTest(@RequestBody CoinRequest coinRequest) {
		CoinRequest req = new CoinRequest();
		req.setChartName("Bitcoin");
		String actualRes = coindeskService.deleteCoin(req);
		
		if(hasDataFlag) {
			Assert.assertEquals("Deleted Coin", actualRes);
		} else {
			Assert.assertEquals("Exception!", actualRes);
		}
	}

	/***
	 * 呼叫 coindesk API，並顯示其內容
	 */
	@Test
	void serviceCallCoindeskApiTest() {
		
		String actualRes = coindeskService.callCoindeskAPI();
		
		String url = "https://api.coindesk.com/v1/bpi/currentprice.json";
		RestTemplate restTemplate = new RestTemplate();
		String expectedRes = restTemplate.getForObject(url, String.class);
		
		Assert.assertEquals(expectedRes, actualRes);
	}

	/***
	 * 資料轉換的API，並顯示其內容
	 */
	@Test
	void serviceConvertDataTest() {
		CoinResponse actualRes = coindeskService.convertData();
		
		if(hasDataFlag) {
			Assert.assertEquals("Bitcoin", actualRes.getChartName());
		} else {
			Assert.assertEquals("not found!", actualRes.getChartName());
		}
	}
}
