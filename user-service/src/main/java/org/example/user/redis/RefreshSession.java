package org.example.user.redis;

public record RefreshSession(
        String familyId,
        String currentJti,
        long rotatedAt
) {}
