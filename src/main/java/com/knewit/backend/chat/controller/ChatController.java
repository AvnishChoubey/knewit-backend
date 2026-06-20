//package com.knewit.backend.chat.controller;
//
//import com.knewit.backend.chat.entity.ChatMessage;
//import com.knewit.backend.chat.entity.ChatConversation;
//import com.knewit.backend.chat.entity.ChatRoomMember;
//import com.knewit.backend.chat.repository.ChatMessageRepository;
//import com.knewit.backend.chat.repository.ChatRoomMemberRepository;
//import com.knewit.backend.chat.repository.ChatRoomRepository;
//import com.knewit.backend.chat.request.CreateGroupChatRequest;
//import com.knewit.backend.user.entity.User;
//import com.knewit.backend.user.repository.UserRepository;
//import jakarta.servlet.http.HttpSession;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.ResponseEntity;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.*;
//
//@RestController
//@RequestMapping("/api/chat")
//public class ChatController {
//
//    private final SimpMessagingTemplate messagingTemplate;
//    private final ChatMessageRepository chatMessageRepository;
//    private final ChatRoomRepository chatRoomRepository;
//    private final ChatRoomMemberRepository chatRoomMemberRepository;
//    private final UserRepository userRepository;
//
//    public ChatController(SimpMessagingTemplate messagingTemplate, ChatMessageRepository chatMessageRepository, ChatRoomRepository chatRoomRepository, ChatRoomMemberRepository chatRoomMemberRepository, UserRepository userRepository) {
//        this.messagingTemplate = messagingTemplate;
//        this.chatMessageRepository = chatMessageRepository;
//        this.chatRoomRepository = chatRoomRepository;
//        this.chatRoomMemberRepository = chatRoomMemberRepository;
//        this.userRepository = userRepository;
//    }
//
//    // ── INBOX ──────────────────────────────────────────────
//    @GetMapping("/rooms")
//    public ResponseEntity<?> getChatRooms(HttpSession session) {
//
//        String username = (String) session.getAttribute("username");
//
//        if (username == null) {
//            return ResponseEntity.status(401)
//                    .body(Map.of("message", "User not logged in"));
//        }
//
//        Long userId = (Long) session.getAttribute("userId");
//
//        List<ChatConversation> rooms =
//                chatRoomRepository.findRoomsByUserId(userId);
//
//        Map<Long, String> roomNames =
//                buildRoomDisplayNames(rooms, username);
//
//        Map<String, Object> response = new HashMap<>();
//
//        response.put("currentUser", username);
//        response.put("currentUserId", userId);
//        response.put("rooms", rooms);
//        response.put("roomNames", roomNames);
//
//        return ResponseEntity.ok(response);
//    }
//
//    // ── OPEN A ROOM ────────────────────────────────────────
//    @GetMapping("/rooms/{roomId}")
//    public ResponseEntity<?> getChatRoom(
//            @PathVariable Long roomId,
//            HttpSession session) {
//
//        String username = (String) session.getAttribute("username");
//        Long userId = (Long) session.getAttribute("userId");
//
//        if (username == null) {
//            return ResponseEntity.status(401)
//                    .body(Map.of("message", "User not logged in"));
//        }
//
//        ChatConversation room = chatRoomRepository.findById(roomId)
//                .orElseThrow(() -> new RuntimeException("Room not found"));
//
//        // Security: user must be a member of the room
//        List<Long> memberIds =
//                chatRoomMemberRepository.findUserIdsByRoomId(roomId);
//
//        if (!memberIds.contains(userId)) {
//            return ResponseEntity.status(403)
//                    .body(Map.of("message", "Access denied"));
//        }
//
//        List<ChatMessage> messages =
//                chatMessageRepository.findByRoomId(roomId);
//
//        // Member usernames
//        List<String> memberNames = new ArrayList<>();
//
//        for (Long mid : memberIds) {
//            userRepository.findById(mid)
//                    .ifPresent(user ->
//                            memberNames.add(user.getUsername()));
//        }
//
//        // Room title
//        String roomTitle = room.getGroupName();
//
//        if ("DIRECT".equals(room.getRoomType())) {
//
//            if (room.getGroupName() != null &&
//                    room.getGroupName().contains("||")) {
//
//                String[] parts =
//                        room.getGroupName().split("\\|\\|");
//
//                roomTitle =
//                        parts[0].equals(username)
//                                ? parts[1]
//                                : parts[0];
//
//            } else {
//
//                roomTitle = memberNames.stream()
//                        .filter(name ->
//                                !name.equals(username))
//                        .findFirst()
//                        .orElse("Chat");
//            }
//        }
//
//        List<ChatConversation> rooms =
//                chatRoomRepository.findRoomsByUserId(userId);
//
//        Map<Long, String> roomNames =
//                buildRoomDisplayNames(rooms, username);
//
//        Map<String, Object> response =
//                new HashMap<>();
//
//        response.put("currentUser", username);
//        response.put("currentUserId", userId);
//
//        response.put("room", room);
//        response.put("roomTitle", roomTitle);
//
//        response.put("messages", messages);
//
//        response.put("memberNames", memberNames);
//
//        response.put("rooms", rooms);
//        response.put("roomNames", roomNames);
//
//        return ResponseEntity.ok(response);
//    }
//
//    // ── INCREMENTAL MESSAGES API (fallback/live sync) ──────
//    @GetMapping("/rooms/{roomId}/messages")
//    public ResponseEntity<?> getRoomMessages(
//            @PathVariable Long roomId,
//            @RequestParam(defaultValue = "0") Long afterId,
//            HttpSession session) {
//
//        Long userId =
//                (Long) session.getAttribute("userId");
//
//        if (userId == null) {
//            return ResponseEntity.status(401)
//                    .body(Map.of(
//                            "message",
//                            "User not logged in"
//                    ));
//        }
//
//        // Security: requester must be a member
//        List<Long> memberIds =
//                chatRoomMemberRepository.findUserIdsByRoomId(roomId);
//
//        if (!memberIds.contains(userId)) {
//            return ResponseEntity.status(403)
//                    .body(Map.of(
//                            "message",
//                            "Access denied"
//                    ));
//        }
//
//        List<Map<String, Object>> messages =
//                chatMessageRepository.findByRoomId(roomId)
//                        .stream()
//                        .filter(m ->
//                                m.getId() != null &&
//                                        m.getId() > afterId)
//                        .map(m -> {
//
//                            Map<String, Object> data =
//                                    new HashMap<>();
//
//                            data.put("id", m.getId());
//                            data.put("senderUsername",
//                                    m.getSenderUsername());
//
//                            data.put("content",
//                                    m.getContent());
//
//                            data.put("chatRoomId",
//                                    m.getChatRoomId());
//
//                            data.put("sentAt",
//                                    m.getCreatedAt() != null
//                                            ? m.getCreatedAt().toString()
//                                            : null);
//
//                            return data;
//                        })
//                        .toList();
//
//        return ResponseEntity.ok(messages);
//    }
//
//    // ── CREATE DIRECT CHAT ─────────────────────────────────
//    @PostMapping("/direct")
//    public ResponseEntity<?> createDirectChat(
//            @RequestParam String username,
//            HttpSession session) {
//
//        String currentUsername =
//                (String) session.getAttribute("username");
//
//        Long currentUserId =
//                (Long) session.getAttribute("userId");
//
//        if (currentUsername == null) {
//            return ResponseEntity.status(401)
//                    .body(Map.of(
//                            "message",
//                            "User not logged in"
//                    ));
//        }
//
//        Optional<User> targetOpt =
//                userRepository.findByUsername(username);
//
//        if (targetOpt.isEmpty()) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of(
//                            "message",
//                            "User not found"
//                    ));
//        }
//
//        if (username.equals(currentUsername)) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of(
//                            "message",
//                            "Cannot chat with yourself"
//                    ));
//        }
//
//        Long targetId = targetOpt.get().getId();
//
//        // Check if room already exists
//        Optional<ChatConversation> existing =
//                chatRoomRepository.findDirectRoom(
//                        currentUserId,
//                        targetId
//                );
//
//        if (existing.isPresent()) {
//
//            return ResponseEntity.ok(
//                    Map.of(
//                            "roomId",
//                            existing.get().getId(),
//                            "alreadyExists",
//                            true
//                    )
//            );
//        }
//
//        // Create room
//        ChatConversation room = new ChatConversation();
//
//        room.setRoomType("DIRECT");
//        room.setCreatedById(currentUserId);
//
//        room.setGroupName(
//                currentUsername + "||" + username
//        );
//
//        chatRoomRepository.save(room);
//
//        // Add current user
//        ChatRoomMember m1 =
//                new ChatRoomMember();
//
//        m1.setChatRoomId(room.getId());
//        m1.setUserId(currentUserId);
//
//        chatRoomMemberRepository.save(m1);
//
//        // Add target user
//        ChatRoomMember m2 =
//                new ChatRoomMember();
//
//        m2.setChatRoomId(room.getId());
//        m2.setUserId(targetId);
//
//        chatRoomMemberRepository.save(m2);
//
//        return ResponseEntity.ok(
//                Map.of(
//                        "roomId",
//                        room.getId(),
//                        "alreadyExists",
//                        false
//                )
//        );
//    }
//
//    // ── CREATE GROUP CHAT ──────────────────────────────────
//    @PostMapping("/group")
//    public ResponseEntity<?> createGroup(
//            @RequestBody CreateGroupChatRequest request,
//            HttpSession session) {
//
//        String currentUsername =
//                (String) session.getAttribute("username");
//
//        Long currentUserId =
//                (Long) session.getAttribute("userId");
//
//        if (currentUsername == null) {
//            return ResponseEntity.status(401)
//                    .body(Map.of(
//                            "message",
//                            "User not logged in"
//                    ));
//        }
//
//        if (request.getGroupName() == null ||
//                request.getGroupName().trim().isEmpty()) {
//
//            return ResponseEntity.badRequest()
//                    .body(Map.of(
//                            "message",
//                            "Group name is required"
//                    ));
//        }
//
//        ChatConversation room = new ChatConversation();
//
//        room.setRoomType("GROUP");
//        room.setGroupName(request.getGroupName());
//        room.setCreatedById(currentUserId);
//
//        chatRoomRepository.save(room);
//
//        // Add creator
//        ChatRoomMember creator =
//                new ChatRoomMember();
//
//        creator.setChatRoomId(room.getId());
//        creator.setUserId(currentUserId);
//
//        chatRoomMemberRepository.save(creator);
//
//        // Add selected users
//        if (request.getUsernames() != null) {
//
//            for (String username : request.getUsernames()) {
//
//                userRepository.findByUsername(username)
//                        .ifPresent(user -> {
//
//                            ChatRoomMember member =
//                                    new ChatRoomMember();
//
//                            member.setChatRoomId(room.getId());
//
//                            member.setUserId(user.getId());
//
//                            chatRoomMemberRepository.save(member);
//                        });
//            }
//        }
//
//        return ResponseEntity.ok(
//                Map.of(
//                        "message",
//                        "Group created successfully",
//                        "roomId",
//                        room.getId()
//
//                )
//        );
//    }
//
//
//    // ── SEND MESSAGE (WebSocket) ───────────────────────────
//    @MessageMapping("/chat.send")
//    public void sendMessage(@Payload ChatMessage message) {
//
//        LocalDateTime now = LocalDateTime.now();
//
//        if (message.getSentAt() == null) {
//            message.setSentAt(now);
//        }
//
//        if (message.getCreatedAt() == null) {
//            message.setCreatedAt(now);
//        }
//
//        // Resolve senderId if missing
//        if (message.getSenderId() == null &&
//                message.getSenderUsername() != null) {
//
//            userRepository.findByUsername(
//                    message.getSenderUsername()
//            ).ifPresent(user ->
//                    message.setSenderId(user.getId()));
//        }
//
//        // Resolve receiver for direct chat
//        if (message.getReceiverUsername() == null &&
//                message.getChatRoomId() != null) {
//
//            chatRoomRepository.findById(
//                    message.getChatRoomId()
//            ).ifPresent(room -> {
//
//                if ("DIRECT".equals(room.getRoomType())) {
//
//                    List<Long> memberIds =
//                            chatRoomMemberRepository
//                                    .findUserIdsByRoomId(room.getId());
//
//                    for (Long memberId : memberIds) {
//
//                        userRepository.findById(memberId)
//                                .ifPresent(user -> {
//
//                                    if (!user.getUsername()
//                                            .equals(message.getSenderUsername())
//                                            && message.getReceiverUsername() == null) {
//
//                                        message.setReceiverUsername(
//                                                user.getUsername()
//                                        );
//                                    }
//                                });
//
//                        if (message.getReceiverUsername() != null) {
//                            break;
//                        }
//                    }
//                }
//            });
//        }
//
//        // Save message
//        ChatMessage savedMessage =
//                chatMessageRepository.save(message);
//
//        // Update room metadata
//        if (savedMessage.getChatRoomId() != null) {
//
//            chatRoomRepository.findById(
//                    savedMessage.getChatRoomId()
//            ).ifPresent(room -> {
//
//                room.setLastActivityAt(LocalDateTime.now());
//
//                room.setLastMessagePreview(
//                        buildPreviewText(
//                                savedMessage.getContent()
//                        )
//                );
//
//                chatRoomRepository.save(room);
//            });
//        }
//
//        // Broadcast to subscribers
//        messagingTemplate.convertAndSend(
//                "/topic/room/" + savedMessage.getChatRoomId(),
//                savedMessage
//        );
//    }
//
//    private String buildPreviewText(String content) {
//        if (content == null) return "";
//        String oneLine = content.replaceAll("\\s+", " ").trim();
//        int max = 80;
//        if (oneLine.length() <= max) return oneLine;
//        return oneLine.substring(0, max - 1) + "…";
//    }
//
//    // ── VALIDATE USER API ──────────────────────────────────
//    @GetMapping("/validate-user")
//    public ResponseEntity<?> validateUser(
//            @RequestParam String username,
//            HttpSession session) {
//
//        String currentUser =
//                (String) session.getAttribute("username");
//
//        if (currentUser == null) {
//
//            return ResponseEntity.status(401)
//                    .body(Map.of(
//                            "valid", false,
//                            "error", "User not logged in"
//                    ));
//        }
//
//        if (username.equalsIgnoreCase(currentUser)) {
//
//            return ResponseEntity.badRequest()
//                    .body(Map.of(
//                            "valid", false,
//                            "error", "You cannot chat with yourself"
//                    ));
//        }
//
//        boolean exists =
//                userRepository.findByUsername(username)
//                        .isPresent();
//
//        if (!exists) {
//
//            return ResponseEntity.ok(
//                    Map.of(
//                            "valid", false,
//                            "error", "User \"" + username + "\" not found"
//                    )
//            );
//        }
//
//        return ResponseEntity.ok(
//                Map.of(
//                        "valid", true
//                )
//        );
//    }
//
//    // ── SEARCH USERS API (for group member picker) ─────────
//    @GetMapping("/search-users")
//    public ResponseEntity<?> searchUsers(
//            @RequestParam String q,
//            HttpSession session) {
//
//        Long currentUserId =
//                (Long) session.getAttribute("userId");
//
//        if (currentUserId == null) {
//
//            return ResponseEntity.status(401)
//                    .body(Map.of(
//                            "message",
//                            "User not logged in"
//                    ));
//        }
//
//        List<Map<String, String>> users =
//                userRepository
//                        .searchByUsernameExcluding(
//                                currentUserId,
//                                q,
//                                PageRequest.of(0, 8)
//                        )
//                        .stream()
//                        .map(user -> Map.of(
//                                "username",
//                                user.getUsername()
//                        ))
//                        .toList();
//
//        return ResponseEntity.ok(users);
//    }
//
//    private Map<Long, String> buildRoomDisplayNames(List<ChatConversation> rooms, String currentUsername) {
//        Map<Long, String> names = new HashMap<>();
//        for (ChatConversation room : rooms) {
//            if ("GROUP".equals(room.getRoomType())) {
//                names.put(room.getId(), room.getGroupName() != null ? room.getGroupName() : "Group");
//            } else {
//                // Try to get name from stored groupName field first
//                if (room.getGroupName() != null && room.getGroupName().contains("||")) {
//                    String[] parts = room.getGroupName().split("\\|\\|");
//                    String displayName = parts[0].equals(currentUsername) ? parts[1] : parts[0];
//                    names.put(room.getId(), displayName);
//                } else {
//                    // Fallback: look up members from DB and find the other person
//                    List<Long> memberIds = chatRoomMemberRepository.findUserIdsByRoomId(room.getId());
//                    String otherName = memberIds.stream()
//                            .filter(id -> {
//                                Optional<User> u = userRepository.findById(id);
//                                return u.isPresent() && !u.get().getUsername().equals(currentUsername);
//                            })
//                            .map(id -> userRepository.findById(id)
//                                    .map(User::getUsername)
//                                    .orElse("Unknown"))
//                            .findFirst()
//                            .orElse("Direct Message");
//                    names.put(room.getId(), otherName);
//
//                    if (!otherName.equals("Direct Message")) {
//                        room.setGroupName(currentUsername + "||" + otherName);
//                        chatRoomRepository.save(room);
//                    }
//
//                }
//            }
//        }
//        return names;
//    }
//}