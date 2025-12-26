package com.example.response;

public class PassengerDetailsResponse {
	private String name;
	private String email;
	private String phoneNum;

	public PassengerDetailsResponse() {}

	public PassengerDetailsResponse(String name, String email, String phoneNum) {
		this.name = name;
		this.email = email;
		this.phoneNum = phoneNum;
	}

	public String getName() { return name; }
	public String getEmail() { return email; }
	public String getPhoneNum() { return phoneNum; }
}
