package seondays.shareticon.api.voucher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import seondays.shareticon.api.config.IntegrationTestSupport;
import seondays.shareticon.exception.GroupNotFoundException;
import seondays.shareticon.exception.IllegalVoucherImageException;
import seondays.shareticon.exception.ImageUploadException;
import seondays.shareticon.exception.InvalidAccessException;
import seondays.shareticon.exception.InvalidVoucherDeleteException;
import seondays.shareticon.exception.UserNotFoundException;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;
import seondays.shareticon.group.JoinStatus;
import seondays.shareticon.image.ImageService;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.userGroup.UserGroup;
import seondays.shareticon.userGroup.UserGroupRepository;
import seondays.shareticon.utils.SliceResponse;
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherFilterCondition;
import seondays.shareticon.voucher.VoucherRepository;
import seondays.shareticon.voucher.VoucherService;
import seondays.shareticon.voucher.VoucherStatus;
import seondays.shareticon.voucher.dto.CreateVoucherRequest;
import seondays.shareticon.voucher.dto.VoucherListResponse;
import seondays.shareticon.voucher.dto.VouchersResponse;

class VoucherServiceTest extends IntegrationTestSupport {

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @MockitoBean
    protected ImageService imageService;

    private Instant testSystemTimeInstant;

    @BeforeEach
    void setUp() {
        testSystemTimeInstant = Instant.parse("2024-12-01T00:00:00Z");
        when(clock.instant()).thenReturn(testSystemTimeInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("Asia/Seoul"));
    }

    @AfterEach
    void tearDown() {
        userGroupRepository.deleteAllInBatch();
        voucherRepository.deleteAllInBatch();
        groupRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("쿠폰 이미지 파일과 유저그룹 정보를 가지고 쿠폰을 등록한다.")
    void voucherRegisterWithImage() {
        //given
        User user = User.builder()
                .build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        String userGroupAlias = "그룹 별칭";
        linkUserWithGroup(user, group, userGroupAlias);

        String voucherName = "voucher name";
        LocalDate expiration = LocalDate.of(2025, 1, 1);
        CreateVoucherRequest request = new CreateVoucherRequest(group.getId(), voucherName,
                expiration);

        MockMultipartFile mockImage = new MockMultipartFile(
                "test",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        //when
        given(imageService.uploadImageWithRetry(any()))
                .willReturn("https://test/test.jpg");

        given(imageService.getPresignedImageUrl(any(), any())).willReturn("presignedImageUrl");

        VouchersResponse response = voucherService.register(request, user.getId(), mockImage);

        //then
        Voucher voucher = voucherRepository.findById(response.id())
                .orElseThrow();

        assertThat(response).isNotNull();
        assertThat(voucher.getUser().getId()).isEqualTo(user.getId());
        assertThat(voucher.getGroup().getId()).isEqualTo(request.groupId());
        assertThat(voucher.getImage()).isEqualTo("https://test/test.jpg");
    }

    @Test
    @DisplayName("쿠폰 등록시 첨부 파일이 이미지 파일이 아니라면 예외가 발생한다.")
    void voucherRegisterWithNoImage() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        String userGroupAlias = "그룹 별칭";
        linkUserWithGroup(user, group, userGroupAlias);

        String voucherName = "voucher name";
        LocalDate expiration = LocalDate.of(2025, 1, 1);
        CreateVoucherRequest request = new CreateVoucherRequest(group.getId(), voucherName,
                expiration);

        MockMultipartFile mockImage = new MockMultipartFile(
                "test",
                "test.jpg",
                "application/json",
                "test".getBytes()
        );

        //when //then
        assertThatThrownBy(
                () -> voucherService.register(request, user.getId(), mockImage)).isInstanceOf(
                IllegalVoucherImageException.class);
    }

    @Test
    @DisplayName("그룹에 참여하지 않은 사용자가 해당 그룹에 속하는 쿠폰을 등록하면 예외가 발생한다.")
    void registerVoucherWithNoGroup() {
        //given
        User user = User.builder()
                .build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        String voucherName = "voucher name";
        LocalDate expiration = LocalDate.of(2025, 1, 1);
        CreateVoucherRequest request = new CreateVoucherRequest(group.getId(), voucherName,
                expiration);

        MockMultipartFile mockImage = new MockMultipartFile(
                "test",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        //when //then
        assertThatThrownBy(
                () -> voucherService.register(request, user.getId(), mockImage)).isInstanceOf(
                InvalidAccessException.class);

    }

    @Test
    @DisplayName("존재하지 않는 그룹에 쿠폰을 등록하는 경우 예외가 발생한다.")
    void registerVoucherWithNoExistGroup() {
        //given
        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        Long noExistUserId = 1L;
        String voucherName = "voucher name";
        LocalDate expiration = LocalDate.of(2025, 1, 1);
        CreateVoucherRequest request = new CreateVoucherRequest(group.getId(), voucherName,
                expiration);

        MockMultipartFile mockImage = new MockMultipartFile(
                "test",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        //when //then
        assertThatThrownBy(
                () -> voucherService.register(request, noExistUserId, mockImage)).isInstanceOf(
                UserNotFoundException.class);

    }

    @Test
    @DisplayName("존재하지 않는 그룹에 쿠폰을 등록하는 경우 예외가 발생한다.")
    void registerVoucherWithNoExistUser() {
        //given
        User user = User.builder()
                .build();
        userRepository.save(user);

        Long noExistGroupId = 1L;
        String voucherName = "voucher name";
        LocalDate expiration = LocalDate.of(2025, 1, 1);
        CreateVoucherRequest request = new CreateVoucherRequest(noExistGroupId, voucherName,
                expiration);

        MockMultipartFile mockImage = new MockMultipartFile(
                "test",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        //when //then
        assertThatThrownBy(
                () -> voucherService.register(request, user.getId(), mockImage)).isInstanceOf(
                GroupNotFoundException.class);

    }

    @Test
    @DisplayName("쿠폰 등록 시 이미지 업로드가 실패하는 경우 예외가 발생하고 Voucher도 롤백된다.")
    void registerVoucherWhenImageUploadFail() {
        //given
        User user = User.builder()
                .build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        String userGroupAlias = "그룹 별칭";
        linkUserWithGroup(user, group, userGroupAlias);

        String voucherName = "voucher name";
        LocalDate expiration = LocalDate.of(2025, 1, 1);
        CreateVoucherRequest request = new CreateVoucherRequest(group.getId(), voucherName,
                expiration);

        MockMultipartFile mockImage = new MockMultipartFile(
                "test",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        //when //then
        given(imageService.uploadImageWithRetry(any()))
                .willThrow(ImageUploadException.class);

        assertThatThrownBy(
                () -> voucherService.register(request, user.getId(), mockImage)).isInstanceOf(
                ImageUploadException.class);

        List<Voucher> vouchers = voucherRepository.findAll();
        assertThat(vouchers).isEmpty();
    }

    @Test
    @DisplayName("쿠폰을 등록한 사용자는 쿠폰을 삭제할 수 있다")
    void deleteWithRegisterUser() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        String userGroupAlias = "그룹 별칭";
        linkUserWithGroup(user, group, userGroupAlias);

        String voucherName = "voucher name";
        LocalDate expiration = LocalDate.of(2025, 1, 1);

        Voucher voucher = Voucher.createNewVoucher(user, group, voucherName, "imageKey",
                expiration);
        voucherRepository.save(voucher);

        //when
        Long startTime = System.nanoTime();
        voucherService.delete(user.getId(), group.getId(), voucher.getId());
        Long endTime = System.nanoTime();
        long resultTime = startTime - endTime;
        System.out.println("소요시간" + resultTime + "나노초");
        //then
        Optional<Voucher> result = voucherRepository.findById(voucher.getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("쿠폰을 등록하지 않은 사람이 쿠폰을 삭제하면 예외가 발생한다")
    void deleteVoucherWithNotRegisterUser() {
        //given
        User registerUser = User.builder().build();
        User notRegisterUser = User.builder().build();
        userRepository.saveAll(List.of(registerUser, notRegisterUser));

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        String userGroupAlias = "그룹 별칭";
        linkUserWithGroup(registerUser, group, userGroupAlias);
        linkUserWithGroup(notRegisterUser, group, userGroupAlias);

        String voucherName = "voucher name";
        LocalDate expiration = LocalDate.of(2025, 1, 1);
        Voucher voucher = Voucher.createNewVoucher(registerUser, group, voucherName,
                "imageKey", expiration);
        voucherRepository.save(voucher);

        //when //then
        assertThatThrownBy(() ->
                voucherService.delete(notRegisterUser.getId(), group.getId(), voucher.getId()))
                .isInstanceOf(InvalidVoucherDeleteException.class);
    }

    @Test
    @DisplayName("그룹에 속해있지 않은 사용자가 쿠폰을 삭제하는 경우 예외가 발생한다")
    void deleteVoucherWithUserNoExistInGroup() {
        //given
        User userExistInGroup = User.builder().build();
        User userNoExistInGroup = User.builder().build();
        userRepository.save(userExistInGroup);
        userRepository.save(userNoExistInGroup);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        String userGroupAlias = "그룹 별칭";
        linkUserWithGroup(userExistInGroup, group, userGroupAlias);

        String voucherName = "voucher name";
        LocalDate expiration = LocalDate.of(2025, 1, 1);
        Voucher voucher = Voucher.createNewVoucher(userExistInGroup, group, voucherName,
                "imageKey", expiration);
        voucherRepository.save(voucher);

        //when //then
        assertThatThrownBy(() ->
                voucherService.delete(userNoExistInGroup.getId(), group.getId(), voucher.getId()))
                .isInstanceOf(InvalidAccessException.class);
    }

    @Test
    @Transactional
    @DisplayName("그룹에 속해있는 사용자가 쿠폰을 조회하는 경우 전체 쿠폰 결과를 담은 slice를 반환한다")
    void getAllVoucherWithUserExistInGroup() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        String userGroupAlias = "그룹 별칭";
        UserGroup userGroup = linkUserWithGroup(user, group, userGroupAlias);

        String voucherName = "voucher name";
        LocalDate expiration = LocalDate.of(2025, 1, 1);
        Voucher voucher1 = Voucher.createNewVoucher(user, group, voucherName, "imageKey",
                expiration);
        Voucher voucher2 = Voucher.createNewVoucher(user, group, voucherName, "imageKey",
                expiration);
        Voucher voucher3 = Voucher.createNewVoucher(user, group, voucherName, "imageKey",
                expiration);

        voucherRepository.saveAll(List.of(voucher1, voucher2, voucher3));

        LocalDate searchStart = LocalDate.of(2024, 12, 10);
        LocalDate searchEnd = LocalDate.of(2025, 1, 10);
        VoucherFilterCondition voucherFilterCondition = VoucherFilterCondition.of(
                VoucherStatus.forDisplayVoucherStatus(), searchStart, searchEnd);

        //when
        String preSignedImageUrl = "presignedImageUrlResult";
        given(imageService.getPresignedImageUrl(any(), any())).willReturn(preSignedImageUrl);

        SliceResponse<VoucherListResponse> allVoucher = voucherService.getAllVoucher(user.getId(),
                group.getId(), null, 3, voucherFilterCondition);

        VoucherListResponse voucherListResponse = allVoucher.getContent().get(0);

        //then
        assertThat(allVoucher).isNotNull();
        assertThat(allVoucher.getSize()).isEqualTo(3);
        assertThat(allVoucher.getContent().size()).isEqualTo(1);

        assertThat(voucherListResponse.groupTitle()).isEqualTo(userGroup.getGroupTitleAlias());
        assertThat(voucherListResponse.vouchers())
                .extracting("id", "presignedImage", "status")
                .contains(
                        tuple(voucher1.getId(), preSignedImageUrl, voucher1.getStatus()),
                        tuple(voucher2.getId(), preSignedImageUrl, voucher2.getStatus()),
                        tuple(voucher3.getId(), preSignedImageUrl, voucher3.getStatus())
                );

    }

    @Test
    @Transactional
    @DisplayName("그룹에 속해있는 사용자가 쿠폰을 조회하는 경우, 쿠폰이 존재하지 않더라도 해당 그룹 정보는 결과에 포함된다")
    void getAllVoucherWithUserExistInGroupAndNoVoucher() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        String userGroupAlias = "그룹 별칭";
        linkUserWithGroup(user, group, userGroupAlias);

        LocalDate searchStart = LocalDate.of(2024, 12, 10);
        LocalDate searchEnd = LocalDate.of(2025, 1, 10);
        VoucherFilterCondition voucherFilterCondition = VoucherFilterCondition.of(
                VoucherStatus.forDisplayVoucherStatus(), searchStart, searchEnd);

        //when
        SliceResponse<VoucherListResponse> allVoucher = voucherService.getAllVoucher(user.getId(),
                group.getId(), null, 3, voucherFilterCondition);

        VoucherListResponse voucherListResponse = allVoucher.getContent().get(0);

        //then
        assertThat(allVoucher).isNotNull();
        assertThat(allVoucher.getSize()).isEqualTo(3);
        assertThat(allVoucher.getContent().size()).isEqualTo(1);

        assertThat(voucherListResponse.vouchers()).isEmpty();
        assertThat(voucherListResponse.groupTitle()).isEqualTo(userGroupAlias);
    }

    @Test
    @DisplayName("그룹에 속해있지 않은 사용자가 쿠폰을 조회하는 경우 예외가 발생한다")
    void getAllVoucherWithNotExistInGroup() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        LocalDate expiration = LocalDate.of(2025, 1, 1);
        Voucher voucher1 = Voucher.createNewVoucher(user, group, "voucher name1", "imageKey",
                expiration);
        Voucher voucher2 = Voucher.createNewVoucher(user, group, "voucher name2", "imageKey",
                expiration);
        Voucher voucher3 = Voucher.createNewVoucher(user, group, "voucher name3", "imageKey",
                expiration);
        voucherRepository.saveAll(List.of(voucher1, voucher2, voucher3));

        LocalDate searchStart = LocalDate.of(2024, 12, 10);
        LocalDate searchEnd = LocalDate.of(2025, 1, 10);
        VoucherFilterCondition voucherFilterCondition = VoucherFilterCondition.of(
                VoucherStatus.forDisplayVoucherStatus(), searchStart, searchEnd);

        //when //then
        assertThatThrownBy(() -> voucherService.getAllVoucher(user.getId(),
                group.getId(), null, 3, voucherFilterCondition)).isInstanceOf(
                InvalidAccessException.class);
    }

    @Test
    @DisplayName("만료일 필터 : 만료일 필터를 설정하여 쿠폰을 조회할 경우, 지정한 날짜 내에 해당되는 쿠폰만 결과에 포함된다")
    void getAllVoucherWithExpiredFilter() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        String userGroupAlias = "그룹 별칭";
        UserGroup userGroup = linkUserWithGroup(user, group, userGroupAlias);

        String voucherName = "voucher name";
        Voucher voucher1 = Voucher.createNewVoucher(user, group, voucherName, "imageKey",
                LocalDate.of(2025, 1, 1));
        Voucher voucher2 = Voucher.createNewVoucher(user, group, voucherName, "imageKey",
                LocalDate.of(2025, 3, 1));
        Voucher voucher3 = Voucher.createNewVoucher(user, group, voucherName, "imageKey",
                LocalDate.of(2025, 2, 15));
        voucherRepository.saveAll(List.of(voucher1, voucher2, voucher3));

        LocalDate searchStart = LocalDate.of(2025, 1, 1);
        LocalDate searchEnd = LocalDate.of(2025, 2, 28);
        VoucherFilterCondition voucherFilterCondition = VoucherFilterCondition.of(
                VoucherStatus.forDisplayVoucherStatus(), searchStart, searchEnd);

        //when
        String preSignedImageUrl = "presignedImageUrlResult";
        given(imageService.getPresignedImageUrl(any(), any())).willReturn(preSignedImageUrl);

        SliceResponse<VoucherListResponse> allVoucher = voucherService.getAllVoucher(user.getId(),
                group.getId(), null, 3, voucherFilterCondition);

        VoucherListResponse content = allVoucher.getContent().get(0);

        //then
        assertThat(allVoucher).isNotNull();
        assertThat(allVoucher.getSize()).isEqualTo(3);
        assertThat(allVoucher.getContent().size()).isEqualTo(1);

        assertThat(content).isNotNull();
        assertThat(content.vouchers()).hasSize(2);
        assertThat(content.vouchers())
                .extracting(VouchersResponse::id)
                .containsExactlyInAnyOrder(voucher1.getId(), voucher3.getId())
                .doesNotContain(voucher2.getId());

    }

    @Test
    @DisplayName("만료일 필터 : 만료일 필터 내역을 설정하지 않고 쿠폰을 조회할 경우, 조회일로부터 30일 이내로 만료되는 쿠폰만 결과에 포함된다")
    void getAllVoucherWithDefaultExpiredFilter() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        String userGroupAlias = "그룹 별칭";
        UserGroup userGroup = linkUserWithGroup(user, group, userGroupAlias);

        String voucherName = "voucher name";
        Voucher voucher1 = Voucher.createNewVoucher(user, group, voucherName, "imageKey",
                LocalDate.of(2024, 12, 31));
        Voucher voucher2 = Voucher.createNewVoucher(user, group, voucherName, "imageKey",
                LocalDate.of(2024, 11, 30));
        Voucher voucher3 = Voucher.createNewVoucher(user, group, voucherName, "imageKey",
                LocalDate.of(2025, 1, 1));
        voucherRepository.saveAll(List.of(voucher1, voucher2, voucher3));

        LocalDate searchStart = null;
        LocalDate searchEnd = null;
        VoucherFilterCondition voucherFilterCondition = VoucherFilterCondition.of(
                VoucherStatus.forDisplayVoucherStatus(), searchStart, searchEnd);

        //when
        String preSignedImageUrl = "presignedImageUrlResult";
        given(imageService.getPresignedImageUrl(any(), any())).willReturn(preSignedImageUrl);

        SliceResponse<VoucherListResponse> allVoucher = voucherService.getAllVoucher(user.getId(),
                group.getId(), null, 3, voucherFilterCondition);

        VoucherListResponse content = allVoucher.getContent().get(0);

        //then
        assertThat(content).isNotNull();
        assertThat(content.vouchers()).hasSize(1);
        assertThat(content.vouchers())
                .extracting(VouchersResponse::id)
                .containsExactlyInAnyOrder(voucher1.getId())
                .doesNotContain(voucher2.getId(), voucher3.getId());

    }

    @Test
    @DisplayName("쿠폰 상태 필터 : 쿠폰 상태 필터를 설정하여 쿠폰을 조회할 경우, 설정한 상태의 쿠폰만 결과에 포함된다")
    void getAllVoucherWithStatusFilter() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        String userGroupAlias = "그룹 별칭";
        UserGroup userGroup = linkUserWithGroup(user, group, userGroupAlias);

        LocalDate expiration = LocalDate.of(2024, 12, 31);
        Voucher voucher1 = Voucher.builder().user(user).group(group).expiration(expiration)
                .status(VoucherStatus.AVAILABLE).build();
        Voucher voucher2 = Voucher.builder().user(user).group(group).expiration(expiration)
                .status(VoucherStatus.EXPIRED).build();
        Voucher voucher3 = Voucher.builder().user(user).group(group).expiration(expiration)
                .status(VoucherStatus.USED).build();
        voucherRepository.saveAll(List.of(voucher1, voucher2, voucher3));

        LocalDate searchStart = null;
        LocalDate searchEnd = null;
        List<VoucherStatus> status = List.of(VoucherStatus.EXPIRED);
        VoucherFilterCondition voucherFilterCondition = VoucherFilterCondition.of(
                status, searchStart, searchEnd);

        //when
        String preSignedImageUrl = "presignedImageUrlResult";
        given(imageService.getPresignedImageUrl(any(), any())).willReturn(preSignedImageUrl);

        SliceResponse<VoucherListResponse> allVoucher = voucherService.getAllVoucher(user.getId(),
                group.getId(), null, 3, voucherFilterCondition);

        VoucherListResponse content = allVoucher.getContent().get(0);

        //then
        assertThat(content).isNotNull();
        assertThat(content.vouchers())
                .extracting(VouchersResponse::id)
                .contains(voucher2.getId());

    }

    @Test
    @DisplayName("쿠폰 상태 필터 : 쿠폰 상태 필터를 설정하지 않고 쿠폰을 조회할 경우, 사용가능, 사용완료 상태인 쿠폰만 결과에 조회된다")
    void getAllVoucherWithDefaultStatusFilter() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        String userGroupAlias = "그룹 별칭";
        UserGroup userGroup = linkUserWithGroup(user, group, userGroupAlias);

        LocalDate expiration = LocalDate.of(2024, 12, 31);
        Voucher voucher1 = Voucher.builder().user(user).group(group).expiration(expiration)
                .status(VoucherStatus.AVAILABLE).build();
        Voucher voucher2 = Voucher.builder().user(user).group(group).expiration(expiration)
                .status(VoucherStatus.EXPIRED).build();
        Voucher voucher3 = Voucher.builder().user(user).group(group).expiration(expiration)
                .status(VoucherStatus.USED).build();
        voucherRepository.saveAll(List.of(voucher1, voucher2, voucher3));

        LocalDate searchStart = null;
        LocalDate searchEnd = null;
        List<VoucherStatus> status = null;
        VoucherFilterCondition voucherFilterCondition = VoucherFilterCondition.of(
                status, searchStart, searchEnd);

        //when
        String preSignedImageUrl = "presignedImageUrlResult";
        given(imageService.getPresignedImageUrl(any(), any())).willReturn(preSignedImageUrl);

        SliceResponse<VoucherListResponse> allVoucher = voucherService.getAllVoucher(user.getId(),
                group.getId(), null, 3, voucherFilterCondition);

        VoucherListResponse content = allVoucher.getContent().get(0);

        //then
        assertThat(content).isNotNull();
        assertThat(content.vouchers())
                .extracting(VouchersResponse::id)
                .contains(voucher1.getId(), voucher3.getId());

    }


    @TestFactory
    @Transactional
    @DisplayName("그룹에 속한 사용자는 그룹에 등록된 쿠폰의 상태를 변경 가능하다")
    Stream<DynamicTest> changeVoucherStatusWithUserExistInGroup() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        String userGroupAlias = "그룹 별칭";
        linkUserWithGroup(user, group, userGroupAlias);

        String voucherName = "voucher name";
        LocalDate expiration = LocalDate.of(2025, 1, 1);
        Voucher voucher = Voucher.createNewVoucher(user, group, voucherName, "imageKey",
                expiration);
        voucherRepository.save(voucher);

        return Stream.of(
                DynamicTest.dynamicTest("사용가능 상태인 쿠폰을 사용완료로 변경한다", () -> {
                    //when
                    voucherService.changeVoucherStatus(user.getId(), group.getId(),
                            voucher.getId());

                    //then
                    assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.USED);
                }),
                DynamicTest.dynamicTest("사용완료 상태인 쿠폰을 사용가능으로 변경한다.", () -> {
                    //when
                    voucherService.changeVoucherStatus(user.getId(), group.getId(),
                            voucher.getId());

                    //then
                    assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.AVAILABLE);
                })
        );
    }

    private UserGroup linkUserWithGroup(User user, Group group, String alias) {
        UserGroup userGroup = UserGroup.builder()
                .user(user).group(group).groupTitleAlias(alias).joinStatus(JoinStatus.JOINED)
                .build();
        userGroupRepository.save(userGroup);
        return userGroup;
    }

    public Group createTestGroup(String inviteCode) {
        User user = User.builder().build();
        userRepository.save(user);
        return Group.builder().inviteCode(inviteCode).leaderUser(user).build();
    }
}