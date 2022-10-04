package com.codestates.seb006main.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.codestates.seb006main.auth.PrincipalDetails;
import com.codestates.seb006main.exception.BusinessLogicException;
import com.codestates.seb006main.exception.ExceptionCode;
import com.codestates.seb006main.members.entity.Member;
import com.codestates.seb006main.members.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
@RequiredArgsConstructor
@Service
public class JwtUtils {

    @Value("${jwt.access}")
    private String accessKey;
    @Value("${jwt.refresh}")
    private String refreshKey;

    private MemberRepository memberRepository;



    public String createAccessToken(Long memberId, String email){

        String accessToken = JWT.create()
                .withSubject("hitch hiker access token")
                .withExpiresAt(new Date(System.currentTimeMillis() + (1000 * 60 * 60)))
                .withClaim("id", memberId)
                .withClaim("email", email)
                .sign(Algorithm.HMAC512(accessKey));
        accessToken = "Bearer " + accessToken;
        return accessToken;
    }

    public String createRefreshToken(Long memberId, String email){
        String refreshToken = JWT.create()
                .withSubject("hitch hiker refresh token")
                .withExpiresAt(new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 24)))
                .withClaim("id", memberId)
                .withClaim("email", email)
                .sign(Algorithm.HMAC512(refreshKey));
        refreshToken = "Bearer " + refreshToken;
        return refreshToken;
    }

    public boolean isTokenExpired(DecodedJWT jwt){
        Date exp = jwt.getExpiresAt();
        return exp.before(new Date());
    }

    public Map<String,Object> getClaimsFromToken(String token, String keys){
        String key = keys.equals("refresh")?refreshKey:accessKey;
        String email = JWT.require(Algorithm.HMAC512(key)).build().verify(token).getClaim("email").asString();
        Long id = JWT.require(Algorithm.HMAC512(key)).build().verify(token).getClaim("id").asLong();
        return Map.of("email",email,"id",id);
    }

    public String getEmailFromToken(String token){
        return JWT.decode(token).getClaim("email").asString();
    }

    public void verifiedToken(String accessToken, String refreshToken){
        if(accessToken == null || !accessToken.startsWith("Bearer")){
            throw new BusinessLogicException(ExceptionCode.TOKEN_EXPIRED);
        }else if (isTokenExpired(JWT.decode(accessToken.replace("Bearer ", "")))){
            if(refreshToken == null || !refreshToken.startsWith("Bearer")){
            }else{
                refreshToken = refreshToken.replace("Bearer ", "");
                DecodedJWT jwt = JWT.decode(refreshToken);
                if(isTokenExpired(jwt)){
                    throw new BusinessLogicException(ExceptionCode.TOKEN_EXPIRED);
                }else{
                    Map<String,Object> map = getClaimsFromToken(refreshToken,"refresh");
                    String access = createAccessToken((Long)map.get("id") ,(String) map.get("email"));
                    Member memberEntity = memberRepository.findByEmail((String) map.get("email")).orElseThrow(()->new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
                    PrincipalDetails principalDetails = new PrincipalDetails(memberEntity);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null,
                            principalDetails.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
    }
}
