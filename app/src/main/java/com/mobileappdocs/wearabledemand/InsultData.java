package com.mobileappdocs.wearabledemand;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jonathan on 29/12/2015.
 */
public class InsultData {

    @SerializedName("insult")
    private String insult;
    @SerializedName("source")
    private String source;
    @SerializedName("sourceUrl")
    private String sourceUrl;

    public InsultData(String insult, String source, String sourceUrl) {
        this.insult = insult;
        this.source = source;
        this.sourceUrl = sourceUrl;
    }

    public String getInsult() {
        return insult;
    }

    public void setInsult(String insult) {
        this.insult = insult;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }
}
