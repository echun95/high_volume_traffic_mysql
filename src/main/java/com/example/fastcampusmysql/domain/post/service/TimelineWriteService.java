package com.example.fastcampusmysql.domain.post.service;

import com.example.fastcampusmysql.domain.post.entity.Timeline;
import com.example.fastcampusmysql.domain.post.repository.TimelineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TimelineWriteService {
    private final TimelineRepository timelineRepository;

    public void deliveryToTimeline(Long postId, List<Long> toMemberIds){
        List<Timeline> timelines = toMemberIds.stream()
                .map((memberId) -> toTimeline(postId, memberId))
                .toList();
        timelineRepository.bulkInsert(timelines);
    }

    private Timeline toTimeline(Long postId, Long memberId) {
        return Timeline.builder().memberId(memberId).postId(postId).build();
    }

}
