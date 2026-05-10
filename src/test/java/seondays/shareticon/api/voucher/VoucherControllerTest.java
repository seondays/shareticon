package seondays.shareticon.api.voucher;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.multipart.MultipartFile;
import seondays.shareticon.api.config.ControllerTestSupport;
import seondays.shareticon.group.Group;
import seondays.shareticon.login.CustomOAuth2User;
import seondays.shareticon.user.User;
import seondays.shareticon.user.dto.UserOAuth2Dto;
import seondays.shareticon.userGroup.UserGroup;
import seondays.shareticon.utils.SliceResponse;
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherFilterCondition;
import seondays.shareticon.voucher.VoucherStatus;
import seondays.shareticon.voucher.dto.CreateVoucherRequest;
import seondays.shareticon.voucher.dto.VoucherListResponse;
import seondays.shareticon.voucher.dto.VouchersResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class VoucherControllerTest extends ControllerTestSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockUser = new CustomOAuth2User(new UserOAuth2Dto(1L, "user", "ROLE_USER"));
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

        VouchersResponse mockResponse = new VouchersResponse(1L, "image", voucherName,
                1L, expiration, VoucherStatus.AVAILABLE, false);

        when(voucherService.register(
                any(CreateVoucherRequest.class), any(Long.class), any(MultipartFile.class)))
                .thenReturn(mockResponse);

        //when && //then
        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/vouchers")
                                .file(imagePart)
                                .file(requestPart)
                                .with(csrf())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/vouchers/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.presignedImage").value("image"))
                .andExpect(jsonPath("$.name").value(voucherName))
                .andExpect(jsonPath("$.expiration").value(
                        expiration.format(DateTimeFormatter.ISO_LOCAL_DATE)));
    }

    @Test
    @DisplayName("신규 쿠폰을 등록할 때 그룹 아이디는 필수이다")
    void registerVoucherWithNoGroupId() throws Exception {
        Long groupId = null;
        String voucherName = "voucher name";
        LocalDate expiration = LocalDate.of(2025, 1, 1);

        CreateVoucherRequest request = new CreateVoucherRequest(groupId, voucherName, expiration);
        String jsonRequestWithoutGroupId = objectMapper.writeValueAsString(request);

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
                jsonRequestWithoutGroupId.getBytes(StandardCharsets.UTF_8)
        );

        VouchersResponse mockResponse = new VouchersResponse(1L, "image", voucherName,
                1L, expiration, VoucherStatus.AVAILABLE, false);

        when(voucherService.register(
                any(CreateVoucherRequest.class), any(Long.class), any(MultipartFile.class)))
                .thenReturn(mockResponse);

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/vouchers")
                                .file(imagePart)
                                .file(requestPart)
                                .with(csrf())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("그룹 ID를 포함해야 합니다"))
                .andExpect(jsonPath("$.code").value("400"));

    }

    @Test
    @DisplayName("신규 쿠폰을 생성할 때 쿠폰 이름은 필수이다")
    void registerVoucherWithNoVoucherName() throws Exception {
        Long groupId = 1L;
        String voucherName = "";
        LocalDate expiration = LocalDate.of(2025, 1, 1);

        CreateVoucherRequest request = new CreateVoucherRequest(groupId, voucherName, expiration);
        String jsonRequestWithoutGroupId = objectMapper.writeValueAsString(request);

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
                jsonRequestWithoutGroupId.getBytes(StandardCharsets.UTF_8)
        );

        VouchersResponse mockResponse = new VouchersResponse(1L, "image", voucherName,
                1L, expiration, VoucherStatus.AVAILABLE, false);

        when(voucherService.register(
                any(CreateVoucherRequest.class), any(Long.class), any(MultipartFile.class)))
                .thenReturn(mockResponse);

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/vouchers")
                                .file(imagePart)
                                .file(requestPart)
                                .with(csrf())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("쿠폰 이름을 포함해야 합니다"))
                .andExpect(jsonPath("$.code").value("400"));

    }

    @Test
    @DisplayName("신규 쿠폰을 생성할 때 쿠폰의 만료 일자는 필수이다")
    void registerVoucherWithNoVoucherExpiration() throws Exception {
        Long groupId = 1L;
        String voucherName = "voucher name";
        LocalDate expiration = null;

        CreateVoucherRequest request = new CreateVoucherRequest(groupId, voucherName, expiration);
        String jsonRequestWithoutGroupId = objectMapper.writeValueAsString(request);

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
                jsonRequestWithoutGroupId.getBytes(StandardCharsets.UTF_8)
        );

        VouchersResponse mockResponse = new VouchersResponse(1L, "image", voucherName,
                1L, expiration, VoucherStatus.AVAILABLE, false);

        when(voucherService.register(
                any(CreateVoucherRequest.class), any(Long.class), any(MultipartFile.class)))
                .thenReturn(mockResponse);

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/vouchers")
                                .file(imagePart)
                                .file(requestPart)
                                .with(csrf())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("쿠폰의 만료 일자를 포함해야 합니다"))
                .andExpect(jsonPath("$.code").value("400"));

    }

    @Test
    @DisplayName("신규 쿠폰을 생성할 때 생성 날짜보다 이전 날짜를 만료 일자로 지정할 수 없다")
    void registerVoucherWithNoVoucherExpirationBeforeToday() throws Exception {
        Long groupId = 1L;
        String voucherName = "voucher name";
        LocalDate expiration = LocalDate.of(2024, 11, 30);

        CreateVoucherRequest request = new CreateVoucherRequest(groupId, voucherName, expiration);
        String jsonRequestWithoutGroupId = objectMapper.writeValueAsString(request);

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
                jsonRequestWithoutGroupId.getBytes(StandardCharsets.UTF_8)
        );

        VouchersResponse mockResponse = new VouchersResponse(1L, "image", voucherName,
                1L, expiration, VoucherStatus.AVAILABLE, false);

        when(voucherService.register(
                any(CreateVoucherRequest.class), any(Long.class), any(MultipartFile.class)))
                .thenReturn(mockResponse);

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/vouchers")
                                .file(imagePart)
                                .file(requestPart)
                                .with(csrf())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(result -> {
                    Exception resolvedException = result.getResolvedException();
                    resolvedException.printStackTrace();
                })
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("쿠폰의 만료 일자는 오늘보다 이전 날짜로 지정할 수 없습니다"))
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
                        MockMvcRequestBuilders.delete("/api/vouchers/group/{groupId}/voucher/{voucherId}",
                                        groupId, voucherId)
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("쿠폰을 삭제할 때 그룹 아이디는 Long 타입이어야 한다")
    void deleteVoucherWithGroupIdNotLong() throws Exception {
        //given
        String groupId = "abc";
        Long voucherId = 1L;

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/vouchers/group/{groupId}/voucher/{voucherId}",
                                        groupId, voucherId)
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
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
                        MockMvcRequestBuilders.delete("/api/vouchers/group/{groupId}/voucher/{voucherId}",
                                        groupId, voucherId)
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"));
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
                .user(user)
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
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.size").value(pageSize))
                .andExpect(jsonPath("$.hasNext").value(hasNext));

    }

    @Test
    @DisplayName("필터 조건은 없고, 페이지 정보는 포함해서 그룹에 등록된 전체 쿠폰을 조회한다")
    void getAllVoucherInGroup() throws Exception {
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
                .user(user)
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

        VoucherFilterCondition mockFilterCondition = VoucherFilterCondition.of(null, null, null);

        when(voucherService.getAllVoucher(eq(mockUser.getId()), eq(groupId), eq(cursorId),
                eq(pageSize), eq(mockFilterCondition))).thenReturn(mockSlice);

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/vouchers/{groupId}", groupId)
                                .param("cursorId", cursorId.toString())
                                .param("pageSize", String.valueOf(pageSize))
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.size").value(pageSize))
                .andExpect(jsonPath("$.hasNext").value(hasNext));
    }

    @Test
    @DisplayName("페이지 조건은 없고, 필터 조건은 포함하여 그룹에 등록된 전체 쿠폰을 조회한다")
    void getAllVoucherInGroupNoPageInfoAndFilterCondition() throws Exception {
        //given
        Long groupId = 1L;
        Long cursorId = null;
        int pageSize = 10;
        Long userId = mockUser.getId();
        Long voucherId = 1L;
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
                                .param("voucherStatuses", "AVAILABLE")
                                .param("startDay", "2025-01-01")
                                .param("endDay", "2025-01-31")
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.size").value(pageSize))
                .andExpect(jsonPath("$.hasNext").value(hasNext));

    }

    @Test
    @DisplayName("startDay와 endDay가 yyyy-MM-dd 포맷이 아닐 경우 예외가 발생한다")
    void getAllVoucherInGroupWithDateFormat() throws Exception {
        //given
        Long groupId = 1L;

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/vouchers/{groupId}", groupId)
                                .param("startDay", "2025/01/01")
                                .param("endDay", "2025/01/31")
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"));

    }

    @ParameterizedTest
    @ValueSource(longs = {0L, 21L})
    @DisplayName("전체 쿠폰 조회 시의 페이지 사이즈는 1 ~ 20 사이여야 한다")
    void getAllVoucherInGroupWithPageSize(Long pageSize) throws Exception {
        //given
        Long groupId = 1L;
        
        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/vouchers/{groupId}", groupId)
                                .param("pageSize", String.valueOf(pageSize))
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("필터 조건과 페이지 정보 모두 없이 그룹에 등록된 전체 쿠폰을 조회한다")
    void getAllVoucherInGroupNoPageInfoAndNoFilterCondition() throws Exception {
        //given
        Long groupId = 1L;
        Long cursorId = null;
        int pageSize = 10;
        Long userId = mockUser.getId();
        Long voucherId = 1L;
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

        VoucherFilterCondition emptyCondition = VoucherFilterCondition.of(null, null, null);

        when(voucherService.getAllVoucher(eq(mockUser.getId()), eq(groupId), eq(cursorId),
                eq(pageSize), eq(emptyCondition))).thenReturn(mockSlice);

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/vouchers/{groupId}", groupId)
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.hasNext").value(hasNext))
                .andExpect(jsonPath("$.size").value(pageSize));
    }

    @Test
    @DisplayName("쿠폰을 사용 완료 상태로 변경한다")
    void markVoucherAsUsed() throws Exception {
        //given
        Long groupId = 1L;
        Long voucherId = 1L;

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/vouchers/group/{groupId}/voucher/{voucherId}/used",
                                        groupId, voucherId)
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("쿠폰을 사용 가능 상태로 변경한다")
    void markVoucherAsAvailable() throws Exception {
        //given
        Long groupId = 1L;
        Long voucherId = 1L;

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/vouchers/group/{groupId}/voucher/{voucherId}/available",
                                        groupId, voucherId)
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("쿠폰을 사용 완료 상태로 변경할 때 그룹 아이디는 Long 이어야 한다")
    void markVoucherAsUsedWithGroupIdNotLong() throws Exception {
        //given
        String groupId = "abcd";
        Long voucherId = 1L;

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/vouchers/group/{groupId}/voucher/{voucherId}/used",
                                        groupId, voucherId)
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"));
    }

    @Test
    @DisplayName("쿠폰을 사용 가능 상태로 변경할 때 쿠폰 아이디는 Long 이어야 한다")
    void markVoucherAsAvailableWithVoucherIdNotLong() throws Exception {
        //given
        Long groupId = 1L;
        String voucherId = "abcd";

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/vouchers/group/{groupId}/voucher/{voucherId}/available",
                                        groupId, voucherId)
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"));
    }
}
