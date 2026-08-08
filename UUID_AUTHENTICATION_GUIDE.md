# UUID Support in Authentication

## Summary of Changes

Your `userId` is now properly supported as a `UUID` type throughout the authentication system. Here's what was changed:

### 1. Created CustomUserDetails Class
**File:** `/workspace/src/main/java/com/tellinbox/tellinbox_api/config/security/CustomUserDetails.java`

A custom implementation of Spring Security's `UserDetails` that includes the user's UUID:

```java
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final UUID userId;  // <-- Now includes UUID
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    // ... other fields
}
```

### 2. Updated CustomUserDetailsService
**File:** `/workspace/src/main/java/com/tellinbox/tellinbox_api/config/security/CustomUserDetailsService.java`

Changed from using standard Spring User to CustomUserDetails:

```java
return CustomUserDetails.create(
    user.getId(),  // UUID from database
    user.getMobile(),
    user.getPasswordHash() != null ? user.getPasswordHash() : "",
    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
);
```

### 3. Updated JwtTokenProvider
**File:** `/workspace/src/main/java/com/tellinbox/tellinbox_api/config/security/JwtTokenProvider.java`

Now automatically includes userId in JWT tokens:

```java
public String generateAccessToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    
    // Include userId in claims if available
    if (userDetails instanceof CustomUserDetails customUserDetails) {
        claims.put("userId", customUserDetails.getUserId().toString());
    }
    
    return createToken(claims, userDetails.getUsername(), jwtExpirationMs);
}
```

### 4. Example Controller Usage
**File:** `/workspace/src/main/java/com/tellinbox/tellinbox_api/user/controller/ProfileController.java`

How to access the UUID in your controllers:

```java
@PostMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<ProfilePictureDto> uploadProfilePicture(
        @RequestParam("file") MultipartFile file,
        @AuthenticationPrincipal UserDetails userDetails) {
    
    // Get userId from CustomUserDetails (UUID type)
    UUID userId = null;
    if (userDetails instanceof CustomUserDetails customUserDetails) {
        userId = customUserDetails.getUserId();
    }
    
    String username = userDetails.getUsername();
    // Now you can use userId (UUID) for your business logic
    ProfilePictureDto result = profilePictureService.uploadProfilePicture(username, file);
    
    return new ResponseEntity<>(result, HttpStatus.OK);
}
```

## React Frontend Integration

When using React on the frontend:

1. **React sends JWT token** in Authorization header:
```javascript
axios.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

2. **Spring Security extracts token** via `JwtAuthenticationFilter`

3. **Token contains userId claim** (UUID as string)

4. **Controller accesses UUID** via `@AuthenticationPrincipal`:
```java
@AuthenticationPrincipal UserDetails userDetails
// Cast to CustomUserDetails to get userId
UUID userId = ((CustomUserDetails) userDetails).getUserId();
```

## Key Benefits

- ✅ `userId` is now `UUID` type (not `long`)
- ✅ Automatically included in JWT tokens
- ✅ Accessible in controllers via `@AuthenticationPrincipal`
- ✅ Type-safe with proper casting
- ✅ Works seamlessly with React frontend
- ✅ No need to manually parse tokens in controllers

## Alternative: Direct Token Extraction

If you prefer to extract userId directly from the token (as shown in your example):

```java
public ResponseEntity<QuestionDto> createQuestion(
        @RequestHeader("Authorization") String token, 
        @Valid @RequestBody CreateQuestionDto createQuestionDto) {
    
    // Extract token from "Bearer <token>"
    String jwt = token.substring(7);
    
    // Use JwtTokenProvider to get UUID
    UUID userId = jwtTokenProvider.getUserIdFromToken(jwt);
    
    QuestionDto createdQuestion = questionService.createQuestion(createQuestionDto, userId);
    return ResponseEntity.status(201).body(createdQuestion);
}
```

However, using `@AuthenticationPrincipal` is recommended as it:
- Provides better separation of concerns
- Automatically validates the token
- Gives you access to full user details
- Is the Spring Security best practice
