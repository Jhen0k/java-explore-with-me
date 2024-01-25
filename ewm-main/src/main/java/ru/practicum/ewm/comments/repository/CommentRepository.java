package ru.practicum.ewm.comments.repository;



import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.comments.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c " +
            "from comments as c " +
            "where lower(c.text) like lower(concat('%', ?1, '%') )")
    List<Comment> search(String text, Pageable pageable);

    List<Comment> findByAuthor_Id(Long userId);

    List<Comment> findAllByEvent_Id(Long eventId, Pageable pageable);


}