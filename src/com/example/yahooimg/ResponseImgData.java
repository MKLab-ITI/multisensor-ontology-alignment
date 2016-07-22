package com.example.yahooimg;

import com.google.gson.annotations.SerializedName;

public class ResponseImgData {

	@SerializedName("refererurl")
	public String ImgWebsite;
	@SerializedName("url")
	public String imgUrl;
	@SerializedName("title")
	public String resTitle;

}
