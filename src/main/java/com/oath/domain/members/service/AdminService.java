package com.oath.domain.members.service;

import com.oath.common.exception.Exception401;
import com.oath.domain.chats.Chat;
import com.oath.domain.chats.ChatRepository;
import com.oath.domain.groups.Group;
import com.oath.domain.groups.repository.GroupMemberRepository;
import com.oath.domain.groups.repository.GroupRepository;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.Role;
import com.oath.domain.members.domain.Status;
import com.oath.domain.members.dto.*;
import com.oath.domain.members.repository.AdminRepository;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.place_tag_plan.place.Place;
import com.oath.domain.place_tag_plan.place.PlaceRepository;
import com.oath.domain.place_tag_plan.place.PlaceResponseDto;
import com.oath.domain.place_tag_plan.place_tag.PlaceTag;
import com.oath.domain.place_tag_plan.place_tag.PlaceTagRepository;
import com.oath.domain.place_tag_plan.tag.Tag;
import com.oath.domain.place_tag_plan.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;

    private final MemberRepository memberRepository;

    private final ChatRepository chatRepository;

    private final PlaceRepository placeRepository;

    private final TagRepository tagRepository;

    private final PlaceTagRepository placeTagRepository;

    @Value("${kakao.rest-api-key}")
    private String kakaoApiKey;

    private final RestTemplate restTemplate;

    public void banMember(Member member, int days) {
        LocalDateTime now = LocalDateTime.now();

        if (member.getStatus() == Status.SUSPENDED && member.getBannedUntil() != null && member.getBannedUntil().isAfter(now)) {
            member.setBannedUntil(member.getBannedUntil().plusDays(days));
        } else {
            member.setStatus(Status.SUSPENDED);
            member.setBannedUntil(now.plusDays(days));
        }
        memberRepository.save(member);
    }

    public Page<AdminResponse.MemberDto> getMembers(String type, String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            Page<AdminResponse.MemberDto> members = memberRepository.findAll(pageable)
                    .map(m -> new AdminResponse.MemberDto(m));
            return members;
        }
        Page<AdminResponse.MemberDto> members = adminRepository.searchMember(type, keyword, pageable)
                    .map(m -> new AdminResponse.MemberDto(m));
        return members;
    }

    public void updateRole(Long id, String role) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));

                member.setRole(Role.valueOf(role));
        memberRepository.save(member);
    }

    public List<AdminResponse.PlanTagPie> PlanTagPie() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(1);

        PageRequest topTen = PageRequest.of(0, 10);
        List<AdminResponse.PlanTagPie> PlanTagsPie = adminRepository.PlanTagPie(startDate, endDate, topTen);
        return PlanTagsPie;
    }

    public List<AdminResponse.ChatDto> getChat(Long groupId){
        List<AdminResponse.ChatDto> chats = chatRepository.findByGroupIdOrderBySentAt(groupId)
                .stream()
                .map(m -> new AdminResponse.ChatDto(m))
                .collect(Collectors.toList());
        return chats;
    }

    public List<AdminResponse.ChatMemberDto> chatMember(Long groupId) {
        List<AdminResponse.ChatMemberDto> chatMembers = adminRepository.chatMember(groupId);
        return chatMembers;
    }

    public List<AdminResponse.MonthlyCount> getMonthlyCount() {
        return adminRepository.getMonthlyCount();
    }



    public List<AdminResponse.placeList> getPlaceList() {
        List<Place> places = placeRepository.findAll();
        List<AdminResponse.placeList> dtos = places.stream()
                .map(p -> new AdminResponse.placeList(p))
                .toList();
        return dtos;
    }

    public List<String> getTagList() {
        List<Tag> tags = tagRepository.findAll();
        List<String> dtos = tags.stream()
                .map(t -> t.getName())
                .toList();
        return dtos;
    }

    public void addTag(String name) {
        Tag tag = Tag.builder()
                .name(name)
                .createdAt(LocalDateTime.now())
                .build();
        tagRepository.save(tag);
    }

    public void deleteTag(String name) {
        Tag tag = tagRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("해당이름의 태그가 없습니다"));

        tagRepository.delete(tag);
    }

    public void updateDescription(Long id, AdminRequest.updateDescription req) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 존재하지 않습니다"));
        place.update(req.getDescription());
    }

    public List<AdminResponse.PlaceTag> getPlaceTag() {
        List<AdminResponse.PlaceTag> placeTags = placeRepository.findAll().stream()
                .map(p -> new AdminResponse.PlaceTag(
                        p.getId(),
                        p.getName(),
                        p.getPlaceTags().stream()
                                .map(pt -> {
                                    Tag tag = pt.getTag();
                                    return new AdminResponse.TagDto(tag.getId(), tag.getName());
                                })
                                .toList()
                        ))
                .toList();
        return placeTags;
    }

    public List<AdminResponse.AddedTagDto> addPlaceTag(AdminRequest.PlaceTag req) {
        List<AdminResponse.AddedTagDto> addedTags = new ArrayList<>();

        for(Long placeId : req.getPlaceIds()) {
            Place place = placeRepository.findById(placeId)
                    .orElseThrow(() -> new IllegalArgumentException("장소를 찾을 수 없습니다."));

            for(String tagName : req.getTags()){
                Tag tag = tagRepository.findByName(tagName)
                        .orElseThrow(() -> new IllegalArgumentException("태그를 찾을 수 없습니다."));

                boolean exists = placeTagRepository.existsByPlaceAndTag(place, tag);
                if(!exists) {
                    PlaceTag pt = new PlaceTag();
                    pt.setPlace(place);
                    pt.setTag(tag);
                    placeTagRepository.save(pt);

                    addedTags.add(
                            new AdminResponse.AddedTagDto(
                                    place.getId(),
                                    tag.getId(),
                                    tag.getName()
                            )
                    );
                }
            }
        }
        return addedTags;
    }

    public void deletePlaceTag(Long placeId, Long tagId) {
        PlaceTag placeTag = placeTagRepository.findByPlace_IdAndTag_Id(placeId, tagId)
                        .orElseThrow(() -> new IllegalArgumentException("장소-태그가 존재하지 않습니다."));

        placeTagRepository.delete(placeTag);
    }

    public PlaceResponseDto.PlaceDto searchPlace(String keyword) {
        String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query="
                + UriUtils.encode(keyword, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<PlaceResponseDto.PlaceDto> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, PlaceResponseDto.PlaceDto.class);

        return response.getBody();

    }

    public Place savePlace(AdminRequest.PlaceDto reqDto) {
        Place place = Place.builder()
                .name(reqDto.getName())
                .address(reqDto.getAddress())
                .lat(reqDto.getLat())
                .lng(reqDto.getLng())
                .build();

        return placeRepository.save(place);
    }

    public Page<AdminResponse.GroupList> getGroupList(Pageable pageble) {
        Page<AdminResponse.GroupList> groups = adminRepository.getGroupList(pageble);

        return groups;
    }

    public Page<AdminResponse.GroupList> searchGroupList(String keyword, Pageable pageable) {
        Page<AdminResponse.GroupList> groups = adminRepository.findByGroupName(keyword, pageable);
        return groups;
    }

    public Page<AdminResponse.GroupList> searchGroupListByMemberEmail(String keyword, Pageable pageable) {
        Page<AdminResponse.GroupList> groups = adminRepository.findByMemberEmail(keyword, pageable);
        return groups;
    }

    public List<AdminResponse.PlanCount> getPlanCount() {
        List<AdminResponse.PlanCount> planCount = adminRepository.getPlanCount();
        return planCount;
    }

    public List<AdminResponse.ParticipantCount> getParticipantCount() {
        List<AdminResponse.ParticipantCount> participantCount = adminRepository.getParticipantCount();
        return participantCount;
    }

    public List<AdminResponse.ChatListDto> chatList(Long groupId) {
        List<AdminResponse.ChatListDto> chatListDtos = adminRepository.getChatList(groupId);
        return chatListDtos;
    }
}
