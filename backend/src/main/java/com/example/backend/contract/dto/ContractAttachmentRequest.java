package com.example.backend.contract.dto;

import jakarta.validation.constraints.NotBlank;

public record ContractAttachmentRequest(
        @NotBlank String fileName,
        @NotBlank String fileType,
        @NotBlank String uploader,
        String remark
) {
}
