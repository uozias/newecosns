package com.example.newecosns.models;

import jp.innovationplus.ipp.jsontype.IPPApplicationResource;

import org.codehaus.jackson.annotate.JsonIgnore;

public class PairItem extends IPPApplicationResource {

	String team_resource_id = null;
	String pair_target_team_resoure_id = null; //相手のteamリソースのid

	int role_self = 0;
	String stress_now = null;

	long start = 0; //タイムスタンプミリ秒で開始日時
	long end = 0; //タイムスタンプミリ秒で終了日時




	@Override
	@JsonIgnore
	public String getResourceName() {
		return "pair";
	}







	//普通のgetter, setter


	public String getTeam_resource_id() {
		return team_resource_id;
	}


	public void setTeam_resource_id(String team_resource_id) {
		this.team_resource_id = team_resource_id;
	}


	public String getPair_target_team_resoure_id() {
		return pair_target_team_resoure_id;
	}


	public void setPair_target_team_resoure_id(String pair_target_team_resoure_id) {
		this.pair_target_team_resoure_id = pair_target_team_resoure_id;
	}




	public long getStart() {
		return start;
	}


	public void setStart(long start) {
		this.start = start;
	}


	public long getEnd() {
		return end;
	}


	public void setEnd(long end) {
		this.end = end;
	}


	public String getStress_now() {
		return stress_now;
	}


	public void setStress_now(String stress_now) {
		this.stress_now = stress_now;
	}







	public int getRole_self() {
		return role_self;
	}







	public void setRole_self(int role_self) {
		this.role_self = role_self;
	}

}

