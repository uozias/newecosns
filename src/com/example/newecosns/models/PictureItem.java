package com.example.newecosns.models;

import jp.innovationplus.ipp.jsontype.IPPApplicationResource;

import org.codehaus.jackson.annotate.JsonIgnore;


public class PictureItem extends IPPApplicationResource  {

	private int id = 0;
	private String log_resource_id = null;//IPPPublicResourceのid
	private byte[] data = null;
	private double width = 0;
	private double height = 0;



	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLog_resource_id() {
		return log_resource_id;
	}
	public void setLog_resource_id(String log_resource_id) {
		this.log_resource_id = log_resource_id;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	@Override
	@JsonIgnore
	public String getResourceName() {

		return "picture";
	}
	public double getWidth() {
		return width;
	}
	public void setWidth(double width) {
		this.width = width;
	}
	public double getHeight() {
		return height;
	}
	public void setHeight(double height) {
		this.height = height;
	}


}
