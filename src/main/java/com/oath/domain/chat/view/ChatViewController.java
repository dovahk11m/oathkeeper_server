package com.oath.domain.chat.view;

import com.oath.common.JwtTokenProvider;
import com.oath.domain.chat.Chat;
import com.oath.domain.members.domain.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@RequiredArgsConstructor
@Controller // 뷰 반환
public class ChatViewController {

    private final ChatViewService chatService;
    private final JwtTokenProvider jwtTokenProvider;

    // 메세지 작성 폼 페이지
    @GetMapping("/save-form")
    public String saveForm() {
        return "save-form";
    }

    // 채팅 목록 페이지
    @GetMapping("/")
    public String index(Model model) {
        // [수정] 테스트를 위해 user1의 토큰을 생성하여 모델에 추가합니다.
        String testToken = jwtTokenProvider.createToken("user1@test.com", Role.USER);
        model.addAttribute("jwtToken", testToken);

        model.addAttribute("models", chatService.findAll());
        return "index";
    }

    // 채팅 메세지 저장 요청
    @PostMapping("/chat")
    public String save(String msg) {
        chatService.save(msg);
        // [수정] PRG(Post-Redirect-Get) 패턴을 적용하여 중복 전송을 방지합니다.
        return "redirect:/";
    }

    /* 메시지 매핑
    MessageMapping의 내부 동작 원리 (AOP)
    유저: /app/chat xxx -> 메시지브로커 -> 브로드캐스트 xxx
    순수 웹소켓 구현시 만들었던 웹소켓 핸들러를 대체한다.
    MessageMapping -> handleMessage() 로직을 대체한다. if -> Chat
    @SendTo("/topic/message") -> 기존 broadcastMessage() 로직 대체
     */
    @MessageMapping("/chat")
    @SendTo("/topic/message")
    public Chat sendMessage(String message, SimpMessageHeaderAccessor headerAccessor) {
        log.info("@ 스톰프 메시지 수신: {}", message);
        try {
            if(message ==null || message.trim().isEmpty()) {
                log.warn("@ 빈 메시지 수신, 브로드캐스트 중단");
                return null;
            }
            Chat savedChat = chatService.save(message.trim());
            return savedChat; // [수정] Chat 객체 전체를 반환하여 JSON으로 변환되도록 함
        } catch (Exception e) {
            log.error("@ 메시지 저장 실패: ",e);
            // 예외 발생 시 클라이언트에 에러 메시지를 담은 Chat 객체를 보낼 수도 있습니다.
            return Chat.builder().content("@ ERROR: 메시지 저장에 실패했습니다.").build();
        }


    }

}
