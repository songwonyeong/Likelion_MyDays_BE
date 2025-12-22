package com.mydays.backend.controller;

import com.mydays.backend.config.CurrentMember;
import com.mydays.backend.domain.Member;
import com.mydays.backend.domain.Todo;
import com.mydays.backend.dto.todo.TodoDtos;
import com.mydays.backend.application.todo.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    /** 할일 등록 */
    @PostMapping
    public ResponseEntity<TodoDtos.Resp> create(@CurrentMember Member member,
                                                @Valid @RequestBody TodoDtos.CreateReq req){
        Todo t = todoService.create(member, req);
        return ResponseEntity.ok(toResp(t));
    }

    /** 날짜별 할일 조회 ?date=YYYY-MM-DD */
    @GetMapping
    public ResponseEntity<List<TodoDtos.Resp>> listByDate(@CurrentMember Member member,
                                                          @RequestParam("date")
                                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
        List<Todo> list = todoService.listByDate(member, date);
        return ResponseEntity.ok(list.stream().map(this::toResp).toList());
    }

    /** 완료여부 수정 */
    @PatchMapping("/{id}/done")
    public ResponseEntity<TodoDtos.Resp> setDone(@CurrentMember Member member,
                                                 @PathVariable Long id,
                                                 @Valid @RequestBody TodoDtos.ToggleDoneReq req){
        Todo t = todoService.setDone(member, id, req.getDone());
        return ResponseEntity.ok(toResp(t));
    }

    /** 할일 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@CurrentMember Member member, @PathVariable Long id){
        todoService.delete(member, id);
        return ResponseEntity.noContent().build();
    }

    private TodoDtos.Resp toResp(Todo t){
        return TodoDtos.Resp.builder()
                .id(t.getId())
                .categoryId(t.getCategory().getId()) // ✅ 추가
                .category_name(t.getCategory().getName())
                .category_color(t.getCategory().getColor())
                .content(t.getContent())
                .done(t.isDone())
                .date(t.getScheduledDate())
                .time(t.getScheduledTime())
                .build();
    }
}
