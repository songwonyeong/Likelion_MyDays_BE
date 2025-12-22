package com.mydays.backend.application.todo;

import com.mydays.backend.domain.Category;
import com.mydays.backend.domain.Member;
import com.mydays.backend.domain.Todo;
import com.mydays.backend.dto.todo.TodoDtos;
import com.mydays.backend.repository.CategoryRepository;
import com.mydays.backend.repository.TodoRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final CategoryRepository categoryRepository;
    private final TodoRepository todoRepository;

    // ✅ 핵심: 영속 Member 프록시를 얻기 위해 추가
    private final EntityManager em;

    private Member managedMember(Member member) {
        if (member == null || member.getId() == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        // DB hit 없이 프록시(영속 상태) 반환
        return em.getReference(Member.class, member.getId());
    }

    @Transactional
    public Todo create(Member member, TodoDtos.CreateReq req){
        Member m = managedMember(member);

        Category category = categoryRepository.findById(req.getCategoryId())
                .filter(c -> c.getMember() != null && c.getMember().getId().equals(m.getId()))
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + req.getCategoryId()));

        boolean done = req.getDone() != null ? req.getDone() : false;

        Todo todo = Todo.builder()
                .member(m)
                .category(category)
                .content(req.getContent())
                .done(done)
                .scheduledDate(req.getDate())
                .scheduledTime(req.getTime())
                .build();

        return todoRepository.save(todo);
    }


    @Transactional(readOnly = true)
    public List<Todo> listByDate(Member member, LocalDate date) {
        Member m = managedMember(member);
        return todoRepository.findAllByMemberAndScheduledDateOrderByScheduledTimeAscIdAsc(m, date);
    }

    @Transactional
    public Todo setDone(Member member, Long todoId, boolean done) {
        Member m = managedMember(member);

        Todo t = todoRepository.findByIdAndMember(todoId, m)
                .orElseThrow(() -> new IllegalArgumentException("할일을 찾을 수 없습니다."));
        t.setDone(done);
        return t;
    }

    @Transactional
    public void delete(Member member, Long todoId) {
        Member m = managedMember(member);

        Todo t = todoRepository.findByIdAndMember(todoId, m)
                .orElseThrow(() -> new IllegalArgumentException("할일을 찾을 수 없습니다."));
        todoRepository.delete(t);
    }
}
