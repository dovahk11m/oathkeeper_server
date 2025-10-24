// DOM 요소가 모두 로드된 후에 스크립트를 실행합니다.
document.addEventListener('DOMContentLoaded', function () {

    // DOM 요소 참조
    const chatArea = document.getElementById('chatArea');
    const status = document.getElementById('status');
    const msgInput = document.getElementById('msgInput');
    const chatForm = document.getElementById('chatForm');
    const sendButton = document.getElementById('sendButton');

    // 전역 변수 선언
    let stompClient = null;

    // HTML에 저장된 JWT 토큰을 가져옵니다.
    const jwtToken = document.body.dataset.jwtToken;

    // 스톰프 연결 시작
    function connect() {
        if (!jwtToken) {
            console.error('JWT 토큰을 찾을 수 없습니다.');
            status.textContent = "인증 토큰이 없어 연결할 수 없습니다.";
            status.className = 'status disconnected';
            return;
        }

        const socket = new SockJS('/ws-stomp');
        stompClient = Stomp.over(socket);

        const headers = { 'Authorization': 'Bearer ' + jwtToken };
        stompClient.connect(headers, onConnected, onError);
    }

    // 연결 요청 함수 실행
    connect();

    // 연결 성공 콜백 메서드
    function onConnected() {
        status.textContent = "실시간 연결됨(STOMP)";
        status.className = 'status connected';
        sendButton.disabled = false;
        sendButton.textContent = '전송';
        msgInput.focus();

        // 메시지 채널 구독
        stompClient.subscribe('/topic/message', onMessageReceived);
        console.log('STOMP 연결 및 구독 처리 완료');
    }

    // 연결 실패 콜백 메서드
    function onError(error) {
        status.textContent = "연결 에러 발생";
        status.className = 'status disconnected';
        sendButton.disabled = true;
        console.error('STOMP 연결 에러', error);
    }

    // 메시지 수신 처리
    function onMessageReceived(message) {
        const chat = JSON.parse(message.body);

        if (chat.content && chat.content.startsWith('ERROR')) {
            console.error('ERROR 발생');
            return;
        }

        const messageDiv = document.createElement('div');
        messageDiv.className = 'message';
        messageDiv.textContent = `[${chat.sender.username}] ${chat.content}`;
        chatArea.appendChild(messageDiv);

        chatArea.scrollTop = chatArea.scrollHeight;
    }

    // 폼 전송 이벤트 리스너
    chatForm.addEventListener('submit', function (e) {
        e.preventDefault();
        sendMessage();
    });

    // 메시지 전송 메서드
    function sendMessage() {
        const messageContent = msgInput.value.trim();

        if (messageContent && stompClient && stompClient.connected) {
            sendButton.disabled = true;
            sendButton.textContent = '전송중...';

            stompClient.send('/app/chat', {}, messageContent);
            msgInput.value = '';
            msgInput.focus();

            setTimeout(() => {
                sendButton.disabled = false;
                sendButton.textContent = '전송';
            }, 500);
        } else {
            alert('STOMP 연결이 끊어졌습니다');
        }
    }

    // 페이지 종료 시 연결 종료 처리
    window.addEventListener('beforeunload', () => {
        if (stompClient) {
            stompClient.disconnect();
            console.log('STOMP 연결 정리 완료');
        }
    });
});