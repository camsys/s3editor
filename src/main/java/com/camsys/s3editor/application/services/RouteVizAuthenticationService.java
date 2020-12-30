package com.camsys.s3editor.application.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.mindrot.jbcrypt.BCrypt;

public class RouteVizAuthenticationService {

	 private static final RouteVizAuthenticationService theInstance = new RouteVizAuthenticationService();

	 private String currentUser;
	 
	 private Connection authDatabase;
	 
	 public RouteVizAuthenticationService() {
	 }

	 public void connect() {
		 int tries = 3;
		 
		 while(tries-- > 0) {
			 try {
				 authDatabase = DriverManager.getConnection(
					 "jdbc:postgresql://otp-admin-prod.ckgyhi5n2bdz.us-east-1.rds.amazonaws.com/otp_admin_prod?readOnlyMode=true", 
					 System.getenv("AUTH_DB_USERNAME"), System.getenv("AUTH_DB_PASSWORD"));

				 if(authDatabase == null || authDatabase.isValid(10)) // in seconds
					 break;
			 } catch (Exception e) {
				 System.out.println("Database initialization failed: " + e.getLocalizedMessage());
			 }		 
		 }
	 }
	 
	 public boolean checkUser(String email, String password) {
		 try {
			 if(authDatabase == null || authDatabase.isValid(10)) // in seconds
				 connect();
			 
			 Statement stmt = authDatabase.createStatement();
			 ResultSet rs = stmt.executeQuery(String.format("SELECT encrypted_password FROM users WHERE email LIKE '%s'", email));

			 if(rs.next()) {
				 String hashedPassword = rs.getString(1); // this array is 1-based

				 if(BCrypt.checkpw(password, hashedPassword)) {
					 currentUser = email;
					 return true;
				 }
			 }

			 return false;
		 } catch(Exception e) {
			 return false;
		 }
	 }

	 public void logout() {
		 currentUser = null;
	 }
	 
	 public String getCurrentUser() {
		 return currentUser;
	 }
	 
	 public static RouteVizAuthenticationService getInstance() {
	 	return theInstance;
	 }
}