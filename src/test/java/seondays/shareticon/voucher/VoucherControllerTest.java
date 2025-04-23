package seondays.shareticon.voucher;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;
import seondays.shareticon.login.CustomOAuth2User;
import seondays.shareticon.user.dto.UserOAuth2Dto;
import seondays.shareticon.voucher.dto.CreateVoucherRequest;
import seondays.shareticon.voucher.dto.VouchersResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = VoucherController.class)
public class VoucherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VoucherService voucherService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomOAuth2User mockUser;

    @BeforeEach
    void setup() {
        mockUser = new CustomOAuth2User(new UserOAuth2Dto(1L, "user", "ROLE_USER"));
    }

    @Test
    @DisplayName("신규 쿠폰을 생성한다")
    void registerVoucher() throws Exception {
        //given
        CreateVoucherRequest request = new CreateVoucherRequest(1L);
        String jsonRequest = objectMapper.writeValueAsString(request);

        MockMultipartFile imagePart = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        // requestPart로 들어갈 JSON mock
        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                null,
                "application/json",
                jsonRequest.getBytes(StandardCharsets.UTF_8)
        );

        VouchersResponse mockResponse = new VouchersResponse(1L, "image", VoucherStatus.AVAILABLE);

        when(voucherService.register(
                any(CreateVoucherRequest.class), any(Long.class), any(MultipartFile.class)))
                .thenReturn(mockResponse);

        //when && //then
        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/vouchers")
                                .file(imagePart)
                                .file(requestPart)
                                .with(csrf())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(header().string("Location", "/vouchers/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("신규 쿠폰을 등록할 때 그룹 아이디는 필수이다")
    void registerVoucherWithNoGroupId() throws Exception {
        String emptyRequest = "{}";

        MockMultipartFile imagePart = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                null,
                "application/json",
                emptyRequest.getBytes(StandardCharsets.UTF_8)
        );

        VouchersResponse mockResponse = new VouchersResponse(1L, "image", VoucherStatus.AVAILABLE);

        when(voucherService.register(
                any(CreateVoucherRequest.class), any(Long.class), any(MultipartFile.class)))
                .thenReturn(mockResponse);

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/vouchers")
                                .file(imagePart)
                                .file(requestPart)
                                .with(csrf())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.message").value("그룹 ID를 포함해야 합니다"))
                .andExpect(jsonPath("$.code").value("400"));

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
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("쿠폰을 삭제할 때 그룹 아이디는 Long 타입이어야 한다")
    void deleteVoucherWithGroupIdNotLong() throws Exception {
        //given
        String groupId = "abc";
        Long voucherId = 1L;

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/vouchers/group/{groupId}/voucher/{voucherId}",
                                        groupId, voucherId)
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"));
    }

    @Test
    @DisplayName("쿠폰을 삭제할 때 쿠폰 아이디는 Long 타입이어야 한다")
    void deleteVoucherWithVoucherIdNotLong() throws Exception {
        //given
        String groupId = "abc";
        Long voucherId = 1L;

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/vouchers/group/{groupId}/voucher/{voucherId}",
                                        groupId, voucherId)
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"));
    }

    @Test
    @DisplayName("페이징을 포함하여 그룹에 등록된 전체 쿠폰을 조회한다")
    void getAllVoucherInGroup() throws Exception {
        //given
        Long groupId = 1L;
        Long cursorId = 1L;
        int pageSize = 1;

        Slice<VouchersResponse> mockSlice =
                new SliceImpl<>(Collections.emptyList(), PageRequest.of(0, pageSize), false);

        when(voucherService.getAllVoucher(eq(mockUser.getId()), eq(groupId), eq(cursorId), eq(pageSize)))
                .thenReturn(mockSlice);

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/vouchers/{groupId}", groupId)
                                .param("cursorId", cursorId.toString())
                                .param("pageSize", String.valueOf(pageSize))
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.pageable.pageSize").value(pageSize))
                .andExpect(jsonPath("$.size").value(pageSize));
    }

    @Test
    @DisplayName("페이징을 포함하지 않고 그룹에 등록된 전체 쿠폰을 조회한다")
    void getAllVoucherInGroupNoPageInfo() throws Exception {
        //given
        Long groupId = 1L;
        Long cursorId = null;
        int pageSize = 10;

        Slice<VouchersResponse> mockSlice =
                new SliceImpl<>(Collections.emptyList(), Pageable.ofSize(pageSize), false);

        when(voucherService.getAllVoucher(eq(mockUser.getId()), eq(groupId), eq(cursorId), eq(pageSize)))
                .thenReturn(mockSlice);

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/vouchers/{groupId}", groupId)
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.pageable.pageSize").value(pageSize))
                .andExpect(jsonPath("$.size").value(pageSize));
    }

    @Test
    @DisplayName("쿠폰의 상태를 변경한다")
    void changeVoucherStatus() throws Exception {
        //given
        Long groupId = 1L;
        Long voucherId = 1L;

        //when //then
        mockMvc.perform(
                MockMvcRequestBuilders.patch("/vouchers/group/{groupId}/voucher/{voucherId}", groupId, voucherId)
                        .with(csrf())
                        .with(oauth2Login().oauth2User(mockUser))
        )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("쿠폰의 상태를 변경할 때 그룹 아이디는 Long 이어야 한다")
    void changeVoucherStatusWithGroupIdNotLong() throws Exception {
        //given
        String groupId = "abcd";
        Long voucherId = 1L;

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/vouchers/group/{groupId}/voucher/{voucherId}", groupId, voucherId)
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"));
    }

    @Test
    @DisplayName("쿠폰의 상태를 변경할 때 쿠폰 아이디는 Long 이어야 한다")
    void changeVoucherStatusWithVoucherIdNotLong() throws Exception {
        //given
        Long groupId = 1L;
        String voucherId = "abcd";

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/vouchers/group/{groupId}/voucher/{voucherId}", groupId, voucherId)
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"));
    }
}
