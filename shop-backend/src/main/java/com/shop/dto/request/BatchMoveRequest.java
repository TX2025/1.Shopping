package com.shop.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class BatchMoveRequest {
    private List<Long> ids;
    private Long targetCategoryId;
}
