package seondays.shareticon.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CacheType {
    VOUCHER_IMAGE(
            "voucherImage",
            280,
            500
    );

    private final String cacheName;
    private final int expireAfterWrite;
    private final int maximumSize;
}