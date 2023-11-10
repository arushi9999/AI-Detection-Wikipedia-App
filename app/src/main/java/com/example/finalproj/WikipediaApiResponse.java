package com.example.finalproj;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class WikipediaApiResponse {
    @SerializedName("query")
    private QueryResult queryResult;

    public QueryResult getQueryResult() {
        return queryResult;
    }

    public static class QueryResult {
        @SerializedName("pages")
        private Map<String, Page> pages;

        public Map<String, Page> getPages() {
            return pages;
        }
    }

    public static class Page {
        @SerializedName("extract")
        private String extract;

        public String getExtract() {
            return extract;
        }
    }
}
