package com.oath.domain.terms.dto;

import com.oath.domain.terms.Term;
import lombok.Getter;

@Getter
public class TermResponse {
    private Long id;
    private String title;
    private String content;
    private boolean isRequired;

    public TermResponse(Term term) {
        this.id = term.getId();
        this.title = term.getTitle();
        this.content = term.getContent();
        this.isRequired = term.isRequired();
    }
}
