package com.oath.domain.place_tag_plan.tag;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagAdminService {

    private final TagRepository tagRepository;

    // 태그 생성 로직
    @Transactional
    public Tag createTag(TagRequestDto.CreateTagDto requestDto) {
        Tag tag = Tag.builder()
                .name(requestDto.getName())
                .createdAt(LocalDateTime.now())
                .build();
        return tagRepository.save(tag);
    }

    // 태그 수정 로직
    @Transactional
    public Tag updateTag(Long tagId, TagRequestDto.UpdateTagDto requestDto) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found with id: " + tagId));

        tag.setName(requestDto.getName());

        return tagRepository.save(tag);
    }

    // 태그 삭제 로직
    @Transactional
    public void deleteTag(Long tagId) {
        if (!tagRepository.existsById(tagId)) {
            throw new EntityNotFoundException("Tag not found with id: " + tagId);
        }
        tagRepository.deleteById(tagId);
    }

    // 모든 태그 조회 로직
    public List<Tag> findAllTags() {
        return tagRepository.findAll();
    }

    // ID로 태그 조회 로직
    public Tag findTagById(Long tagId) {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found with id: " + tagId));
    }
}
