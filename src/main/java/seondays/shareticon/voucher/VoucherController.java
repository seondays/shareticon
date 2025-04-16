package seondays.shareticon.voucher;

import java.net.URI;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import seondays.shareticon.voucher.dto.UserGroupInformationRequest;
import seondays.shareticon.voucher.dto.VouchersResponse;

@RestController
@RequestMapping("/vouchers")
public class VoucherController {

    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VouchersResponse> registerVoucher(
            @RequestPart("request") UserGroupInformationRequest request,
            @RequestPart("image") MultipartFile image) {
        VouchersResponse response = voucherService.register(request, image);
        return ResponseEntity.created(URI.create("/vouchers/" + response.id())).body(response);
    }

    @DeleteMapping(value = "/{userId}/{groupId}/{voucherId}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable("userId") Long userId,
            @PathVariable("groupId") Long groupId, @PathVariable("voucherId") Long voucherId) {
        voucherService.delete(userId, groupId, voucherId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/{userId}/{groupId}")
    public ResponseEntity<Slice<VouchersResponse>> getAllVoucherInGroup(
            @PathVariable("userId") Long userId, @PathVariable("groupId") Long groupId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int pageSize) {
        Slice<VouchersResponse> response = voucherService.getAllVoucher(userId, groupId,
                cursorId, pageSize);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}/{groupId}/{voucherId}")
    public ResponseEntity<Void> changeVoucherStatus(@PathVariable("userId") Long userId,
            @PathVariable("groupId") Long groupId, @PathVariable("voucherId") Long voucherId) {
        voucherService.changeVoucherStatus(userId, groupId, voucherId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
