package com.odinbook.accountservice.record;

import java.sql.Date;
import java.util.List;

public record NotifyAccountsRecord(Long id,
                                   Long accountId,
                                   Long postAccountId,
                                   Long postId,
                                   Boolean isShared,
                                   Boolean isVisibleToFollowers,
                                   Boolean friendsVisibilityType,
                                   List<Long> visibleToFriendList,
                                   List<Long> notifyAccountList) {
}
