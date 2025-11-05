package com.oath.domain.terms;

import com.oath.domain.members.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "member_agreed_terms_tb",
       uniqueConstraints = {
           @UniqueConstraint(
               name = "member_term_unique",
               columnNames = {"member_id", "term_id"}
           )
       })
public class MemberAgreedTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id", nullable = false)
    private Term term;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime agreedAt;
}
