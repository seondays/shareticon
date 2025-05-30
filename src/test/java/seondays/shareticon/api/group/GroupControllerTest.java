package seondays.shareticon.api.group;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import seondays.shareticon.api.config.ControllerTestSupport;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.dto.ApplyToJoinRequest;
import seondays.shareticon.group.dto.ApplyToJoinResponse;
import seondays.shareticon.group.dto.CreateGroupRequest;
import seondays.shareticon.group.dto.GroupListResponse;
import seondays.shareticon.group.dto.GroupResponse;
import seondays.shareticon.login.CustomOAuth2User;
import seondays.shareticon.user.dto.UserOAuth2Dto;

public class GroupControllerTest extends ControllerTestSupport {

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        mockUser = new CustomOAuth2User(new UserOAuth2Dto(1L, "user", "ROLE_USER"));
    }

    @Test
    @DisplayName("그룹을 새로 생성한다")
    void createGroupTest() throws Exception {
        //given
        Group createdGroup = Group.builder()
                .id(1L)
                .build();

        when(groupService.createGroup(any(Long.class), any(CreateGroupRequest.class)))
                .thenReturn(GroupResponse.of(createdGroup));

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/group")
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(header().string("Location", "/group/1"));
    }

    @Test
    @DisplayName("유저의 모든 그룹 리스트를 조회한다")
    void getAllGroupList() throws Exception {
        //given
        GroupListResponse response1 = new GroupListResponse(1L, "title1");
        GroupListResponse response2 = new GroupListResponse(2L, "title2");
        List<GroupListResponse> responseList = List.of(response1, response2);

        when(groupService.getAllGroupList(any(Long.class))).thenReturn(responseList);

        //when //then
        mockMvc.perform(
                MockMvcRequestBuilders.get("/group")
                        .with(csrf())
                        .with(oauth2Login().oauth2User(mockUser))
        )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("그룹 가입을 신청한다")
    void applyToJoinGroup() throws Exception {
        //given
        ApplyToJoinRequest request = new ApplyToJoinRequest("inviteCode");
        String json = new ObjectMapper().writeValueAsString(request);

        //when //then
        mockMvc.perform(
                MockMvcRequestBuilders.post("/group/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf())
                        .with(oauth2Login().oauth2User(mockUser))
        )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("그룹 가입 신청 시 대상 그룹Id는 필수이다")
    void applyToJoinGroupWithoutGroupId() throws Exception {
        //given
        ApplyToJoinRequest request = new ApplyToJoinRequest("");
        String json = new ObjectMapper().writeValueAsString(request);

        //when //then
        mockMvc.perform(
                MockMvcRequestBuilders.post("/group/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf())
                        .with(oauth2Login().oauth2User(mockUser))
        )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("유저가 확인할 수 있는 가입 신청 리스트를 조회한다")
    void getAllApplyToJoinList() throws Exception {
        //given
        Long userId = mockUser.getId();
        String userName = mockUser.getName();
        ApplyToJoinResponse response1 = new ApplyToJoinResponse(userId, userName, 1L);
        ApplyToJoinResponse response2 = new ApplyToJoinResponse(userId, userName, 2L);
        List<ApplyToJoinResponse> responseList = List.of(response1, response2);

        when(groupService.getAllApplyToJoinList(any(Long.class))).thenReturn(responseList);

        //when //then
        mockMvc.perform(
                MockMvcRequestBuilders.get("/group/join")
                        .with(csrf())
                        .with(oauth2Login().oauth2User(mockUser))
        )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("가입 신청 내역을 승인한다")
    void approvedJoinApplyStatus() throws Exception {
        //given
        Long userId = mockUser.getId();

        //when //then
        mockMvc.perform(
                MockMvcRequestBuilders.patch("/group/{groupId}/user/{userId}", 1L, userId)
                        .param("status", "APPROVED")
                        .with(csrf())
                        .with(oauth2Login().oauth2User(mockUser))
        )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("가입 신청 내역을 거절한다")
    void rejectJoinApplyStatus() throws Exception {
        //given
        Long userId = mockUser.getId();

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/group/{groupId}/user/{userId}", 1L, userId)
                                .param("status", "REJECTED")
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("가입 신청 상태 변경 시, groupId는 Long 타입이어야 한다")
    void joinApplyStatusWithoutGroupIdLongType() throws Exception {
        //given
        Long userId = mockUser.getId();
        String groupId = "groupId";

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/group/{groupId}/user/{userId}", groupId, userId)
                                .param("status", "REJECTED")
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("가입 신청 상태 변경 시, userId는 Long 타입이어야 한다")
    void joinApplyStatusWithoutUserIdLongType() throws Exception {
        //given
        String userId = "1L";
        Long groupId = 1L;

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/group/{groupId}/user/{userId}", groupId, userId)
                                .param("status", "REJECTED")
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
