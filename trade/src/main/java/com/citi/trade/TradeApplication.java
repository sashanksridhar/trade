package com.citi.trade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;


@SpringBootApplication

public class TradeApplication {

	public static final MongoClientURI uri = new MongoClientURI(
		    "mongodb+srv://mongoUser:cR5p1eKma8qWgIhp@cluster0.cddgx.mongodb.net/Task5?retryWrites=true&w=majority");
	public static final MongoClient myMongo = new MongoClient(uri);
	public static final MongoDatabase database = myMongo.getDatabase("Task5");
	public static void main(String[] args) {
		SpringApplication.run(TradeApplication.class, args);
	}

}
