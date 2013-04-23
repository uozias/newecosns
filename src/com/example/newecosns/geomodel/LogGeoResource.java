package com.example.newecosns.geomodel;

import jp.innovationplus.ipp.jsontype.IPPGeoResource;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.example.newecosns.models.LogItem;

public class LogGeoResource extends IPPGeoResource<LogItem> {

	@Override
	@JsonIgnore
	public String getResourceName() {

		return "log";
	}

}
