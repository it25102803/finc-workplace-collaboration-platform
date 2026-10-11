package com.finc.platform.repository;

import com.finc.platform.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChannelIdOrderByTimestampAsc(Long channelId);

    @Query("select message from Message message "
            + "where locate(lower(:query), lower(message.content)) > 0 "
            + "order by message.timestamp desc")
    List<Message> searchByContent(@Param("query") String query, Pageable pageable);
}
