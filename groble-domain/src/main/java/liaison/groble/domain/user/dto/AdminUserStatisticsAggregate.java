package liaison.groble.domain.user.dto;

public record AdminUserStatisticsAggregate(
    long totalActiveUsers,
    long withdrawnUsers,
    long newUsers7Days,
    long newUsers30Days,
    long buyerOnlyCount,
    long buyerAndSellerCount,
    long marketingAgreedCount,
    long phoneNumberProvidedCount,
    long sellerTermsAgreedCount,
    long verificationVerifiedCount,
    long verificationPendingCount,
    long verificationInProgressCount,
    long verificationFailedCount,
    long verificationNoneCount,
    long businessTypeIndividualSimplifiedCount,
    long businessTypeIndividualNormalCount,
    long businessTypeCorporateCount,
    long businessTypeIndividualMakerCount,
    long businessTypeNoneCount) {}
