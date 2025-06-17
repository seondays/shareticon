package seondays.shareticon.voucher;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import seondays.shareticon.login.CustomOAuth2User;
import seondays.shareticon.voucher.dto.CreateVoucherRequest;
import seondays.shareticon.voucher.dto.VoucherListResponse;
import seondays.shareticon.voucher.dto.VouchersResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vouchers")
public class VoucherController {

    private final VoucherService voucherService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VouchersResponse> registerVoucher(
            @AuthenticationPrincipal CustomOAuth2User userDetails,
            @RequestPart("request") @Valid CreateVoucherRequest request,
            @RequestPart("image") MultipartFile image) {
        Long userId = userDetails.getId();
        VouchersResponse response = voucherService.register(request, userId, image);
        return ResponseEntity.created(URI.create("/vouchers/" + response.id())).body(response);
    }

    @DeleteMapping(value = "/group/{groupId}/voucher/{voucherId}")
    public ResponseEntity<Void> deleteVoucher(
            @AuthenticationPrincipal CustomOAuth2User userDetails,
            @PathVariable("groupId") Long groupId,
            @PathVariable("voucherId") Long voucherId) {
        Long userId = userDetails.getId();
        voucherService.delete(userId, groupId, voucherId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/{groupId}")
    public ResponseEntity<Slice<VoucherListResponse>> getAllVoucherInGroup(
            @AuthenticationPrincipal CustomOAuth2User userDetails,
            @PathVariable("groupId") Long groupId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = userDetails.getId();
        Slice<VoucherListResponse> response = voucherService.getAllVoucher(userId, groupId,
                cursorId, pageSize);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/group/{groupId}/voucher/{voucherId}")
    public ResponseEntity<Void> changeVoucherStatus(
            @AuthenticationPrincipal CustomOAuth2User userDetails,
            @PathVariable("groupId") Long groupId,
            @PathVariable("voucherId") Long voucherId) {
        Long userId = userDetails.getId();
        voucherService.changeVoucherStatus(userId, groupId, voucherId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
