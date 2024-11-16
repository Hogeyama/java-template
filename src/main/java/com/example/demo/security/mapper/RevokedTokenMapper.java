package com.example.demo.security.mapper;

import com.example.demo.security.entity.RevokedToken;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

@Mapper
public interface RevokedTokenMapper {
    @Insert("INSERT INTO revoked_tokens (jti, reason, created_by_user_id) " +
            "VALUES (#{jti}, #{reason}, #{createdByUserId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(RevokedToken revokedToken);

    @Select("SELECT * FROM revoked_tokens WHERE jti = #{jti}")
    Optional<RevokedToken> findByJti(String jti);

    @Delete("DELETE FROM revoked_tokens WHERE jti = #{jti}")
    void deleteByJti(String jti);

    // トークンが無効化されているかどうかを確認
    @Select("SELECT EXISTS(SELECT 1 FROM revoked_tokens WHERE jti = #{jti})")
    boolean isTokenRevoked(String jti);
}
