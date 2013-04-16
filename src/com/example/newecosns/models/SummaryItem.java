package com.example.newecosns.models;

import jp.innovationplus.ipp.jsontype.IPPApplicationResource;

import org.codehaus.jackson.annotate.JsonIgnore;

public class SummaryItem extends IPPApplicationResource  {

	//IPPプラットフォーム対応
	String user_name = null;
	String screen_name = null;

	//twitter 対応
	long user_id = 0; ///tiwtter上のユーザid(現状では家計簿自体がtwitterに投稿されることはない)

	//家計簿ログまとめ自体の属性
	String updated = null;

	int number = 0;
	double money = 0; //節約できた金額　節約系のエコでカウント
	double co2 = 0;
	int price = 0; //使った金額 商品購入系のエコでつかう

	int rank_of_number = 0;
	int rank_of_money = 0;
	int rank_of_co2 = 0;
	int rank_of_price = 0;
	//timestampは継承

	//役割
	String role = null;
	String team_resource_id = null;

	String pair_common_id = null;

	@Override
	@JsonIgnore
	public String getResourceName() {

		return "summary";
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


	public long getUser_id() {
		return user_id;
	}


	public void setUser_id(long user_id) {
		this.user_id = user_id;
	}


	public String getUpdated() {
		return updated;
	}


	public void setUpdated(String updated) {
		this.updated = updated;
	}


	public int getNumber() {
		return number;
	}


	public void setNumber(int number) {
		this.number = number;
	}


	public double getMoney() {
		return money;
	}


	public void setMoney(double money) {
		this.money = money;
	}


	public double getCo2() {
		return co2;
	}


	public void setCo2(double co2) {
		this.co2 = co2;
	}


	public int getRank_of_number() {
		return rank_of_number;
	}


	public void setRank_of_number(int rank_of_number) {
		this.rank_of_number = rank_of_number;
	}


	public int getRank_of_money() {
		return rank_of_money;
	}


	public void setRank_of_money(int rank_of_money) {
		this.rank_of_money = rank_of_money;
	}


	public int getRank_of_co2() {
		return rank_of_co2;
	}


	public void setRank_of_co2(int rank_of_co2) {
		this.rank_of_co2 = rank_of_co2;
	}


	public int getPrice() {
		return price;
	}


	public void setPrice(int price) {
		this.price = price;
	}


	public int getRank_of_price() {
		return rank_of_price;
	}


	public void setRank_of_price(int rank_of_price) {
		this.rank_of_price = rank_of_price;
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




}
