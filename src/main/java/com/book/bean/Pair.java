package com.book.bean;

public class Pair extends BaseBean {

	protected Object key;
	protected Object value;
	
	public Pair(){
		
	}
	
	public Pair(Object key, Object value){
		this.key = key;
		this.value = value;
	}
	
	public Object getKey() {
		return key;
	}
	public void setKey(Object key) {
		this.key = key;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
}
