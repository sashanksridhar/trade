package com.citi.trade.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.citi.trade.TradeApplication;
import com.citi.trade.model.Trade;
import com.citi.trade.model.TradeType;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

@Controller
@CrossOrigin
public class TradeController {
	public static MongoClientURI uri = new MongoClientURI(
		    "mongodb+srv://mongoUser:cR5p1eKma8qWgIhp@cluster0.cddgx.mongodb.net/Task5?retryWrites=true&w=majority");
	
	MongoDatabase database = TradeApplication.database;
	@RequestMapping(value = "/trade/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String createTrade(Trade trade, @RequestParam Map<String, String> request, Model model) throws MalformedURLException {
		Stock stock = null;
		String email = request.get("email");

		String type = request.get("tradetype");
		System.out.println(type);
		if(type.equals("BUY")) {
			System.out.println(type);
			trade.setType(TradeType.BUY);
		}
		else if (type.equals("SELL")) {
			System.out.println(type);
			trade.setType(TradeType.SELL);
		} 
		System.out.println(trade.getType());
		try {
			stock = YahooFinance.get(trade.getTicker());
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(stock == null) {
			
			model.addAttribute("message","Please Enter Correct Ticker");
		    return "redirecthome";
		}
		else {
			URL url = new URL("https://financialmodelingprep.com/api/v3/quote/"+trade.getTicker()+"?apikey=a9d39eebca61a0cd592cdf037ef01b4e");

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
	    	
	    	trade.setPrice(object.getBigDecimal("price").doubleValue());
//			MongoClient myMongo = new MongoClient(uri);
//			MongoDatabase database = myMongo.getDatabase("Task5");
			
			MongoCollection<Document> usercollection = database.getCollection("users");
			double amoun = usercollection.find(Filters.eq("email",email)).first().getDouble("amount");
			if(type.equals("BUY")) {
				amoun -= trade.getPrice()*trade.getQuantity(); 
			}
			else if (type.equals("SELL")) {
				amoun += trade.getPrice()*trade.getQuantity(); 
			} 
			
			if(amoun< 0) {
				model.addAttribute("message","Trade failed. Insufficient balance") ;
				return "createtrade";
				
			}
			else {
			Document docu = new Document("amount",amoun);		
			usercollection.updateOne(Filters.eq("email",email), new Document("$set", docu));
			
			Document doc = new Document("created", trade.getCreated()).append("type", trade.getType().toString()).append("state",trade.getState().toString()).append("ticker", trade.getTicker()).append("quantity",trade.getQuantity()).append("price", trade.getPrice());
			MongoCollection<Document> mycollection = database.getCollection("trade");
			mycollection.insertOne(doc);
			ObjectId objectId = doc.getObjectId("_id");
			Document document = new Document("id",objectId).append("email", email);
			mycollection = database.getCollection("user_portfolio");
			mycollection.insertOne(document);
//			myMongo.close(); 
			model.addAttribute("message","Trade has been created successfully. Trade ID is "+doc.getObjectId("_id").toString()+". Balance is "+Double.toString(amoun));
			return "createtrade";
			}
		}
	}
	@RequestMapping(value = "/trade/view", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
//	@RequestMapping(value = "/trade/view", method = RequestMethod.GET)

	public String viewTrade(@RequestParam Map<String,String> request,Model model) {


		String email = request.get("email");

//		MongoClient myMongo = new MongoClient(uri);
//		MongoDatabase database = myMongo.getDatabase("Task5");
		MongoCollection<Document> usercollection = database.getCollection("user_portfolio");
		
		
		FindIterable<Document> iterDoc = usercollection.find(Filters.eq("email",email));
		Iterator<Document> it = iterDoc.iterator();
		String s = "";
		MongoCollection<Document> user = database.getCollection("users");
		double amoun = user.find(Filters.eq("email",email)).first().getDouble("amount");
		s+="Balance Remaining is "+Double.toString(amoun)+"<br>";
		
		while (it.hasNext()) {
			Document myDocument = it.next();
			ObjectId id = myDocument.getObjectId("id");
			MongoCollection<Document> tradeCollection = database.getCollection("trade");
			Document myDoc = tradeCollection.find(Filters.eq("_id",id)).first();
			s+=myDoc.toString()+"<br>";
			
		}
		
//		myMongo.close();
		
			model.addAttribute("message",s);
		    return "viewtrades";
		
	}
	@RequestMapping(value = "/trade/delete", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String deleteTrade(@RequestParam Map<String,String> request,Model model) {
		
		String id = request.get("id");
		ObjectId objId = new ObjectId(id);
//		MongoClient myMongo = new MongoClient(uri);
//		MongoDatabase database = myMongo.getDatabase("Task5");
		MongoCollection<Document> tradecollection = database.getCollection("trade");
		tradecollection.deleteOne(Filters.eq("_id",objId));
		System.out.println(objId.toString());
		MongoCollection<Document> usercollection = database.getCollection("user_portfolio");
		usercollection.deleteOne(Filters.eq("id",objId));
		model.addAttribute("message","Trade Deleted Successfully");
//		myMongo.close();
		
		return "deletetrades";
	}
}
