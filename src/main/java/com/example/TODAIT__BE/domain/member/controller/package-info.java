/**
 * Member controllers are grouped by API path and authentication flow instead of
 * being split one file per endpoint.
 *
 * <p>Current groups:</p>
 * <ul>
 *     <li>{@code AuthController}: email signup, email login, logout</li>
 *     <li>{@code TokenController}: access token refresh</li>
 *     <li>{@code OAuthController}: Kakao and Google login</li>
 *     <li>{@code EmailVerificationController}: email verification send and verify</li>
 *     <li>{@code OnboardingController}: social onboarding completion</li>
 *     <li>{@code MemberController}: member profile and nickname availability</li>
 * </ul>
 *
 * <p>Swagger docs interfaces should stay one-to-one with their controllers.
 * Add new member APIs to the closest existing flow rather than creating empty
 * scaffold controllers or docs interfaces in advance.</p>
 */
package com.example.TODAIT__BE.domain.member.controller;
