package com.oath.domain.terms.termRepository;

import com.oath.domain.terms.Term;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TermRepository extends JpaRepository<Term, Long> {

    // 필수 약관(isRequired가 true인 약관) 목록을 조회하는 쿼리 메소드
    List<Term> findAllByIsRequired(boolean isRequired);
}
