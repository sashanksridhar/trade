package com.citi.trade.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.citi.trade.model.Trade;
import com.citi.trade.model.TradeType;
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
public class AdminController {
	public static MongoClientURI uri = new MongoClientURI(
		    "mongodb+srv://mongoUser:cR5p1eKma8qWgIhp@cluster0.cddgx.mongodb.net/Task5?retryWrites=true&w=majority");
	@RequestMapping(value = "/admin/login", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String login(@RequestParam Map<String,String> request,Model model) {
//		System.out.println(request);
		String name = request.get("id");
		
//		String nameString = "admin";
		String password = request.get("password");
		

		MongoClient myMongo = new MongoClient(uri);
		MongoDatabase database = myMongo.getDatabase("Task5");
		MongoCollection<Document> mycollection = database.getCollection("admin");
		Document myDoc = mycollection.find(Filters.eq("name",name)).first();
		System.out.println(myDoc);
		String retrievePass = myDoc.getString("password");
//		String name = myDoc.getString("name");
		if(retrievePass.equals(password)) {
//			model.addAttribute("message","Welcome "+name);
		    return "adminview";
		}
		else {
			model.addAttribute("message","Please Enter Correct Details");
		    return "redirecthome";
		}
		
	}
		@RequestMapping(value = "/admin/add/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
		public String createCompanyPortfolio(@RequestParam Map<String, String> request,Model model) {
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
				MongoClient myMongo = new MongoClient(uri);
				MongoDatabase database = myMongo.getDatabase("Task5");
				
				
				
				Document doc = new Document("ticker", ticker).append("name", stock.getName()).append("exchange",stock.getStockExchange()).append("currency", stock.getCurrency()).append("price",stock.getQuote().getPrice()).append("volume", stock.getQuote().getAvgVolume()).append("high", stock.getQuote().getDayHigh()).append("low", stock.getQuote().getDayLow()).append("open", stock.getQuote().getOpen());
				MongoCollection<Document> mycollection = database.getCollection("ticker_portfolio");
				mycollection.insertOne(doc);
				
				myMongo.close(); 
				model.addAttribute("message","Company Portfolio has been created");
				return "addcompany";
			}
		}
		
		
		@RequestMapping(value = "/admin/view", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
//		@RequestMapping(value = "/trade/view", method = RequestMethod.GET)

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
			    return "updatetickersadmin";
			
		}
		
		
		@RequestMapping(value = "/admin/update", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
		public String UpdateCompanyPortfolio(@RequestParam Map<String, String> request,Model model) {
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
				MongoClient myMongo = new MongoClient(uri);
				MongoDatabase database = myMongo.getDatabase("Task5");
				
				
				
				Document doc = new Document("ticker", ticker).append("name", stock.getName()).append("exchange",stock.getStockExchange()).append("currency", stock.getCurrency()).append("price",stock.getQuote().getPrice()).append("volume", stock.getQuote().getAvgVolume()).append("high", stock.getQuote().getDayHigh()).append("low", stock.getQuote().getDayLow()).append("open", stock.getQuote().getOpen());
				MongoCollection<Document> mycollection = database.getCollection("ticker_portfolio");
				mycollection.updateOne(Filters.eq("ticker", ticker), new Document("$set", doc));
				
				
				myMongo.close(); 
				model.addAttribute("message","Company Portfolio has been updated");
				return "addcompany";
			}
		}

	
		@RequestMapping(value = "/admin/main", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
		public String mainMenu(@RequestParam Map<String,String> request,Model model) {

			    return "adminview";
			
		}
}
