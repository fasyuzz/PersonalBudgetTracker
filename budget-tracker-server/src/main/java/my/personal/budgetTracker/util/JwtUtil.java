package my.personal.budgetTracker.util;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    private static final String SECRET = 
    "mySuperSecretKeyForJwtGenerationThatShouldBeLongEnoughToBeSecure";

    public String generateToken(){
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        return Jwts.builder()
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
