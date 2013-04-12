package com.example.newecosns.models;

import jp.innovationplus.ipp.jsontype.IPPApplicationResource;

import org.codehaus.jackson.annotate.JsonIgnore;

public class MarkItem extends IPPApplicationResource {

	private int id = 0;
	private String name = null;
	private String owner = null;

	@Override
	@JsonIgnore
	public String getResourceName() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

}
