package com.example.newecosns.models;

import jp.innovationplus.ipp.jsontype.IPPApplicationResource;

import org.codehaus.jackson.annotate.JsonIgnore;

public class StressItem extends IPPApplicationResource  {

	public static final String COHESIVE= "cohesive";
	public static final String RELAXED = "relaxed";
	public static final String DEFAULT =COHESIVE;

	private String stress_now = null;

	public String getStress_now() {
		return stress_now;
	}

	public void setStress_now(String stress_now) {
		this.stress_now = stress_now;
	}

	@Override
	@JsonIgnore
	public String getResourceName() {
		// TODO 自動生成されたメソッド・スタブ
		return "stress_now";
	}


}
