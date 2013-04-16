package com.example.newecosns.utnils;

import java.util.Comparator;

import jp.innovationplus.ipp.jsontype.IPPApplicationResource;

public class PublicResourceComparator
  implements Comparator<IPPApplicationResource>
{
  int result = 0;

  public int compare(IPPApplicationResource paramIPPApplicationResource1, IPPApplicationResource paramIPPApplicationResource2)
  {
	if (paramIPPApplicationResource1.getTimestamp() < paramIPPApplicationResource2.getTimestamp()) this.result = 1;
	if (paramIPPApplicationResource1.getTimestamp() > paramIPPApplicationResource2.getTimestamp()) this.result = -1;
	else if (paramIPPApplicationResource1.getTimestamp() == paramIPPApplicationResource2.getTimestamp()) this.result = 0;

      return this.result;

  }


}

/* Location:           C:\Users\Saizo Aoyagi\aptktool\zip\classes_dex2jar.jar
 * Qualified Name:     com.example.newecosns.utnils.PublicResourceComparator
 * JD-Core Version:    0.6.2
 */