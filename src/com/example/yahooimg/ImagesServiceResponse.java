package com.example.yahooimg;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ImagesServiceResponse {
	@SerializedName("start")
	public String respStart;
	@SerializedName("count")
	public String resultsCount;
	@SerializedName("totalresults")
	public String totalResults;
	@SerializedName("results")
	public List<ResponseImgData> results;

}
