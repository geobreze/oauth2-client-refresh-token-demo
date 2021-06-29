# Purpose

This demo illustrates an infinite loop of token refreshes when refresh token is not accepted by OAuth2 backend.

Test

```
com.example.demo.DemoApplicationTests.shouldRefreshTokenInInfiniteLoopEvenWhenRefreshReturnsError
```

demonstrates this issue.
