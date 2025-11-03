package com.oath.common.auth;

import com.oath.domain.members.domain.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API에 대한 접근 제어를 위한 어노테이션
 *
 * 사용 예시:
 *
 * 1. 단순 인증 (로그인한 모든 사용자 접근 가능)
 *    @Auth
 *    @GetMapping("/some-resource")
 *    public ResponseEntity<?> getSomeResource() { ... }
 *
 * 2. 관리자 인증 (ADMIN 역할만 접근 가능)
 *    @Auth(roles = {Role.ADMIN})
 *    @GetMapping("/admin/resource")
 *    public ResponseEntity<?> getAdminResource() { ... }
 *
 * 3. 소유자 인증 (리소스의 소유자만 접근 가능)
 *    - isOwner = true로 설정하고, 컨트롤러 메소드에서 소유자 여부를 직접 확인해야 합니다.
 *    - AuthInterceptor에서는 isOwner 플래그만 제공하며, 실제 소유자 확인 로직은 각 컨트롤러에서 구현해야 합니다.
 *    - 예: 게시글 작성자만 해당 게시글을 수정/삭제할 수 있도록 하는 경우
 *
 *    @Auth(isOwner = true)
 *    @PutMapping("/posts/{postId}")
 *    public ResponseEntity<?> updatePost(@PathVariable Long postId, @RequestBody PostUpdateRequest request, @AuthenticationPrincipal UserDetails userDetails) {
 *        // 1. postId로 게시글 조회
 *        // 2. 게시글 작성자와 userDetails.getUsername() (또는 userDetails.getMemberId()) 비교
 *        // 3. 일치하지 않으면 예외 처리
 *        // 4. 일치하면 수정 로직 수행
 *    }
 */
@Target(ElementType.METHOD) // @Auth 메소드에만다
@Retention(RetentionPolicy.RUNTIME) // 런타임에 이 정보를 JVM이 볼수있다
public @interface Auth {

    Role[] roles() default {}; // 독립된 Role 열거형을 사용하도록 수정

    boolean isOwner() default false; // 리소스 소유자 확인 여부
}
