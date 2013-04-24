package com.example.newecosns.utnils;

import java.util.Comparator;

import com.example.newecosns.models.SummaryItem;

public class SummaryItemRankComparator implements Comparator<SummaryItem>
{
  int result = 0;

  public int compare(SummaryItem summaryItem1, SummaryItem summaryItem2)
  {
	if (summaryItem1.getNumber() < summaryItem2.getNumber()) this.result = 1;
	if (summaryItem1.getNumber() > summaryItem2.getNumber()) this.result = -1;
	else if (summaryItem1.getNumber() == summaryItem2.getNumber()) this.result = 0;

      return this.result;

  }


}

