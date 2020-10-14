package com.citi.trade.controller;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;


import com.citi.trade.model.User;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
@Controller
@CrossOrigin
public class UserController {
	public static MongoClientURI uri = new MongoClientURI(
		    "mongodb+srv://mongoUser:cR5p1eKma8qWgIhp@cluster0.cddgx.mongodb.net/Task5?retryWrites=true&w=majority");
	
	
	@RequestMapping(value="/user/signup", method=RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)

	public String signup(User user) {
		System.out.println("hi");
		MongoClient myMongo = new MongoClient(uri);
		MongoDatabase database = myMongo.getDatabase("Task5");
		
		Document doc = new Document("email", user.getEmail()).append("name", user.getName()).append("password",user.getPassword());
		MongoCollection<Document> mycollection = database.getCollection("users");
		mycollection.insertOne(doc);
		myMongo.close(); 
		return "signupsuccess";
		
	}
	
	@RequestMapping(value = "/user/login", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String login(@RequestParam Map<String,String> request,Model model) {
//		System.out.println(request);
		String email = request.get("email");
		String password = request.get("password");
		

		MongoClient myMongo = new MongoClient(uri);
		MongoDatabase database = myMongo.getDatabase("Task5");
		MongoCollection<Document> mycollection = database.getCollection("users");
		Document myDoc = mycollection.find(Filters.eq("email",email)).first();
		System.out.println(myDoc);
		String retrievePass = myDoc.getString("password");
		String name = myDoc.getString("name");
		if(retrievePass.equals(password)) {
			model.addAttribute("message","Welcome "+name);
		    return "userview";
		}
		else {
			model.addAttribute("message","Please Enter Correct Details");
		    return "redirecthome";
		}
	}
	
	@RequestMapping(value = "/user/main", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String mainMenu(@RequestParam Map<String,String> request,Model model) {

		String email = request.get("email");
		

		MongoClient myMongo = new MongoClient(uri);
		MongoDatabase database = myMongo.getDatabase("Task5");
		MongoCollection<Document> mycollection = database.getCollection("users");
		Document myDoc = mycollection.find(Filters.eq("email",email)).first();
		System.out.println(myDoc);
		String name = myDoc.getString("name");
		
			model.addAttribute("message","Welcome "+name);
		    return "userview";
		
	}
	
	@RequestMapping(value = "/portfolio/view", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
//	@RequestMapping(value = "/trade/view", method = RequestMethod.GET)

	public String viewTrade(@RequestParam Map<String,String> request,Model model) {


		

		MongoClient myMongo = new MongoClient(uri);
		MongoDatabase database = myMongo.getDatabase("Task5");
		MongoCollection<Document> usercollection = database.getCollection("ticker_portfolio");
		
		
		FindIterable<Document> iterDoc = usercollection.find();
		Iterator<Document> it = iterDoc.iterator();
		List<String> strings = new ArrayList<>();
		while (it.hasNext()) {
			Document myDocument = it.next();
			
			strings.add(myDocument.getString("ticker"));
			
		}
		myMongo.close();
		
			model.addAttribute("messages",strings);
		    return "viewtickers";
		
	}
	
	
	@RequestMapping(value = "/portfolio/ticker/view", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String tickerInfo(@RequestParam Map<String,String> request,Model model) {

		String ticker = request.get("ticker");
		

		MongoClient myMongo = new MongoClient(uri);
		MongoDatabase database = myMongo.getDatabase("Task5");
		MongoCollection<Document> mycollection = database.getCollection("ticker_portfolio");
		Document myDoc = mycollection.find(Filters.eq("ticker",ticker)).first();
		System.out.println(myDoc);
//		String name = myDoc.getString("name");
		model.addAttribute("ticker", myDoc.getString("ticker"));
		model.addAttribute("name",myDoc.getString("name"));
		model.addAttribute("exchange",myDoc.getString("exchange"));
		model.addAttribute("currency",myDoc.getString("currency"));
		model.addAttribute("price",((org.bson.types.Decimal128)myDoc.get("price")).bigDecimalValue());
		model.addAttribute("volume",myDoc.getLong("volume"));
		model.addAttribute("high",((org.bson.types.Decimal128)myDoc.get("high")).bigDecimalValue());
		model.addAttribute("low",((org.bson.types.Decimal128)myDoc.get("low")).bigDecimalValue());
		model.addAttribute("open",((org.bson.types.Decimal128)myDoc.get("open")).bigDecimalValue());
			
		    return "tickerdata";
		
	}
	
	
	@RequestMapping(value = "/portfolio/livequote", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String createCompanyPortfolio(@RequestParam Map<String, String> request,Model model) throws MalformedURLException {
		Stock stock = null;
		String ticker = request.get("ticker");

		
		try {
			stock = YahooFinance.get(ticker);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(stock == null) {
			model.addAttribute("message","Please Correct Ticker");
		    return "redirecthome";
		}
		else {
			
			URL url = new URL("https://financialmodelingprep.com/api/v3/quote/"+ticker+"?apikey=a9d39eebca61a0cd592cdf037ef01b4e");

			String lString = "";
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
			   
				for (String line; (line = reader.readLine()) != null;) {
			    lString+=line;	
//			    System.out.println(line);
			    
			  }
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			System.out.println(lString);
	    	JSONArray array = new JSONArray(lString);
	    	JSONObject object = array.getJSONObject(0);  
	    	model.addAttribute("name",object.get("name"));
	    	model.addAttribute("price",object.getBigDecimal("price"));
			
			return "displayliveprice";
		}
	}
	
	
}
