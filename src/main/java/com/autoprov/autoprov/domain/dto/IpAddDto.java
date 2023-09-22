package com.autoprov.autoprov.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IpAddDto {
    private Long id;

    private String networkAddress;
    private String hostAddress;
    private String status;
    private String clientId;
    private Integer vlanId;

}