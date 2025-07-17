package com.odinbook.accountservice.record;

public record AddFollowerRecord(Long followerId, Long followeeId, Boolean isFollowBack) {
}
