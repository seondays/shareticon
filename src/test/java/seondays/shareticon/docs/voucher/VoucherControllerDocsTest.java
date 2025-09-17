package seondays.shareticon.docs.voucher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;
import seondays.shareticon.docs.RestDocsSupport;
import seondays.shareticon.group.Group;
import seondays.shareticon.user.User;
import seondays.shareticon.userGroup.UserGroup;
import seondays.shareticon.utils.SliceResponse;
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherController;
import seondays.shareticon.voucher.VoucherFilterCondition;
import seondays.shareticon.voucher.VoucherService;
import seondays.shareticon.voucher.VoucherStatus;
import seondays.shareticon.voucher.dto.CreateVoucherRequest;
import seondays.shareticon.voucher.dto.VoucherListResponse;
import seondays.shareticon.voucher.dto.VouchersResponse;

public class VoucherControllerDocsTest extends RestDocsSupport {

    private final VoucherService voucherService = mock(VoucherService.class);

    @Override
    protected Object initController() {
        return new VoucherController(voucherService);
    }

    @Test
    @DisplayName("신규 쿠폰을 생성한다")
    void registerVoucher() throws Exception {
        //given
        Long groupId = 1L;
        String voucherName = "voucher name";
        LocalDate expiration = LocalDate.of(2025, 1, 1);

        CreateVoucherRequest request = new CreateVoucherRequest(groupId, voucherName, expiration);
        String jsonRequest = objectMapper.writeValueAsString(request);

        MockMultipartFile imagePart = new MockMultipartFile(
                "image",
                "voucherImage.jpg",
                "image/jpeg",
                "voucherImage".getBytes()
        );

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                null,
                "application/json",
                jsonRequest.getBytes(StandardCharsets.UTF_8)
        );

        VouchersResponse mockResponse = new VouchersResponse(1L, "image", voucherName,
                1L, expiration, VoucherStatus.AVAILABLE, true);

        when(voucherService.register(
                any(CreateVoucherRequest.class), any(Long.class), any(MultipartFile.class)))
                .thenReturn(mockResponse);

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/vouchers")
                                .file(imagePart)
                                .file(requestPart)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(addBearerToken())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(header().string("Location", "/vouchers/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.presignedImage").value("image"))
                .andExpect(jsonPath("$.registeredUserId").value(1L))
                .andExpect(jsonPath("$.name").value(voucherName))
                .andExpect(jsonPath("$.expiration").value(
                        expiration.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .andDo(document("voucher-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParts(
                                partWithName("request").description("쿠폰 생성 request"),
                                partWithName("image").description("쿠폰 이미지 파일")
                        ),
                        requestPartFields("request",
                                fieldWithPath("groupId").type(JsonFieldType.NUMBER)
                                        .description("쿠폰을 저장할 그룹 ID"),
                                fieldWithPath("voucherName").type(JsonFieldType.STRING)
                                        .description("저장할 쿠폰의 이름"),
                                fieldWithPath("expiration").type(JsonFieldType.STRING)
                                        .description("저장할 쿠폰의 만료 기간")
                        ),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER)
                                        .description("생성된 쿠폰 ID"),
                                fieldWithPath("presignedImage").type(JsonFieldType.STRING)
                                        .description("이미지 URL"),
                                fieldWithPath("status").type(JsonFieldType.STRING)
                                        .description("쿠폰 상태 (AVAILABLE/EXPIRED/USED)"),
                                fieldWithPath("registeredUserId").type(JsonFieldType.NUMBER)
                                        .description("쿠폰을 등록한 유저의 ID"),
                                fieldWithPath("name").type(JsonFieldType.STRING)
                                        .description("쿠폰 등록자가 설정한 쿠폰의 이름"),
                                fieldWithPath("expiration").type(JsonFieldType.STRING)
                                        .description("쿠폰 등록자가 설정한 쿠폰의 만료 기간"),
                                fieldWithPath("isWishList").type(JsonFieldType.BOOLEAN)
                                        .description("쿠폰이 찜 되어 있는지의 여부")
                        )
                ));
    }

    @Test
    @DisplayName("쿠폰을 삭제한다")
    void deleteVoucher() throws Exception {
        //given
        Long groupId = 1L;
        Long voucherId = 1L;

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/vouchers/group/{groupId}/voucher/{voucherId}",
                                        groupId, voucherId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(addBearerToken())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(document("voucher-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("groupId").description("쿠폰이 속한 그룹 ID"),
                                parameterWithName("voucherId").description("삭제할 쿠폰 ID")
                        )));
    }

    @Test
    @DisplayName("필터 조건과 페이지 정보 모두 포함하여 그룹에 등록된 전체 쿠폰을 조회한다")
    void getAllVoucherInGroupPageInfoAndFilterCondition() throws Exception {
        //given
        Long groupId = 1L;
        Long userId = mockUser.getId();
        Long voucherId = 1L;
        Long cursorId = 1L;
        int pageSize = 1;
        boolean hasNext = false;
        String mockPresignedUrl = "mockPresignedUrl";

        User user = User.builder()
                .id(userId)
                .build();
        Voucher voucher = Voucher.builder()
                .id(voucherId)
                .image("www.image.com")
                .name("쿠폰 이름")
                .user(user)
                .expiration(LocalDate.of(2020, 1, 1))
                .status(VoucherStatus.AVAILABLE)
                .build();
        Group group = Group.builder()
                .id(groupId)
                .inviteCode("InviteCode")
                .build();
        UserGroup userGroup = UserGroup.builder()
                .user(user)
                .group(group)
                .groupTitleAlias("나의 그룹 이름")
                .build();
        VoucherListResponse mockResponse = VoucherListResponse.of(
                List.of(VouchersResponse.withWishList(voucher, mockPresignedUrl, false)),
                userGroup);

        SliceResponse<VoucherListResponse> mockSlice = SliceResponse.of(mockResponse, hasNext,
                pageSize);

        VoucherFilterCondition emptyCondition = VoucherFilterCondition.of(
                List.of(VoucherStatus.AVAILABLE),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));

        when(voucherService.getAllVoucher(eq(mockUser.getId()), eq(groupId), eq(cursorId),
                eq(pageSize), eq(emptyCondition))).thenReturn(mockSlice);

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/vouchers/{groupId}", groupId)
                                .param("cursorId", cursorId.toString())
                                .param("pageSize", String.valueOf(pageSize))
                                .param("voucherStatuses", "AVAILABLE")
                                .param("startDay", "2025-01-01")
                                .param("endDay", "2025-01-31")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(addBearerToken())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.size").value(pageSize))
                .andExpect(jsonPath("$.hasNext").value(hasNext))
                .andDo(document("voucher-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("groupId").description("쿠폰을 조회하고자 하는 그룹 ID")
                        ),
                        queryParameters(
                                parameterWithName("cursorId").description("커서 ID 값").optional(),
                                parameterWithName("pageSize").description("페이지 사이즈 값 (기본값 10)")
                                        .optional(),
                                parameterWithName("voucherStatuses").description(
                                        "조회할 쿠폰의 사용 상태 (AVAILABLE/EXPIRED/USED)").optional(),
                                parameterWithName("startDay").description(
                                        "만료일 기준으로 쿠폰을 조회하는 기간의 시작일 (yyyy-MM-dd)").optional(),
                                parameterWithName("endDay").description(
                                        "만료일 기준으로 쿠폰을 조회하는 기간의 종료일 (yyyy-MM-dd)").optional()
                        ),
                        responseFields(
                                fieldWithPath("content").type(JsonFieldType.ARRAY)
                                        .description("조회된 쿠폰 객체들의 배열"),
                                fieldWithPath("content[].groupTitle").type(JsonFieldType.STRING)
                                        .description("해당 쿠폰 객체가 속해있는 그룹의 사용자별 별칭"),
                                fieldWithPath("content[].groupInviteCode")
                                        .type(JsonFieldType.STRING)
                                        .description("해당 쿠폰 객체가 속해있는 그룹의 초대코드"),
                                fieldWithPath("content[].vouchers[].id").type(JsonFieldType.NUMBER)
                                        .description("쿠폰 ID"),
                                fieldWithPath("content[].vouchers[].presignedImage")
                                        .type(JsonFieldType.STRING).description("쿠폰 이미지 URL"),
                                fieldWithPath("content[].vouchers[].status")
                                        .type(JsonFieldType.STRING)
                                        .description("쿠폰 사용 상태 (AVAILABLE/EXPIRED/USED)"),
                                fieldWithPath("content[].vouchers[].registeredUserId")
                                        .type(JsonFieldType.NUMBER).description("쿠폰을 등록한 유저의 ID"),
                                fieldWithPath("content[].vouchers[].name")
                                        .type(JsonFieldType.STRING)
                                        .description("쿠폰 등록자가 설정한 쿠폰의 이름"),
                                fieldWithPath("content[].vouchers[].expiration")
                                        .type(JsonFieldType.STRING)
                                        .description("쿠폰 등록자가 설정한 쿠폰의 만료 기간"),
                                fieldWithPath("content[].vouchers[].status").type(
                                                JsonFieldType.STRING)
                                        .description("쿠폰의 현재 상태"),
                                fieldWithPath("content[].vouchers[].isWishList").type(
                                                JsonFieldType.BOOLEAN)
                                        .description("쿠폰이 찜 되어 있는지의 여부"),

                                fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN)
                                        .description("다음 페이지가 존재하는지 여부"),
                                fieldWithPath("size").type(JsonFieldType.NUMBER)
                                        .description("요청한 페이지의 사이즈")
                        )));

    }

    @Test
    @DisplayName("쿠폰의 상태를 변경한다")
    void changeVoucherStatus() throws Exception {
        //given
        Long groupId = 1L;
        Long voucherId = 1L;

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/api/vouchers/group/{groupId}/voucher/{voucherId}",
                                        groupId, voucherId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(addBearerToken())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(document("voucherStatus-change",
                        pathParameters(
                                parameterWithName("groupId").description("쿠폰이 속한 그룹 ID"),
                                parameterWithName("voucherId").description("변경할 쿠폰 ID")
                        )));
    }
}
