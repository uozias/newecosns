package com.example.newecosns.models;

import jp.innovationplus.ipp.jsontype.IPPApplicationResource;

import org.codehaus.jackson.annotate.JsonIgnore;

public class TeamItem extends IPPApplicationResource   {




	@Override
	@JsonIgnore
	public String getResourceName() {

		return "team";
	}




}
