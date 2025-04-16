package seondays.shareticon.voucher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import seondays.shareticon.exception.GroupNotFoundException;
import seondays.shareticon.exception.IllegalVoucherImageException;
import seondays.shareticon.exception.ImageUploadException;
import seondays.shareticon.exception.InvalidAccessVoucherException;
import seondays.shareticon.exception.UserNotFoundException;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;
import seondays.shareticon.image.ImageService;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.userGroup.UserGroup;
import seondays.shareticon.userGroup.UserGroupRepository;
import seondays.shareticon.voucher.dto.UserGroupInformationRequest;
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
                .id(1L)
                .build();
        Group group = Group.builder().inviteCode("123").build();
        userRepository.save(user);
        groupRepository.save(group);
        linkUserWithGroup(user, group);

        UserGroupInformationRequest request = new UserGroupInformationRequest(user.getId(),
                group.getId());

        MockMultipartFile mockImage = new MockMultipartFile(
                "test",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        //when
        given(imageService.uploadImage(any()))
                .willReturn("https://test/test.jpg");

        VouchersResponse response = voucherService.register(request, mockImage);

        //then
        Voucher voucher = voucherRepository.findById(response.id())
                .orElseThrow();

        assertThat(response).isNotNull();
        assertThat(voucher.getUser().getId()).isEqualTo(request.userId());
        assertThat(voucher.getGroup().getId()).isEqualTo(request.groupId());
        assertThat(voucher.getImage()).isEqualTo("https://test/test.jpg");
    }

    @Test
    @DisplayName("쿠폰 등록시 첨부 파일이 이미지 파일이 아니라면 예외가 발생한다.")
    void voucherRegisterWithNoImage() {
        //given
        User user = User.builder()
                .id(1L)
                .build();
        Group group = Group.builder().build();
        userRepository.save(user);
        groupRepository.save(group);
        linkUserWithGroup(user, group);

        UserGroupInformationRequest request = new UserGroupInformationRequest(user.getId(),
                group.getId());

        MockMultipartFile mockImage = new MockMultipartFile(
                "test",
                "test.jpg",
                "application/json",
                "test".getBytes()
        );

        //when //then
        assertThatThrownBy(() -> voucherService.register(request, mockImage)).isInstanceOf(
                IllegalVoucherImageException.class);
    }

    @Test
    @DisplayName("그룹에 참여하지 않은 사용자가 해당 그룹에 속하는 쿠폰을 등록하면 예외가 발생한다.")
    void registerVoucherWithNoGroup() {
        //given
        User user = User.builder()
                .id(1L)
                .build();
        Group group = Group.builder().build();
        userRepository.save(user);
        groupRepository.save(group);

        UserGroupInformationRequest request = new UserGroupInformationRequest(user.getId(),
                group.getId());

        MockMultipartFile mockImage = new MockMultipartFile(
                "test",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        //when //then
        assertThatThrownBy(() -> voucherService.register(request, mockImage)).isInstanceOf(
                InvalidAccessVoucherException.class);

    }

    @Test
    @DisplayName("존재하지 않는 그룹에 쿠폰을 등록하는 경우 예외가 발생한다.")
    void registerVoucherWithNoExistGroup() {
        //given
        Group group = Group.builder().build();
        groupRepository.save(group);

        Long noExistUserId = 1L;
        UserGroupInformationRequest request = new UserGroupInformationRequest(noExistUserId,
                group.getId());

        MockMultipartFile mockImage = new MockMultipartFile(
                "test",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        //when //then
        assertThatThrownBy(() -> voucherService.register(request, mockImage)).isInstanceOf(
                UserNotFoundException.class);

    }

    @Test
    @DisplayName("존재하지 않는 그룹에 쿠폰을 등록하는 경우 예외가 발생한다.")
    void registerVoucherWithNoExistUser() {
        //given
        User user = User.builder()
                .id(1L)
                .build();
        userRepository.save(user);

        Long noExistGroupId = 1L;
        UserGroupInformationRequest request = new UserGroupInformationRequest(user.getId(),
                noExistGroupId);

        MockMultipartFile mockImage = new MockMultipartFile(
                "test",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        //when //then
        assertThatThrownBy(() -> voucherService.register(request, mockImage)).isInstanceOf(
                GroupNotFoundException.class);

    }

    @Test
    @DisplayName("쿠폰 등록 시 이미지 업로드가 실패하는 경우 예외가 발생하고 Voucher도 롤백된다.")
    void registerVoucherWhenImageUploadFail() {
        //given
        User user = User.builder()
                .id(1L)
                .build();
        Group group = Group.builder().build();
        userRepository.save(user);
        groupRepository.save(group);
        linkUserWithGroup(user, group);

        UserGroupInformationRequest request = new UserGroupInformationRequest(user.getId(),
                group.getId());

        MockMultipartFile mockImage = new MockMultipartFile(
                "test",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        //when //then
        given(imageService.uploadImage(any()))
                .willThrow(ImageUploadException.class);

        assertThatThrownBy(() -> voucherService.register(request, mockImage)).isInstanceOf(
                ImageUploadException.class);

        List<Voucher> vouchers = voucherRepository.findAll();
        assertThat(vouchers).isEmpty();

    }

    private void linkUserWithGroup(User user, Group group) {
        UserGroup userGroup = UserGroup.builder().user(user).group(group).build();
        userGroupRepository.save(userGroup);
    }
}