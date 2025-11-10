package com.oath.domain.place_tag_plan.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<String> autocompleteTagNames(String prefix) {
        Pageable limit = PageRequest.of(0, 10); // 최대 10개까지 결과 제한
        List<Tag> tags = tagRepository.findByNameStartingWith(prefix, limit);
        return tags.stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
    }
}
