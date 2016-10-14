package com.example.tweekend;

public class ListItems {

	private String name;
	private String screenName;
	private String text;
	private String time;
	private byte[] p1;
	private byte[] p2;
	private byte[] p3;
	private byte[] p4;

	public ListItems(String name, String screenName, String text, String time, byte[] p1,byte[] p2, byte[] p3, byte[] p4){
		this.name = name;
		this.screenName = screenName;
		this.text = text;
		this.time = time;
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
		this.p4 = p4;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public byte[] getP1() {
		return p1;
	}

	public void setP1(byte[] p1) {
		this.p1 = p1;
	}

	public byte[] getP2() {
		return p2;
	}

	public void setP2(byte[] p2) {
		this.p2 = p2;
	}

	public byte[] getP3() {
		return p3;
	}

	public void setP3(byte[] p3) {
		this.p3 = p3;
	}

	public byte[] getP4() {
		return p4;
	}

	public void setP4(byte[] p4) {
		this.p4 = p4;
	}
}
