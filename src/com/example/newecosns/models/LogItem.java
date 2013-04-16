package com.example.newecosns.models;

import jp.innovationplus.ipp.jsontype.IPPApplicationResource;

import org.codehaus.jackson.annotate.JsonIgnore;

public class LogItem extends IPPApplicationResource  {

	//IPPプラットフォーム対応
	String user_name = null;
	String screen_name = null;

	//twitter 対応
	long user_id = 0; ///tiwtter上のユーザid(現状では家計簿自体がtwitterに投稿されることはない)

	//PEBログ自体の属性
	String created = null;
	String updated= null;
	String category = null;
	String name= null;

	String place = null; //場所選択用に残しとく

	private double money = 0;

	//PEB
	private long peb_id = 0;
	String peb_resource_id = null;

	private double coe = 0;
	private double co2= 0;


	//個々の端末内部DBでのid
	long id_in_user = 0;

	//位置情報
	double longitude = 0;
	double latitude = 0;
	String provider = null;
	double accuracy = 0;

	//写真(内部DB用)
	byte[] picture = null;


	//写真(プラットフォーム用)
	String picture_resource_id = null;


	//商品購入系エコ向けの拡張
	long product_id = 0;
	String product_resource_id = null;
	int price = 0;

	//役割
	String role = null;
	String team_resource_id = null;
	String pair_resource_id = null;
	String pair_common_id = null;

	@Override
	@JsonIgnore
	public String getResourceName() {
		return "log";
	}




	public String getUser_name() {
		return user_name;
	}




	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}




	public String getScreen_name() {
		return screen_name;
	}




	public void setScreen_name(String screen_name) {
		this.screen_name = screen_name;
	}








	public String getCreated() {
		return created;
	}




	public void setCreated(String created) {
		this.created = created;
	}




	public String getUpdated() {
		return updated;
	}




	public void setUpdated(String updated) {
		this.updated = updated;
	}




	public String getCategory() {
		return category;
	}




	public void setCategory(String category) {
		this.category = category;
	}




	public String getName() {
		return name;
	}




	public void setName(String name) {
		this.name = name;
	}




	public String getPlace() {
		return place;
	}




	public void setPlace(String place) {
		this.place = place;
	}




	public double getMoney() {
		return money;
	}




	public void setMoney(double money) {
		this.money = money;
	}




	public long getPeb_id() {
		return peb_id;
	}




	public void setPeb_id(long l) {
		this.peb_id = l;
	}




	public double getCoe() {
		return coe;
	}




	public void setCoe(double coe) {
		this.coe = coe;
	}




	public double getCo2() {
		return co2;
	}




	public void setCo2(double co2) {
		this.co2 = co2;
	}







	public double getLongitude() {
		return longitude;
	}




	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}




	public double getLatitude() {
		return latitude;
	}




	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}




	public String getProvider() {
		return provider;
	}




	public void setProvider(String provider) {
		this.provider = provider;
	}




	public double getAccuracy() {
		return accuracy;
	}




	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}




	public byte[] getPicture() {
		return picture;
	}




	public void setPicture(byte[] picture) {
		this.picture = picture;
	}




	public String getPicture_resource_id() {
		return picture_resource_id;
	}




	public void setPicture_resource_id(String picture_resource_id) {
		this.picture_resource_id = picture_resource_id;
	}




	public long getUser_id() {
		return user_id;
	}




	public void setUser_id(long user_id) {
		this.user_id = user_id;
	}




	public long getId_in_user() {
		return id_in_user;
	}




	public void setId_in_user(long id_in_user) {
		this.id_in_user = id_in_user;
	}




	public long getProduct_id() {
		return product_id;
	}




	public void setProduct_id(long product_id) {
		this.product_id = product_id;
	}




	public int getPrice() {
		return price;
	}




	public void setPrice(int price) {
		this.price = price;
	}




	public String getPeb_resource_id() {
		return peb_resource_id;
	}




	public void setPeb_resource_id(String peb_resource_id) {
		this.peb_resource_id = peb_resource_id;
	}




	public String getProduct_resource_id() {
		return product_resource_id;
	}




	public void setProduct_resource_id(String product_resource_id) {
		this.product_resource_id = product_resource_id;
	}




	public String getRole() {
		return role;
	}




	public void setRole(String role) {
		this.role = role;
	}




	public String getTeam_resource_id() {
		return team_resource_id;
	}




	public void setTeam_resource_id(String team_resource_id) {
		this.team_resource_id = team_resource_id;
	}




	public String getPair_common_id() {
		return pair_common_id;
	}




	public void setPair_common_id(String pair_common_id) {
		this.pair_common_id = pair_common_id;
	}




	public String getPair_resource_id() {
		return pair_resource_id;
	}




	public void setPair_resource_id(String pair_resource_id) {
		this.pair_resource_id = pair_resource_id;
	}




}



