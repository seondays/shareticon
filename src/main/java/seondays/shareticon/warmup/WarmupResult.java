package seondays.shareticon.warmup;

public record WarmupResult(
        Long userId,
        Long groupId,
        int requestedPageSize,
        int warmedVoucherCount,
        boolean hasNext,
        long durationMs
) {
}
