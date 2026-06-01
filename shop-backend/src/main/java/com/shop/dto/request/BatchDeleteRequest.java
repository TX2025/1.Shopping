package com.shop.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class BatchDeleteRequest {
    private List<Long> ids;
}
