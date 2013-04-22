package com.example.newecosns.models;

import jp.innovationplus.ipp.jsontype.IPPApplicationResource;

import org.codehaus.jackson.annotate.JsonIgnore;

public class StarItem extends IPPApplicationResource {

	String target_resource_id = null;
	String target_class = null;

	@Override
	@JsonIgnore
	public String getResourceName() {

		return "star";
	}

	public String getTarget_resource_id() {
		return target_resource_id;
	}

	public void setTarget_resource_id(String target_resource_id) {
		this.target_resource_id = target_resource_id;
	}

	public String getTarget_class() {
		return target_class;
	}

	public void setTarget_class(String target_class) {
		this.target_class = target_class;
	}

}
