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
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
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
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherController;
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
        CreateVoucherRequest request = new CreateVoucherRequest(1L);
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

        VouchersResponse mockResponse = new VouchersResponse(1L, "voucherImage",
                VoucherStatus.AVAILABLE);

        when(voucherService.register(
                any(CreateVoucherRequest.class), any(Long.class), any(MultipartFile.class)))
                .thenReturn(mockResponse);

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/vouchers")
                                .file(imagePart)
                                .file(requestPart)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(authentication(auth))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(header().string("Location", "/vouchers/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andDo(document("voucher-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParts(
                                partWithName("request").description("쿠폰 생성 request"),
                                partWithName("image").description("쿠폰 이미지 파일")
                        ),
                        requestPartFields("request",
                                fieldWithPath("groupId").type(JsonFieldType.NUMBER)
                                        .description("쿠폰을 저장할 그룹 ID")
                        ),
                        responseFields(

                                fieldWithPath("id").type(JsonFieldType.NUMBER)
                                        .description("생성된 쿠폰 ID"),
                                fieldWithPath("image").type(JsonFieldType.STRING)
                                        .description("이미지 URL"),
                                fieldWithPath("status").type(JsonFieldType.STRING)
                                        .description("쿠폰 상태 (AVAILABLE/EXPIRED/USED)")

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
                        MockMvcRequestBuilders.delete("/vouchers/group/{groupId}/voucher/{voucherId}",
                                        groupId, voucherId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(authentication(auth))
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
    @DisplayName("페이징을 포함하여 그룹에 등록된 전체 쿠폰을 조회한다")
    void getAllVoucherInGroup() throws Exception {
        //given
        Long groupId = 1L;
        Long userId = mockUser.getId();
        Long voucherId = 1L;
        Long cursorId = 1L;
        int pageSize = 1;

        Voucher voucher = Voucher.builder()
                .id(voucherId)
                .image("www.image.com")
                .status(VoucherStatus.AVAILABLE)
                .build();
        Group group = Group.builder()
                .id(groupId)
                .build();
        User user = User.builder()
                .id(userId)
                .build();
        UserGroup userGroup = UserGroup.builder()
                .user(user)
                .group(group)
                .groupTitleAlias("나의 그룹 이름")
                .build();
        List<VoucherListResponse> mockResponse = List.of(
                VoucherListResponse.of(List.of(VouchersResponse.of(voucher)), userGroup));

        Slice<VoucherListResponse> mockSlice =
                new SliceImpl<>(mockResponse, PageRequest.of(0, pageSize), false);

        when(voucherService.getAllVoucher(eq(mockUser.getId()), eq(groupId), eq(cursorId),
                eq(pageSize)))
                .thenReturn(mockSlice);

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/vouchers/{groupId}", groupId)
                                .param("cursorId", cursorId.toString())
                                .param("pageSize", String.valueOf(pageSize))
                                .with(authentication(auth))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.pageable.pageSize").value(pageSize))
                .andExpect(jsonPath("$.size").value(pageSize))
                .andExpect(jsonPath("$.content[0].vouchers").isArray())
                .andDo(document("voucher-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("groupId").description("쿠폰을 조회하고자 하는 그룹 ID")
                        ),
                        queryParameters(
                                parameterWithName("cursorId").description("커서 ID 값").optional(),
                                parameterWithName("pageSize").description("페이지 사이즈 값 (기본값 10)")
                                        .optional()
                        ),
                        responseFields(
                                fieldWithPath("content").type(JsonFieldType.ARRAY)
                                        .description("조회된 쿠폰 객체들의 배열"),
                                fieldWithPath("content[].groupId").type(JsonFieldType.NUMBER)
                                        .description("해당 쿠폰 객체가 속해있는 그룹 ID"),
                                fieldWithPath("content[].groupTitle").type(JsonFieldType.STRING)
                                        .description("해당 쿠폰 객체가 속해있는 그룹의 사용자별 별칭"),
                                fieldWithPath("content[].vouchers[].id").type(JsonFieldType.NUMBER)
                                        .description("쿠폰 ID"),
                                fieldWithPath("content[].vouchers[].image").type(
                                                JsonFieldType.STRING)
                                        .description("쿠폰 이미지 URL"),
                                fieldWithPath("content[].vouchers[].status").type(
                                                JsonFieldType.STRING)
                                        .description("쿠폰 사용 상태"),

                                subsectionWithPath("pageable").type(JsonFieldType.OBJECT)
                                        .description("페이지네이션 요청 정보"),
                                fieldWithPath("pageable.pageNumber").type(JsonFieldType.NUMBER)
                                        .description("현재 페이지 번호"),
                                fieldWithPath("pageable.pageSize").type(JsonFieldType.NUMBER)
                                        .description("요청된 페이지 크기"),
                                subsectionWithPath("pageable.sort").type(JsonFieldType.OBJECT)
                                        .description("요청된 정렬 정보"),
                                fieldWithPath("pageable.sort.empty").type(JsonFieldType.BOOLEAN)
                                        .description("정렬 기준 존재 여부"),
                                fieldWithPath("pageable.sort.sorted").type(JsonFieldType.BOOLEAN)
                                        .description("정렬이 적용되었는지 여부"),
                                fieldWithPath("pageable.sort.unsorted").type(JsonFieldType.BOOLEAN)
                                        .description("정렬이 적용되지 않았는지 여부"),
                                fieldWithPath("pageable.offset").type(JsonFieldType.NUMBER)
                                        .description("요청 오프셋"),
                                fieldWithPath("pageable.paged").type(JsonFieldType.BOOLEAN)
                                        .description("페이지네이션이 적용되었는지 여부"),
                                fieldWithPath("pageable.unpaged").type(JsonFieldType.BOOLEAN)
                                        .description("페이지네이션이 적용되지 않았는지 여부"),

                                fieldWithPath("size").type(JsonFieldType.NUMBER)
                                        .description("페이지 크기"),
                                fieldWithPath("number").type(JsonFieldType.NUMBER)
                                        .description("현재 페이지 번호"),

                                subsectionWithPath("sort").type(JsonFieldType.OBJECT)
                                        .description("실제 적용된 정렬 정보"),
                                fieldWithPath("sort.empty").type(JsonFieldType.BOOLEAN)
                                        .description("정렬 기준 존재 여부"),
                                fieldWithPath("sort.sorted").type(JsonFieldType.BOOLEAN)
                                        .description("정렬이 적용되었는지 여부"),
                                fieldWithPath("sort.unsorted").type(JsonFieldType.BOOLEAN)
                                        .description("정렬이 적용되지 않았는지 여부"),

                                fieldWithPath("first").type(JsonFieldType.BOOLEAN)
                                        .description("첫 페이지인지 여부"),
                                fieldWithPath("last").type(JsonFieldType.BOOLEAN)
                                        .description("마지막 페이지인지 여부"),
                                fieldWithPath("numberOfElements").type(JsonFieldType.NUMBER)
                                        .description("현재 페이지에 실제 포함된 요소 개수"),
                                fieldWithPath("empty").type(JsonFieldType.BOOLEAN)
                                        .description("현재 페이지가 비어 있는지 여부")
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
                        MockMvcRequestBuilders.patch("/vouchers/group/{groupId}/voucher/{voucherId}",
                                        groupId, voucherId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(authentication(auth))
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
