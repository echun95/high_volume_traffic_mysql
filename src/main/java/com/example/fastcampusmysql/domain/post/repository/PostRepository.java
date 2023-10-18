package com.example.fastcampusmysql.domain.post.repository;

import com.example.fastcampusmysql.domain.member.entity.Member;
import com.example.fastcampusmysql.util.PageHelper;
import com.example.fastcampusmysql.domain.post.dto.DailyPostCount;
import com.example.fastcampusmysql.domain.post.dto.DailyPostCountRequest;
import com.example.fastcampusmysql.domain.post.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class PostRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final static String TABLE = "Post";

    private static final RowMapper<Post> ROW_MAPPER = (ResultSet resultSet, int rowNum) -> Post.builder()
            .id(resultSet.getLong("id"))
            .memberId(resultSet.getLong("memberId"))
            .contents(resultSet.getString("contents"))
            .createdDate(resultSet.getObject("createdDate", LocalDate.class))
            .createdTime(resultSet.getObject("createdTime", LocalDateTime.class))
            .likeCount(resultSet.getLong("likeCount"))
            .build();

    private static final RowMapper<DailyPostCount> DAILY_POST_COUNT_MAPPER = (ResultSet resultSet, int rowNum)
            -> new DailyPostCount(
            resultSet.getLong("memberId"),
            resultSet.getObject("createdDate", LocalDate.class),
            resultSet.getLong("count")
    );

    public List<DailyPostCount> groupByCreatedDate(DailyPostCountRequest request) {
        String sql = String.format("SELECT createdDate, memberId, count(id) count" +
                " FROM %s" +
                " where memberId = :memberId" +
                " and createdDate between :firstDate and :lastDate" +
                " GROUP BY memberId, createdDate", TABLE);
        BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(request);
        return namedParameterJdbcTemplate.query(sql, params, DAILY_POST_COUNT_MAPPER);
    }

    public Optional<Post> findById(Long postId, Boolean requiredLock){
        String sql = String.format("""
                SELECT *
                FROM %s
                WHERE id = :postId
                """, TABLE);
        if(requiredLock){
            sql += " FOR UPDATE";
        }
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("postId", postId);
        Post nullablePost = namedParameterJdbcTemplate.queryForObject(sql, params, ROW_MAPPER);
        return Optional.ofNullable(nullablePost);
    }

    public Page<Post> findAllByMemberId(Long memberId, Pageable pageable) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("size", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        String sql = String.format("""
                SELECT *
                FROM %s
                WHERE memberId = :memberId
                ORDER BY %s
                LIMIT :size
                OFFSET :offset
                """, TABLE, PageHelper.orderBy(pageable.getSort()));
        List<Post> posts = namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);
        Long totalCount = getCount(memberId);
        return new PageImpl(posts, pageable, totalCount);
    }

    private Long getCount(Long memberId) {
        String sql = String.format("""
                SELECT count(id)
                FROM %s
                WHERE memberId = :memberId
                """, TABLE);
        MapSqlParameterSource param = new MapSqlParameterSource().addValue("memberId", memberId);
        return namedParameterJdbcTemplate.queryForObject(sql, param, Long.class);
    }

    public List<Post> findAllByMemberIdAndOrderByIdDesc(Long memberId, int size){
        String sql = String.format("""
                SELECT *
                FROM %s
                WHERE memberId = :memberId
                ORDER BY id desc
                LIMIT :size
                """, TABLE);
        MapSqlParameterSource param = new MapSqlParameterSource().addValue("memberId", memberId).addValue("size", size);
        return namedParameterJdbcTemplate.query(sql, param, ROW_MAPPER);
    }

    public List<Post> findAllByInId(List<Long> ids){
        if(ids.isEmpty()){
            return List.of();
        }
        String sql = String.format("""
                SELECT *
                FROM %s
                WHERE id in (:ids)
                """, TABLE);
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("ids", ids);
        return namedParameterJdbcTemplate.query(sql, param, ROW_MAPPER);
    }
    public List<Post> findAllByInMemberIdAndOrderByIdDesc(List<Long> memberIds, int size){
        if(memberIds.isEmpty()){
            return List.of();
        }
        String sql = String.format("""
                SELECT *
                FROM %s
                WHERE memberId in (:memberIds)
                ORDER BY id desc
                LIMIT :size
                """, TABLE);
        MapSqlParameterSource param = new MapSqlParameterSource().addValue("memberIds", memberIds).addValue("size", size);
        return namedParameterJdbcTemplate.query(sql, param, ROW_MAPPER);
    }
    public List<Post> findAllByLessThanIdAndMemberIdAndOrderByIdDesc(Long id, Long memberId, int size){
        String sql = String.format("""
                SELECT *
                FROM %s
                WHERE memberId = :memberId and id < :id
                ORDER BY id desc
                LIMIT :size
                """, TABLE);
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("size", size)
                .addValue("id", id);
        return namedParameterJdbcTemplate.query(sql, param, ROW_MAPPER);
    }

    public List<Post> findAllByLessThanIdAndInMemberIdAndOrderByIdDesc(Long id, List<Long> memberIds, int size){
        if(memberIds.isEmpty()){
            return List.of();
        }
        String sql = String.format("""
                SELECT *
                FROM %s
                WHERE memberId in (:memberIds) and id < :id
                ORDER BY id desc
                LIMIT :size
                """, TABLE);
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("memberIds", memberIds)
                .addValue("size", size)
                .addValue("id", id);
        return namedParameterJdbcTemplate.query(sql, param, ROW_MAPPER);
    }

    public Post save(Post post) {
        if (post.getId() == null) {
            return insert(post);
        }

        return update(post);
    }

    public void bulkInsert(List<Post> posts) {
        String sql = String.format("INSERT INTO %s (memberId, contents, createdDate, createdTime) " +
                "VALUES (:memberId, :contents, :createdDate, :createdTime)", TABLE);
        SqlParameterSource[] param = posts.stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate(sql, param);
    }

    private Post insert(Post post) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate())
                .withTableName(TABLE)
                .usingGeneratedKeyColumns("id");
        SqlParameterSource params = new BeanPropertySqlParameterSource(post);
        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return Post.builder()
                .id(id)
                .memberId(post.getMemberId())
                .contents(post.getContents())
                .createdDate(post.getCreatedDate())
                .createdTime(post.getCreatedTime())
                .build();
    }

    private Post update(Post post){
        String sql = String.format(""" 
                UPDATE %s 
                set 
                memberId = :memberId, 
                contents = :contents, 
                createdDate = :createdDate, 
                likeCount = :likeCount,
                createdTime = :createdTime 
                WHERE id = :id
                """, TABLE);
        SqlParameterSource params = new BeanPropertySqlParameterSource(post);
        namedParameterJdbcTemplate.update(sql, params);
        return post;
    }


}
