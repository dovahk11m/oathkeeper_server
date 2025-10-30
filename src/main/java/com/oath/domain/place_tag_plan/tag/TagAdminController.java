package com.oath.domain.place_tag_plan.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/tags")
public class TagAdminController {

    private final TagAdminService tagAdminService;

    // 태그 목록 페이지 표시
    @GetMapping
    public String listTags(Model model) {
        List<Tag> tags = tagAdminService.findAllTags();
        model.addAttribute("tags", tags);
        return "admin/tags/list"; // templates/admin/tags/list.html (or .mustache) 뷰를 반환
    }

    // 새 태그 등록 폼 페이지 표시
    @GetMapping("/new")
    public String newTagForm(Model model) {
        model.addAttribute("tag", new TagRequestDto.CreateTagDto());
        return "admin/tags/new"; // templates/admin/tags/new.html (or .mustache) 뷰를 반환
    }

    // 태그 생성 처리
    @PostMapping
    public String createTag(TagRequestDto.CreateTagDto requestDto, RedirectAttributes redirectAttributes) {
        tagAdminService.createTag(requestDto);
        redirectAttributes.addFlashAttribute("message", "태그가 성공적으로 생성되었습니다.");
        return "redirect:/admin/tags";
    }

    // 태그 수정 폼 페이지 표시
    @GetMapping("/{tagId}/edit")
    public String editTagForm(@PathVariable Long tagId, Model model) {
        Tag tag = tagAdminService.findTagById(tagId);
        TagRequestDto.UpdateTagDto updateDto = new TagRequestDto.UpdateTagDto();
        updateDto.setName(tag.getName());
        model.addAttribute("tagId", tagId);
        model.addAttribute("tag", updateDto);
        return "admin/tags/edit"; // templates/admin/tags/edit.html (or .mustache) 뷰를 반환
    }

    // 태그 수정 처리
    @PostMapping("/{tagId}") // PUT 대신 POST를 사용하여 폼 제출 처리
    public String updateTag(@PathVariable Long tagId, TagRequestDto.UpdateTagDto requestDto, RedirectAttributes redirectAttributes) {
        tagAdminService.updateTag(tagId, requestDto);
        redirectAttributes.addFlashAttribute("message", "태그가 성공적으로 수정되었습니다.");
        return "redirect:/admin/tags";
    }

    // 태그 삭제 처리
    @PostMapping("/{tagId}/delete") // DELETE 대신 POST를 사용하여 폼 제출 처리
    public String deleteTag(@PathVariable Long tagId, RedirectAttributes redirectAttributes) {
        tagAdminService.deleteTag(tagId);
        redirectAttributes.addFlashAttribute("message", "태그가 성공적으로 삭제되었습니다.");
        return "redirect:/admin/tags";
    }
}
