package com.example.newecosns.utnils;

import java.util.ArrayList;
import java.util.List;

import com.example.newecosns.models.MarkItem;

public class  Constants{

	//role
	public static final int SENPAI = 1;
	public static final int KOHAI = 2;
	public static final int RELAXED_ROLE = 3;

	//stress_now
	public static final int RELAXED = 1;
	public static final int COHESIVE = 2;
	public static final int STRESS_DEFAULT =COHESIVE;

	//peb place tag
	public static final String INNDOOR  = "indoor";
	public static final String OUTDOOR = "outdoor";
	public static final String PURCHASE = "purchase";


	//Twitter関連データ
	public static final String CALLBACK = "http://sns.uozias.jp";
	public static final String CONSUMER_KEY = "AJOoyPGkkIRBgmjAtVNw";
	public static final String CONSUMER_SECRET = "1OMzUfMcqy4QHkyT6jJoUyxN4KXEu7R87k3bVOzp8c";
	public static final String hash_tag = "ecosns_test";

	//GCMのセンダーid
	public static final String SENDERID ="399376059950";


	//エコ関連マーク
	private String[] array = new String[7];
	private List<MarkItem> MarkList = new ArrayList<MarkItem>();

	public List<MarkItem> getMarkList() {

		array[0] = "なし";
		array[1] = "エコマーク";
		array[2] = "エコリーフ";
		array[3] = "エネルギースター";
		array[4] = "再生紙使用マーク";
		array[5] = "省エネラベル";
		array[5] = "その他";



		for (int i = 0; i < array.length; i++){
			MarkItem mark_item = new MarkItem();
			mark_item.setId(i);
			mark_item.setName(array[i]);
			MarkList.add(mark_item);

		}


		return MarkList;
	}








}
