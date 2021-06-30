# Purpose

This demo illustrates an infinite loop of token refreshes when refresh token is not accepted by OAuth2 backend.

Test

```
com.example.demo.DemoApplicationTests.shouldNotUseRefreshTokenWhenRefreshReturnsError
```

demonstrates this issue.

# Resolution

This test fails on Spring Boot `2.2.8.RELEASE` with Spring Security `5.2.5.RELEASE`. Seems to be working fine on Spring
Boot `2.5.2` which is running Spring Security `5.5.1`.
