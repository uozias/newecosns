package com.example.newecosns.geomodel;

import jp.innovationplus.ipp.jsontype.IPPGeoResource;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.example.newecosns.models.CommentItem;

public class CommentGeoResource extends IPPGeoResource<CommentItem> {

	@Override
	@JsonIgnore
	public String getResourceName() {
		// TODO 自動生成されたメソッド・スタブ
		return "comment";
	}

}
