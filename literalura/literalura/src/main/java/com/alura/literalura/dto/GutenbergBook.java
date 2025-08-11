package com.alura.literalura.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;
import java.util.Map;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true) // Ignora campos inesperados da API
public class GutenbergBook {

    private int id;
    private String title;
    private List<GutenbergAuthor> authors;
    private List<String> languages;
    private Integer downloadCount;
    private Map<String, String> formats;
    private List<String> summaries; // Novo campo adicionado

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public List<GutenbergAuthor> getAuthors() {
        return authors;
    }
    public void setAuthors(List<GutenbergAuthor> authors) {
        this.authors = authors;
    }

    public List<String> getLanguages() {
        return languages;
    }
    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }
    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public Map<String, String> getFormats() {
        return formats;
    }
    public void setFormats(Map<String, String> formats) {
        this.formats = formats;
    }

    public List<String> getSummaries() {
        return summaries;
    }
    public void setSummaries(List<String> summaries) {
        this.summaries = summaries;
    }
}