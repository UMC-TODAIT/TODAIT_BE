package com.example.TODAIT__BE.domain.member.repository;

import com.example.TODAIT__BE.domain.member.entity.MemberOAuthAccount;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberOAuthAccountRepository extends JpaRepository<MemberOAuthAccount,Long> {

   Optional<MemberOAuthAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);

}
