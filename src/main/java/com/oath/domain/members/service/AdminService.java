package com.oath.domain.members.service;

import com.oath.common.exception.Exception401;
import com.oath.domain.chats.Chat;
import com.oath.domain.chats.ChatRepository;
import com.oath.domain.groups.Group;
import com.oath.domain.groups.groupRepository.GroupRepository;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.domain.Role;
import com.oath.domain.members.domain.Status;
import com.oath.domain.members.dto.*;
import com.oath.domain.members.repository.AdminRepository;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.place_tag_plan.place.Place;
import com.oath.domain.place_tag_plan.place.PlaceRepository;
import com.oath.domain.place_tag_plan.place_tag.PlaceTag;
import com.oath.domain.place_tag_plan.place_tag.PlaceTagRepository;
import com.oath.domain.place_tag_plan.plan_tag.PlanTag;
import com.oath.domain.place_tag_plan.plan_tag.PlanTagRepository;
import com.oath.domain.place_tag_plan.tag.Tag;
import com.oath.domain.place_tag_plan.tag.TagRepository;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.ParticipantRepository;
import com.oath.domain.plan.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;

    private final MemberRepository memberRepository;

    private final GroupRepository groupRepository;

    private final ChatRepository chatRepository;

    private final PlanJpaRepository planJpaRepository;

    private final PlanTagRepository planTagRepository;

    private final ParticipantRepository participantRepository;

    private final PlaceRepository placeRepository;

    private final TagRepository tagRepository;

    private final PlaceTagRepository placeTagRepository;

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

//    public AdminResponse.ListDto list() {
//        List<AdminResponse.MemberDto> memberDtos = memberRepository.findAll().stream()
//                .map(m -> new AdminResponse.MemberDto(m))
//                .collect(Collectors.toList());
//
//        AdminResponse.ListDto response = new AdminResponse.ListDto();
//        response.setMembers(memberDtos);
//
//        return response;
//    }

    public Page<AdminResponse.MemberDto> getMembers(Pageable pageable) {
        Page<AdminResponse.MemberDto> members = memberRepository.findAll(pageable)
                .map(m -> new AdminResponse.MemberDto(m));


        return members;
    }

    public void updateRole(Long id, String role) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));

                member.setRole(Role.valueOf(role));
        memberRepository.save(member);
    }

    public List<AdminResponse.popularPlanTag> getPopularPlanTag(LocalDateTime startDate, LocalDateTime endDate) {
        PageRequest topTen = PageRequest.of(0, 10);
        List<AdminResponse.popularPlanTag> popularPlanTags = adminRepository.populrPlanTag(startDate, endDate, topTen);
        return popularPlanTags;
    }

    public List<AdminResponse.PlanTagPie> PlanTagPie(LocalDateTime startDate, LocalDateTime endDate) {
        PageRequest topTen = PageRequest.of(0, 10);
        List<AdminResponse.PlanTagPie> PlanTagsPie = adminRepository.PlanTagPie(startDate, endDate, topTen);
        return PlanTagsPie;
    }

    public Page<AdminResponse.groupListDto> getGroupList(Pageable pageable) {
        Page<AdminResponse.groupListDto> groups = groupRepository.findAll(pageable)
                .map(g -> new AdminResponse.groupListDto(g));
        return groups;
    }

    public List<ActiveChartDto> activeCharts() {
        List<ActiveChartDto> activeChart = adminRepository.activeChart();
        return activeChart;
    }

    public List<AdminResponse.ChatDto> chatList(Long groupId){
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

    public List<AdminResponse.ChatDto> chatListByMember (Long groupId, Long memberId){
        List<AdminResponse.ChatDto> chats = chatRepository.findByGroup_IdAndSender_IdOrderBySentAt(groupId, memberId)
                .stream()
                .map(m -> new AdminResponse.ChatDto(m))
                .collect(Collectors.toList());
        return chats;
    }


    public List<AdminResponse.PlanDto> getPlanList(Long groupId) {
        List<AdminResponse.PlanDto> plans = adminRepository.getPlanList(groupId);
        for (AdminResponse.PlanDto plan : plans) {
            List<String> tagNames = planTagRepository.findByPlanId(plan.getPlanId())
                    .stream()
                    .map(pt -> pt.getTag().getName())
                    .collect(Collectors.toList());
            plan.setTags(tagNames);
        }

        for (AdminResponse.PlanDto plan : plans) {
            List<String> profileImageUrls = participantRepository.findByPlanId(plan.getPlanId())
                    .stream()
                    .map(pp -> pp.getMember().getProfileImageUrl())
                    .collect(Collectors.toList());
            plan.setProfileImageUrl(profileImageUrls);
        }

        return plans;
    }

    public AdminResponse.DailyTagCount buildDailyTagCount(LocalDateTime startDate, LocalDateTime endDate) {

        List<Object[]> rows = adminRepository.getDailyTagCount(startDate, endDate);

        Map<String, List<Long>> tagToCounts = new LinkedHashMap<>();
        List<String> dates = new ArrayList<>();

        for (Object[] row : rows) {
            String tag = (String) row[0];
            String date = row[1].toString();
            Long count = (Long) row[2];

            if(!dates.contains(date)) {
                dates.add(date);
            }

            tagToCounts.computeIfAbsent(tag, k-> new ArrayList<>());
            tagToCounts.get(tag).add(count);
        }

        List<AdminResponse.DailyTagCount.DailyTag> dailyTags = tagToCounts.entrySet()
                .stream()
                .map(e -> new AdminResponse.DailyTagCount.DailyTag(e.getKey(), e.getValue()))
                .toList();

        return new AdminResponse.DailyTagCount(dates, dailyTags);
    }

    public List<AdminResponse.MonthlyCount> getMonthlyCount() {
        return adminRepository.getMonthlyCount();
    }

    public List<AdminResponse.activeCount> getActiveCount() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<AdminResponse.activeCount> activeCount = adminRepository.getActiveCount(oneMonthAgo);
        return activeCount;
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

    public List<String> getAllTags() {
        return tagRepository.findAll().stream()
                .map(t -> t.getName())
                .toList();
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
}
