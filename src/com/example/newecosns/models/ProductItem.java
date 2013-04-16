package com.example.newecosns.models;

import jp.innovationplus.ipp.jsontype.IPPApplicationResource;

import org.codehaus.jackson.annotate.JsonIgnore;

public class ProductItem extends IPPApplicationResource {

	long id = 0;
	String author_name = null;
	String name = null;
	String producer = null;
	int producer_id = 0;
	int mark_id = 0;

	String team_resource_id = null;
	String pair_resource_id = null;
	String pair_common_id = null;

	@Override
	@JsonIgnore
	public String getResourceName() {

		return "product";
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAuthor_name() {
		return author_name;
	}

	public void setAuthor_name(String author_name) {
		this.author_name = author_name;
	}



	public int getMark_id() {
		return mark_id;
	}

	public void setMark_id(int mark_id) {
		this.mark_id = mark_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProducer() {
		return producer;
	}

	public void setProducer(String producer) {
		this.producer = producer;
	}

	public int getProducer_id() {
		return producer_id;
	}

	public void setProducer_id(int producer_id) {
		this.producer_id = producer_id;
	}

	public String getTeam_resource_id() {
		return team_resource_id;
	}

	public void setTeam_resource_id(String team_resource_id) {
		this.team_resource_id = team_resource_id;
	}

	public String getPair_resource_id() {
		return pair_resource_id;
	}

	public void setPair_resource_id(String pair_resource_id) {
		this.pair_resource_id = pair_resource_id;
	}

	public String getPair_common_id() {
		return pair_common_id;
	}

	public void setPair_common_id(String pair_common_id) {
		this.pair_common_id = pair_common_id;
	}

}
