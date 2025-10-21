package com.oath.domain.proposals;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "proposal_votes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposalVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long proposalId; // FK plan_proposals.id

    @Column(nullable = false)
    private Long memberId; // FK members.id

    @Column(nullable = false)
    private LocalDateTime selectedDatetime;
}
