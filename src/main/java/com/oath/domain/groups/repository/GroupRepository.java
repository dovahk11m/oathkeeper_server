package com.oath.domain.groups.repository;

import com.oath.domain.groups.Group;
import com.oath.domain.members.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {

    // 기본적인 CRUD 메서드가 자동으로 제공됩니다.
    Page<Group> findAll(Pageable pageable);

    @Query("SELECT COUNT(g) FROM Group g")
    Long getTotalGroupCount();

    long count();

    Optional<Group> findByName(String name);
}

