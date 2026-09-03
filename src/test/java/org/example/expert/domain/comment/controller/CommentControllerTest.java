package org.example.expert.domain.comment.service;

import jakarta.persistence.EntityManager;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
class CommentControllerTest {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TodoRepository todoRepository;
    @Autowired
    private EntityManager em;

    @Test
    void N_plus_1_해결_여부_테스트() {
        User writer = userRepository.save(new User("writer@email.com", "password", "글쓴이", UserRole.USER));
        Todo todo = todoRepository.save(new Todo("제목", "내용", "맑음", writer));

        for (int i = 1; i <= 3; i++) {
            User commenter = userRepository.save(new User("user" + i + "@email.com", "password", "댓글러" + i, UserRole.USER));
            commentRepository.save(new Comment("댓글 " + i, commenter, todo));
        }

        em.flush();
        em.clear();   // 1차 캐시 초기화 — 여기가 핵심

        System.out.println("====== 쿼리 실행 시작 ======");
        List<Comment> comments = commentRepository.findByTodoIdWithUser(todo.getId());

        for (Comment comment : comments) {
            System.out.println("댓글 내용: " + comment.getContents() + ", 작성자 이메일: " + comment.getUser().getEmail());
        }
        System.out.println("====== 쿼리 실행 종료 ======");
    }
}