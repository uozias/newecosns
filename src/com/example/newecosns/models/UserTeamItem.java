package com.example.newecosns.models;

import jp.innovationplus.ipp.jsontype.IPPApplicationResource;

import org.codehaus.jackson.annotate.JsonIgnore;

public class UserTeamItem extends IPPApplicationResource  {
	private String user_name;
	private long team_id = 0;
	private String team_resource_id = null;

	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	public long getTeam_id() {
		return team_id;
	}
	public void setTeam_id(long team_id) {
		this.team_id = team_id;
	}
	public String getTeam_resource_id() {
		return team_resource_id;
	}
	public void setTeam_resource_id(String team_resource_id) {
		this.team_resource_id = team_resource_id;
	}
	@Override
	@JsonIgnore
	public String getResourceName() {

		return "user_team";
	}

}
