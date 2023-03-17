package com.jvnlee.catchdining.domain.user.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static io.jsonwebtoken.io.Decoders.*;

@Service
public class JwtService {

    private final Key SECRET_KEY;

    private final SignatureAlgorithm SIG_ALG;

    private final Long ACCESS_EXP;

    private final Long REFRESH_EXP;

    public JwtService(@Value("${jwt.secret}") String secretKey,
                      @Value("${jwt.alg}") String sigAlg,
                      @Value("${jwt.access.exp}") Long accessExp,
                      @Value("${jwt.refresh.exp}") Long refreshExp) {
        this.SECRET_KEY = Keys.hmacShaKeyFor(BASE64.decode(secretKey));
        this.SIG_ALG = SignatureAlgorithm.forName(sigAlg);
        this.ACCESS_EXP = accessExp;
        this.REFRESH_EXP = refreshExp;
    }

    public String createAccessToken(String username, Collection<? extends GrantedAuthority> authorities) {
        String authoritiesString = getAuthoritiesString(authorities);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(getExpDate(ACCESS_EXP))
                .claim("auth", authoritiesString)
                .signWith(SECRET_KEY, SIG_ALG)
                .compact();
    }

    public String createRefreshToken(Long id) {
        return Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(getExpDate(REFRESH_EXP))
                .claim("id", id.toString())
                .signWith(SECRET_KEY, SIG_ALG)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);

        if (claims.getSubject() == null) {
            throw new MalformedJwtException("토큰에 subject 정보가 존재하지 않습니다.");
        }

        if (claims.get("auth") == null) {
            throw new MalformedJwtException("토큰에 authorities 정보가 존재하지 않습니다.");
        }

        String username = claims.getSubject();
        List<SimpleGrantedAuthority> authorities = getAuthoritiesList(claims);
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    public boolean validateScheme(String scheme) {
        return scheme.equals("Bearer");
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getRefreshExp() {
        return this.REFRESH_EXP;
    }

    private String getAuthoritiesString(Collection<? extends GrantedAuthority> authorities) {
        return authorities
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    private List<SimpleGrantedAuthority> getAuthoritiesList(Claims claims) {
        return Arrays.stream(claims.get("auth").toString().split(",")).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    private Date getExpDate(Long expIn) {
        return new Date(System.currentTimeMillis() + expIn);
    }

}
