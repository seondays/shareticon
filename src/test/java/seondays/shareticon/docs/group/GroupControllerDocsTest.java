package seondays.shareticon.docs.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import seondays.shareticon.docs.RestDocsSupport;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupController;
import seondays.shareticon.group.GroupService;
import seondays.shareticon.group.dto.ApplyToJoinRequest;
import seondays.shareticon.group.dto.ApplyToJoinResponse;
import seondays.shareticon.group.dto.ChangeGroupTitleAliasRequest;
import seondays.shareticon.group.dto.ChangeGroupTitleAliasResponse;
import seondays.shareticon.group.dto.CreateGroupRequest;
import seondays.shareticon.group.dto.GroupListResponse;
import seondays.shareticon.group.dto.GroupResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class GroupControllerDocsTest extends RestDocsSupport {

    private final GroupService groupService = mock(GroupService.class);

    @Override
    protected Object initController() {
        return new GroupController(groupService);
    }

    @Test
    @DisplayName("그룹을 새로 생성한다")
    void createGroupTest() throws Exception {
        //given
        Group createdGroup = Group.builder()
                .id(1L)
                .inviteCode("ABC")
                .build();

        CreateGroupRequest request = new CreateGroupRequest("그룹 이름");
        String json = objectMapper.writeValueAsString(request);

        when(groupService.createGroup(any(Long.class), any(CreateGroupRequest.class)))
                .thenReturn(GroupResponse.of(createdGroup));

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/group")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                                .with(authentication(auth))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(header().string("Location", "/group/1"))
                .andDo(document("group-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER)
                                        .description("생성된 그룹 ID"),
                                fieldWithPath("inviteCode").type(JsonFieldType.STRING)
                                        .description("생성된 그룹의 초대코드")
                        ),
                        responseHeaders(
                                headerWithName("Location").description("생성된 그룹 조회 URI")
                        )
                ));
    }

    @Test
    @DisplayName("유저의 모든 그룹 리스트를 조회한다")
    void getAllGroupList() throws Exception {
        //given
        GroupListResponse response1 = new GroupListResponse(1L, "title1", 2);
        GroupListResponse response2 = new GroupListResponse(2L, "title2", 2);
        List<GroupListResponse> responseList = List.of(response1, response2);

        when(groupService.getAllGroupList(any(Long.class))).thenReturn(responseList);

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/group")
                                .with(authentication(auth))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andDo(document("group-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[]").type(JsonFieldType.ARRAY)
                                        .description("조회된 그룹 리스트"),
                                fieldWithPath("[].groupId").type(JsonFieldType.NUMBER)
                                        .description("그룹 ID"),
                                fieldWithPath("[].groupTitleAlias").type(JsonFieldType.STRING)
                                        .description("그룹의 별칭"),
                                fieldWithPath("[].memberCount").type(JsonFieldType.NUMBER)
                                        .description("그룹에 참여중인 총 멤버의 수")
                        )
                ));
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
                                .with(authentication(auth))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(document("group-join",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("inviteCode").type(JsonFieldType.STRING)
                                        .description("가입하기를 원하는 그룹의 초대 코드"))
                ));
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
                                .with(authentication(auth))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andDo(document("group-joinList-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[]").type(JsonFieldType.ARRAY)
                                        .description("유저가 처리할 수 있는 그룹 가입 신청 리스트"),
                                fieldWithPath("[].applyUserId").type(JsonFieldType.NUMBER)
                                        .description("신청한 유저의 ID"),
                                fieldWithPath("[].pendingUserName").type(JsonFieldType.STRING)
                                        .description("신청한 유저의 닉네임"),
                                fieldWithPath("[].targetGroupId").type(JsonFieldType.NUMBER)
                                        .description("유저가 가입 신청한 그룹 ID")
                        )));
    }

    @Test
    @DisplayName("가입 신청 내역을 처리한다")
    void approvedJoinApplyStatus() throws Exception {
        //given
        Long userId = mockUser.getId();

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/group/{groupId}/user/{userId}", 1L, userId)
                                .queryParam("status", "APPROVED")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(authentication(auth))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(document("group-JoinList-approved",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("groupId").description("유저가 가입 신청을 한 대상 그룹 ID"),
                                parameterWithName("userId").description("가입 신청을 한 유저 ID")
                        ),
                        queryParameters(
                                parameterWithName("status").description(
                                        "처리할 상태값. (APPROVED: 승인, REJECTED: 거절)")
                        )));
    }

    @Test
    @DisplayName("그룹의 별칭을 변경한다")
    void changeGroupTitleAlias() throws Exception {
        //given
        Long groupId = 1L;
        String newTitleAlias = "그룹별칭";
        ChangeGroupTitleAliasRequest request = new ChangeGroupTitleAliasRequest(newTitleAlias);
        String json = objectMapper.writeValueAsString(request);

        ChangeGroupTitleAliasResponse response = new ChangeGroupTitleAliasResponse(groupId,
                newTitleAlias);

        when(groupService.changeGroupTitleAlias(
                any(Long.class), any(Long.class), any(ChangeGroupTitleAliasRequest.class)))
                .thenReturn(response);

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/group/{groupId}", groupId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                                .with(authentication(auth))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.groupId").value(groupId))
                .andExpect(jsonPath("$.titleAlias").value(newTitleAlias))
                .andDo(document("group-title-alias-change",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("groupId").description("별칭을 변경할 대상이 되는 그룹 ID")),
                        requestFields(
                                fieldWithPath("newGroupTitleAlias").type(JsonFieldType.STRING)
                                        .description("변경하기를 원하는 별칭")),
                        responseFields(
                                fieldWithPath("groupId").type(JsonFieldType.NUMBER)
                                        .description("별칭이 변경된 그룹 ID"),
                                fieldWithPath("titleAlias").type(JsonFieldType.STRING)
                                        .description("변경된 별칭")
                        )));

    }
}
