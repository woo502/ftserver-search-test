package com.book.bean;

public class HotWord extends BaseBean {

	public long id;
	public String kw;
	public long count;
	public byte T;
	
	public String getKw() {
		return kw;
	}
	public void setKw(String kw) {
		this.kw = kw;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	
	public HotWord(long id, String kw, Byte T) {
		this.id = id;
		this.kw = kw;
		this.T = T == null ? 0 : T.byteValue();
		this.count = 1;
	}
	
	public HotWord() {
		this(0l, "", (byte)0);
	}
	
}
