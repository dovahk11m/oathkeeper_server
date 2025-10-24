package com.oath.domain.groups.groupRepository;

import com.oath.domain.groups.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
    // 기본적인 CRUD 메서드가 자동으로 제공됩니다.
}