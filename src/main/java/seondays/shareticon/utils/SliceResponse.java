package seondays.shareticon.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Slice;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SliceResponse<T> {

    @Getter
    private List<T> content;
    private boolean hasNext;
    @Getter
    private int size;

    public static <T> SliceResponse<T> from(Slice<T> slice) {
        return new SliceResponse<>(slice.getContent(), slice.hasNext(), slice.getSize());
    }

    public static <T> SliceResponse<T> of(T response, boolean hasNext, int size) {
        return new SliceResponse<>(List.of(response), hasNext, size);
    }

    @JsonProperty("hasNext")
    public boolean hasNext() {
        return hasNext;
    }
}
