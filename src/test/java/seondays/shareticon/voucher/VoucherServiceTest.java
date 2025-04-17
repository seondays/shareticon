package seondays.shareticon.voucher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Slice;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import seondays.shareticon.exception.GroupNotFoundException;
import seondays.shareticon.exception.IllegalVoucherImageException;
import seondays.shareticon.exception.ImageUploadException;
import seondays.shareticon.exception.InvalidAccessVoucherException;
import seondays.shareticon.exception.InvalidVoucherDeleteException;
import seondays.shareticon.exception.UserNotFoundException;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;
import seondays.shareticon.image.ImageService;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.userGroup.UserGroup;
import seondays.shareticon.userGroup.UserGroupRepository;
import seondays.shareticon.voucher.dto.CreateVoucherRequest;
import seondays.shareticon.voucher.dto.VouchersResponse;

@ActiveProfiles("test")
@SpringBootTest
class VoucherServiceTest {

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
    private ImageService imageService;

    @AfterEach
    void tearDown() {
        userGroupRepository.deleteAllInBatch();
        voucherRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        groupRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("쿠폰 이미지 파일과 유저그룹 정보를 가지고 쿠폰을 등록한다.")
    void voucherRegisterWithImage() {
        //given
        User user = User.builder()
                .build();
        Group group = Group.builder().inviteCode("123").build();
        userRepository.save(user);
        groupRepository.save(group);
        linkUserWithGroup(user, group);

        CreateVoucherRequest request = new CreateVoucherRequest(group.getId());

        MockMultipartFile mockImage = new MockMultipartFile(
                "test",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        //when
        given(imageService.uploadImage(any()))
                .willReturn("https://test/test.jpg");

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
        User user = User.builder()
                .build();
        Group group = Group.builder().build();
        userRepository.save(user);
        groupRepository.save(group);
        linkUserWithGroup(user, group);

        CreateVoucherRequest request = new CreateVoucherRequest(group.getId());

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
        Group group = Group.builder().build();
        userRepository.save(user);
        groupRepository.save(group);

        CreateVoucherRequest request = new CreateVoucherRequest(group.getId());

        MockMultipartFile mockImage = new MockMultipartFile(
                "test",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        //when //then
        assertThatThrownBy(
                () -> voucherService.register(request, user.getId(), mockImage)).isInstanceOf(
                InvalidAccessVoucherException.class);

    }

    @Test
    @DisplayName("존재하지 않는 그룹에 쿠폰을 등록하는 경우 예외가 발생한다.")
    void registerVoucherWithNoExistGroup() {
        //given
        Group group = Group.builder().build();
        groupRepository.save(group);

        Long noExistUserId = 1L;
        CreateVoucherRequest request = new CreateVoucherRequest(group.getId());

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
        CreateVoucherRequest request = new CreateVoucherRequest(noExistGroupId);

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
        Group group = Group.builder().build();
        userRepository.save(user);
        groupRepository.save(group);
        linkUserWithGroup(user, group);

        CreateVoucherRequest request = new CreateVoucherRequest(group.getId());

        MockMultipartFile mockImage = new MockMultipartFile(
                "test",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        //when //then
        given(imageService.uploadImage(any()))
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

        Group group = Group.builder().build();
        groupRepository.save(group);
        linkUserWithGroup(user, group);

        Voucher voucher = Voucher.createAvailableStatus(user, group);
        voucherRepository.save(voucher);

        //when
        voucherService.delete(user.getId(), group.getId(), voucher.getId());

        //then
        Optional<Voucher> result = voucherRepository.findById(voucher.getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("쿠폰을 등록하지 않은 사람이 쿠폰을 삭제하면 예외가 발생한다")
    void deleteVoucherWithNotRegisterUser() {
        //given
        User registerUser = User.builder().build();
        User NotRegisterUser = User.builder().build();
        userRepository.save(registerUser);
        userRepository.save(NotRegisterUser);

        Group group = Group.builder().build();
        groupRepository.save(group);
        linkUserWithGroup(registerUser, group);
        linkUserWithGroup(NotRegisterUser, group);

        Voucher voucher = Voucher.createAvailableStatus(registerUser, group);
        voucherRepository.save(voucher);

        //when //then
        assertThatThrownBy(() ->
                voucherService.delete(NotRegisterUser.getId(), group.getId(), voucher.getId()))
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

        Group group = Group.builder().build();
        groupRepository.save(group);
        linkUserWithGroup(userExistInGroup, group);

        Voucher voucher = Voucher.createAvailableStatus(userExistInGroup, group);
        voucherRepository.save(voucher);

        //when //then
        assertThatThrownBy(() ->
                voucherService.delete(userNoExistInGroup.getId(), group.getId(), voucher.getId()))
                .isInstanceOf(InvalidVoucherDeleteException.class);
    }

    @Test
    @DisplayName("그룹에 속해있는 사용자가 쿠폰을 조회하는 경우 전체 쿠폰 결과를 담은 slice를 반환한다")
    void getAllVoucherWithUserExistInGroup() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        Group group = Group.builder().build();
        groupRepository.save(group);
        linkUserWithGroup(user, group);

        Voucher voucher1 = Voucher.createAvailableStatus(user, group);
        Voucher voucher2 = Voucher.createAvailableStatus(user, group);
        Voucher voucher3 = Voucher.createAvailableStatus(user, group);
        voucherRepository.saveAll(List.of(voucher1, voucher2, voucher3));

        //when
        Slice<VouchersResponse> allVoucher = voucherService.getAllVoucher(user.getId(),
                group.getId(), null, 3);

        //then
        assertThat(allVoucher).isNotNull();
        assertThat(allVoucher.getSize()).isEqualTo(3);
        assertThat(allVoucher.getContent())
                .extracting("id")
                .contains(voucher3.getId(), voucher2.getId(), voucher1.getId());
    }

    @Test
    @DisplayName("그룹에 속해있지 않은 사용자가 쿠폰을 조회하는 경우 예외가 발생한다")
    void getAllVoucherWithNotExistInGroup() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        Group group = Group.builder().build();
        groupRepository.save(group);

        Voucher voucher1 = Voucher.createAvailableStatus(user, group);
        Voucher voucher2 = Voucher.createAvailableStatus(user, group);
        Voucher voucher3 = Voucher.createAvailableStatus(user, group);
        voucherRepository.saveAll(List.of(voucher1, voucher2, voucher3));

        //when //then
        assertThatThrownBy(() -> voucherService.getAllVoucher(user.getId(),
                group.getId(), null, 3)).isInstanceOf(InvalidAccessVoucherException.class);
    }

    private void linkUserWithGroup(User user, Group group) {
        UserGroup userGroup = UserGroup.builder().user(user).group(group).build();
        userGroupRepository.save(userGroup);
    }
}