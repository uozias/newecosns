package com.example.newecosns.models;


import jp.innovationplus.ipp.jsontype.IPPApplicationResource;

import org.codehaus.jackson.annotate.JsonIgnore;

import android.graphics.Bitmap;

public class CommentItem extends IPPApplicationResource {

	//IPPプラットフォーム対応
	String CommentUserName = null;
	long CommentTimestamp = 0L;


	//twitter対応
	String CommentName = null; //ユーザ名、本名
	String CommentScreenName = null; //スクリーンネーム @hogehgoe みたいなの
	String CommentIconUrl = null;
	String CommentRetweeterName = null;
	int CommentFavCount = 0;
	long CommentUserId = 0; //tiwtter上のユーザid
	long CommentTwitterId = 0; //tiwtter上のtweetid

	String CommentParentResourceId = null;

	//コメント自体の属性
	String CommentCreated = null; //(log)
	String CommentText = null;
	long CommentId = 0; //IPP上のResourceIDと対応
	//int CommentIdInUser = 0; //android端末内部のsqliteでのid

	//個々の端末内部DBでのid
	long CommentIdInUser = 0;


	//位置情報
	double longitude = 0;
	double latitude = 0;
	String provider = null;
	double accuracy = 0;

	//写真
	Bitmap picture = null;

	//役割
	String role = null;
	String team_resource_id = null;
	String pair_resource_id = null;
	String pair_common_id = null;

	public String getCommentUserName() {
		return CommentUserName;
	}
	public void setCommentUserName(String commentUserName) {
		CommentUserName = commentUserName;
	}

	public String getCommentCreated() {
		return CommentCreated;
	}
	public void setCommentCreated(String commentCreated) {
		CommentCreated = commentCreated;
	}
	public String getCommentScreenName() {
		return CommentScreenName;
	}
	public void setCommentScreenName(String commentScreenName) {
		CommentScreenName = commentScreenName;
	}
	public String getCommentIconUrl() {
		return CommentIconUrl;
	}
	public void setCommentIconUrl(String commentIconUrl) {
		CommentIconUrl = commentIconUrl;
	}
	public String getCommentRetweeterName() {
		return CommentRetweeterName;
	}
	public void setCommentRetweeterName(String commentRetweeterName) {
		CommentRetweeterName = commentRetweeterName;
	}
	public int getCommentFavCount() {
		return CommentFavCount;
	}
	public void setCommentFavCount(int commentFavCount) {
		CommentFavCount = commentFavCount;
	}
	public long getCommentUserId() {
		return CommentUserId;
	}
	public void setCommentUserId(long commentUserId) {
		CommentUserId = commentUserId;
	}
	public String getCommentText() {
		return CommentText;
	}
	public void setCommentText(String commentText) {
		CommentText = commentText;
	}
	public long getCommentId() {
		return CommentId;
	}
	public void setCommentId(long commentId) {
		CommentId = commentId;
	}
	/*
	public int getCommentIdInUser() {
		return CommentIdInUser;
	}
	public void setCommentIdInUser(int commentIdInUser) {
		CommentIdInUser = commentIdInUser;
	}
	*/
	public String getCommentName() {
		return CommentName;
	}
	public void setCommentName(String commentName) {
		CommentName = commentName;
	}
	public long getCommentTwitterId() {
		return CommentTwitterId;
	}
	public void setCommentTwitterId(long commentTwitterId) {
		CommentTwitterId = commentTwitterId;
	}

	public long getCommentTimestamp() {
		return CommentTimestamp;
	}
	public void setCommentTimestamp(long commentTimestamp) {
		CommentTimestamp = commentTimestamp;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getProvider() {
		return provider;
	}
	public void setProvider(String provider) {
		this.provider = provider;
	}
	public double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}
	public Bitmap getPicture() {
		return picture;
	}
	public void setPicture(Bitmap picture) {
		this.picture = picture;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public String getCommentParentResourceId() {
		return CommentParentResourceId;
	}
	public void setCommentParentResourceId(String commentParentResourceId) {
		CommentParentResourceId = commentParentResourceId;
	}
	public long getCommentIdInUser() {
		return CommentIdInUser;
	}
	public void setCommentIdInUser(long commentIdInUser) {
		CommentIdInUser = commentIdInUser;
	}
	@Override
	@JsonIgnore
	public String getResourceName() {
		return "comment";
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
	public String getPair_resource_id() {
		return pair_resource_id;
	}
	public void setPair_resource_id(String pair_resource_id) {
		this.pair_resource_id = pair_resource_id;
	}

	//この他に、IPPPublicResourceが持っているTimestampとResourceIDがある



}

