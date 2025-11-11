package com.mydays.backend.application.todo;

import com.mydays.backend.domain.Category;
import com.mydays.backend.domain.Member;
import com.mydays.backend.domain.Todo;
import com.mydays.backend.dto.todo.TodoDtos;
import com.mydays.backend.repository.CategoryRepository;
import com.mydays.backend.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service @RequiredArgsConstructor
public class TodoService {

    private final CategoryRepository categoryRepository;
    private final TodoRepository todoRepository;

    @Transactional
    public Todo create(Member member, TodoDtos.CreateReq req){
        Category category = categoryRepository.findByMemberAndName(member, req.getCategory_name())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + req.getCategory_name()));

        boolean done = req.getDone() != null ? req.getDone() : false;

        Todo todo = Todo.builder()
                .member(member)
                .category(category)
                .content(req.getContent())
                .done(done)
                .scheduledDate(req.getDate())
                .scheduledTime(req.getTime())
                .build();

        return todoRepository.save(todo);
    }

    @Transactional(readOnly = true)
    public List<Todo> listByDate(Member member, LocalDate date){
        return todoRepository.findAllByMemberAndScheduledDateOrderByScheduledTimeAscIdAsc(member, date);
    }

    @Transactional
    public Todo setDone(Member member, Long todoId, boolean done){
        Todo t = todoRepository.findByIdAndMember(todoId, member)
                .orElseThrow(() -> new IllegalArgumentException("할일을 찾을 수 없습니다."));
        t.setDone(done);
        return t;
    }

    @Transactional
    public void delete(Member member, Long todoId){
        Todo t = todoRepository.findByIdAndMember(todoId, member)
                .orElseThrow(() -> new IllegalArgumentException("할일을 찾을 수 없습니다."));
        todoRepository.delete(t);
    }
}
