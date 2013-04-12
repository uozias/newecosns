package com.example.newecosns.models;

import jp.innovationplus.ipp.jsontype.IPPApplicationResource;

import org.codehaus.jackson.annotate.JsonIgnore;


public class PEBItem extends IPPApplicationResource  {

	private long id = 0;
	private String name = null; //行動のなまえ
	private String text = null;


	private double money = 0;
	private double coe = 0;
	private double co2= 0;

	private String detail = null;
	private String place = null;

	private String author_name = null; //作者のuser_name



	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public double getMoney() {
		return money;
	}

	public void setMoney(double money) {
		this.money = money;
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

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public long getId() {
		return id;
	}

	public void setId(long l) {
		this.id = l;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getAuthor_name() {
		return author_name;
	}

	public void setAuthor_name(String author_name) {
		this.author_name = author_name;
	}

	@Override
	@JsonIgnore
	public String getResourceName() {

		return "peb";
	}





}
