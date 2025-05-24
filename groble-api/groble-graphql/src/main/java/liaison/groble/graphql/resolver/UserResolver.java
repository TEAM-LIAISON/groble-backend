package liaison.groble.graphql.resolver;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import com.netflix.graphql.dgs.context.DgsContext;

import liaison.groble.application.user.service.UserService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.graphql.generated.types.*;
import lombok.RequiredArgsConstructor;

/**
 * GraphQL User Resolver
 * Netflix DGS를 사용한 GraphQL 구현
 */
@DgsComponent
@RequiredArgsConstructor
public class UserResolver {
    
    private final UserService userService;
    
    @DgsQuery
    public User me(@Auth Accessor accessor) {
        if (accessor == null) {
            return null;
        }
        
        var userDto = userService.getUserDetail(accessor.getUserId());
        return User.newBuilder()
            .id(userDto.getId().toString())
            .nickname(userDto.getNickname())
            .profileImageUrl(userDto.getProfileImageUrl())
            .userType(UserType.valueOf(userDto.getUserType()))
            .createdAt(userDto.getCreatedAt())
            .build();
    }
    
    @DgsQuery
    public MyPageSummary myPageSummary(@Auth Accessor accessor) {
        var summaryDto = userService.getUserMyPageSummary(accessor.getUserId());
        
        // 사용자 타입에 따라 다른 구현체 반환
        if (summaryDto.getUserType() == "BUYER") {
            return BuyerMyPageSummary.newBuilder()
                .nickname(summaryDto.getNickname())
                .profileImageUrl(summaryDto.getProfileImageUrl())
                .userType(UserType.BUYER)
                .canSwitchToSeller(summaryDto.isCanSwitchToSeller())
                .build();
        } else {
            return SellerMyPageSummary.newBuilder()
                .nickname(summaryDto.getNickname())
                .profileImageUrl(summaryDto.getProfileImageUrl())
                .userType(UserType.SELLER)
                .verificationStatus(
                    VerificationStatus.newBuilder()
                        .code(VerificationStatusCode.valueOf(summaryDto.getVerificationCode()))
                        .description(summaryDto.getVerificationDescription())
                        .build()
                )
                .build();
        }
    }
    
    @DgsMutation
    public SwitchRolePayload switchRole(
        @InputArgument("input") SwitchRoleInput input,
        @Auth Accessor accessor
    ) {
        try {
            boolean success = userService.switchUserType(
                accessor.getUserId(), 
                input.getTargetRole().name()
            );
            
            if (success) {
                var user = userService.getUserDetail(accessor.getUserId());
                return SwitchRolePayload.newBuilder()
                    .success(true)
                    .message("역할이 성공적으로 전환되었습니다.")
                    .user(mapToUser(user))
                    .build();
            } else {
                return SwitchRolePayload.newBuilder()
                    .success(false)
                    .message("역할 전환 조건을 충족하지 않습니다.")
                    .build();
            }
        } catch (Exception e) {
            return SwitchRolePayload.newBuilder()
                .success(false)
                .message(e.getMessage())
                .build();
        }
    }
}
