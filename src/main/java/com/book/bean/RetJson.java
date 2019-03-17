package com.book.bean;

public class RetJson extends BaseBean{
	
	public enum RetCode {
		
		SUCCESS(0, "成功"),
		UNKONWERR(-1, "系统繁忙"),
		PARAMERR(-2, "参数错误"),;
		
		private RetCode(long code, String msg) {
			this.code = code;
			this.msg = msg;
		}
		
		public long code;
		public String msg;
		
		public static String getMsg(long code) {
			RetCode[] businessModeEnums = values();  
	        for (RetCode businessModeEnum : businessModeEnums) {  
	            if (businessModeEnum.code == code) {  
	                return businessModeEnum.msg;  
	            }  
	        }  
	        return "系统繁忙";  
		}
	}
	

	protected long errCode;
	protected String msg;
	protected Object data;
	
	public RetJson() {
		this(0l, null);
	}
	
	public RetJson(long errCode, Object data) {
		this.errCode = errCode;
		this.data = data;
		this.msg = RetCode.getMsg(errCode);
	}
	
	public long getErrCode() {
		return errCode;
	}
	public void setErrCode(long errCode) {
		this.errCode = errCode;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	
	
	
}
